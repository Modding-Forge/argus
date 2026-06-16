package com.argus.client.sodium;

import com.argus.config.ArgusConfigHolder;
import com.argus.client.benchmark.ArgusBenchmark;
import com.argus.client.render.CtmMinecraftNeighborView;
import com.argus.ctm.CompactCtmTiles;
import com.argus.ctm.CtmMaterialEntry;
import com.argus.ctm.CtmMaterialTable;
import com.argus.ctm.CtmOverlayTile;
import com.argus.ctm.CtmRenderResolver;
import com.argus.ctm.CtmRenderScratch;
import com.argus.ctm.CtmRenderSelection;
import com.argus.ctm.CtmTileResolver;
import com.argus.ctm.Faces;
import com.argus.platform.Platforms;
import com.argus.resource.NamespaceId;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteFinderCache;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sodium realization adapter for Argus's backend-neutral CTM selections.
 *
 * <p>The adapter runs before Sodium shades and buffers a terrain quad. It
 * resolves a shared {@link CtmRenderSelection}, then either remaps the current
 * quad to a replacement atlas sprite or records overlay quads that the mixin
 * will feed back into Sodium's normal quad pipeline.
 *
 * <p>The implementation has no Fabric imports. The only loader-specific piece
 * is the Mixin that calls it from the current Fabric client source set.
 *
 * <h2>Threading</h2>
 *
 * <p>Owned by one Sodium {@code BlockRenderer} instance. The processor caches a
 * neighbour view for the current Sodium level slice and reads immutable CTM
 * registry/material snapshots.
 *
 * <h2>Performance</h2>
 *
 * <p>Performance: HOT PATH. Allocation policy: no allocation on disabled,
 * empty-table, non-full-face, or non-matching quads. Matching quads use
 * null-returning material lookups and a reusable resolver scratch buffer.
 */
public final class CtmSodiumQuadProcessor {

    private static final float FULL_FACE_EPSILON = 0.001F;
    private static final Logger LOGGER =
            LoggerFactory.getLogger("argus/sodium-ctm");
    private static final int DEBUG_LOG_LIMIT = 96;
    private static final AtomicInteger DEBUG_LOGS = new AtomicInteger();
    private static final int GLASS_DEBUG_LOG_LIMIT = 512;
    private static final AtomicInteger GLASS_DEBUG_LOGS =
            new AtomicInteger();

    private final CtmSodiumSpriteLookup spriteLookup =
            new CtmSodiumSpriteLookup();
    private final CtmSodiumOverlayTint overlayTint =
            new CtmSodiumOverlayTint();
    private final CtmSodiumLayerPolicy layerPolicy =
            new CtmSodiumLayerPolicy();
    private final CtmRenderResolver resolver =
            new CtmRenderResolver(Platforms.get().ctmRegistry());
    private final CtmRenderScratch renderScratch = new CtmRenderScratch();

    private static final ConcurrentMap<Identifier, NamespaceId> SPRITE_IDS =
            new ConcurrentHashMap<>();
    private static final ConcurrentMap<Block, String> BLOCK_IDS =
            new ConcurrentHashMap<>();

    private @Nullable BlockAndTintGetter viewSource;
    private @Nullable CtmMinecraftNeighborView view;

    /**
     * Resolves and applies CTM for one Sodium terrain quad.
     *
     * @return true when {@code plan} contains overlay quads to emit later
     */
    public boolean prepare(MutableQuadViewImpl quad,
                           BlockAndTintGetter level,
                           BlockState state,
                           BlockPos pos,
                           MutableQuadViewImpl overlaySource,
                           CtmSodiumQuadPlan plan) {
        plan.clear();
        if (!ArgusConfigHolder.get().ctmActive()) {
            return false;
        }
        CtmMaterialTable materialTable = CtmMaterialTable.current();
        if (materialTable.isEmpty() || level == null
                || state == null || pos == null) {
            return false;
        }
        Direction direction = faceDirection(quad);
        if (direction == null) {
            return false;
        }
        int face = direction.get3DDataValue();
        boolean fullBlockExteriorFace =
                isFullBlockExteriorFace(quad, direction);
        if (!fullBlockExteriorFace && !isFullPlanarFace(quad, direction)) {
            return false;
        }
        TextureAtlasSprite sourceSprite = sourceSprite(quad);
        if (sourceSprite == null) {
            return false;
        }
        String blockId = blockId(state);
        NamespaceId baseSprite = namespaceId(sourceSprite);
        long prefilterStart = ArgusBenchmark.start();
        if (!resolver.hasCandidates(blockId, baseSprite, face)) {
            ArgusBenchmark.record(ArgusBenchmark.CTM_PREFILTER,
                    prefilterStart);
            return false;
        }
        ArgusBenchmark.record(ArgusBenchmark.CTM_PREFILTER, prefilterStart);
        long neighborStart = ArgusBenchmark.start();
        CtmMinecraftNeighborView neighborView = neighborView(level, pos, state);
        neighborView.setSpriteForFace(0, 0, 0, face, sourceSprite);
        ArgusBenchmark.record(ArgusBenchmark.CTM_NEIGHBOR_VIEW,
                neighborStart);
        long resolveStart = ArgusBenchmark.start();
        boolean resolved = resolver.resolveInto(
                blockId,
                baseSprite,
                neighborView,
                pos.getX(), pos.getY(), pos.getZ(),
                face,
                renderScratch);
        ArgusBenchmark.record(ArgusBenchmark.CTM_RESOLVE, resolveStart);
        if (!resolved || !renderScratch.hasWork()) {
            debugSeenQuad(blockId, pos, direction, sourceSprite, null,
                    neighborView, "no-selection");
            return false;
        }
        CtmRenderSelection selection = renderScratch.replacement();
        if (selection != null && !fullBlockExteriorFace) {
            debugSeenQuad(blockId, pos, direction, sourceSprite, selection,
                    neighborView, "not-full-exterior-face");
            selection = null;
        }
        if (selection != null
                && isOppositeNominalGlassDuplicate(quad, blockId, direction)) {
            debugGlassDecision(quad, blockId, pos, direction, sourceSprite,
                    selection, null, neighborView,
                    "discard-model-backface");
            plan.discardOriginal();
            return false;
        }
        if (selection != null && isConnectedGlassInteriorFace(
                quad, state, direction, neighborView, selection, blockId)) {
            debugGlassDecision(quad, blockId, pos, direction, sourceSprite,
                    selection, null, neighborView, "discard-interior");
            plan.discardOriginal();
            return false;
        }
        if (renderScratch.hasOverlays()) {
            overlaySource.copyFrom(quad);
            long overlayStart = ArgusBenchmark.start();
            buildOverlayPlan(renderScratch, materialTable, state, level, pos,
                    quad.getRenderType(), plan);
            ArgusBenchmark.record(ArgusBenchmark.CTM_OVERLAY_PLAN,
                    overlayStart);
        }
        if (selection == null) {
            return plan.hasOverlays();
        }
        if (!selection.hasPrimaryTile() || selection.isPrimaryDefault()) {
            debugSeenQuad(blockId, pos, direction, sourceSprite, selection,
                    neighborView,
                    "default-selection");
            return plan.hasOverlays();
        }
        long materialStart = ArgusBenchmark.start();
        if (CtmTileResolver.isCompactFullTileIndex(
                selection.primaryTileIndex())) {
            CtmMaterialEntry material = materialTable.findOrNull(
                    selection.rule(), selection.primaryTileIndex());
            if (material != null && !isGlassLikeBlock(blockId)) {
                TextureAtlasSprite target = spriteLookup.sprite(
                        material.sprite());
                if (target != null && !sameSprite(sourceSprite, target)) {
                    ArgusBenchmark.record(ArgusBenchmark.CTM_MATERIAL,
                            materialStart);
                    debugReplacement(blockId, pos, direction, sourceSprite,
                            selection, target);
                    long remapStart = ArgusBenchmark.start();
                    remapSprite(quad, sourceSprite, target);
                    ArgusBenchmark.record(ArgusBenchmark.CTM_UV_REMAP,
                            remapStart);
                    return plan.hasOverlays();
                }
            }
            overlaySource.copyFrom(quad);
            if (isGlassLikeBlock(blockId)) {
                buildGeneratedCompactReplacementPlan(
                        selection, materialTable, plan);
            } else {
                buildCompactReplacementPlan(
                        selection, materialTable, sourceSprite, plan);
            }
            plan.shapeReplacementQuadrants();
            plan.replacementLayer(layerPolicy.replacementLayer(
                    blockId,
                    quad.getRenderType()));
            if (plan.allReplacementSpritesMatch()
                    && !isGlassLikeBlock(blockId)
                    && !plan.hasOverlays()) {
                TextureAtlasSprite target = plan.replacementSprite(0);
                plan.clear();
                if (sameSprite(sourceSprite, target)) {
                    ArgusBenchmark.record(ArgusBenchmark.CTM_MATERIAL,
                            materialStart);
                    debugSeenQuad(blockId, pos, direction, sourceSprite,
                            selection, neighborView, "compact-same-sprite");
                    return false;
                }
                ArgusBenchmark.record(ArgusBenchmark.CTM_MATERIAL,
                        materialStart);
                debugReplacement(blockId, pos, direction, sourceSprite,
                        selection, target);
                debugGlassDecision(quad, blockId, pos, direction, sourceSprite,
                        selection, target, neighborView, "replace-compact");
                long remapStart = ArgusBenchmark.start();
                remapSprite(quad, sourceSprite, target);
                ArgusBenchmark.record(ArgusBenchmark.CTM_UV_REMAP, remapStart);
                applyReplacementLayer(quad, blockId);
                return plan.hasOverlays();
            }
            debugCompactPlan(blockId, pos, direction, sourceSprite,
                    selection, plan);
            ArgusBenchmark.record(ArgusBenchmark.CTM_MATERIAL, materialStart);
            return plan.hasWork();
        }
        CtmMaterialEntry material = materialTable.findOrNull(
                selection.rule(), selection.primaryTileIndex());
        if (material == null) {
            ArgusBenchmark.record(ArgusBenchmark.CTM_MATERIAL, materialStart);
            return false;
        }
        TextureAtlasSprite target = spriteLookup.sprite(material.sprite());
        if (target == null || sameSprite(sourceSprite, target)) {
            ArgusBenchmark.record(ArgusBenchmark.CTM_MATERIAL, materialStart);
            debugSeenQuad(blockId, pos, direction, sourceSprite, selection,
                    neighborView,
                    target == null ? "target-null" : "same-sprite");
            return false;
        }
        debugReplacement(blockId, pos, direction, sourceSprite,
                selection, target);
        debugGlassDecision(quad, blockId, pos, direction, sourceSprite,
                selection, target, neighborView, "replace");
        ArgusBenchmark.record(ArgusBenchmark.CTM_MATERIAL, materialStart);
        if (shouldPlanTranslucentReplacementBackface(quad)) {
            overlaySource.copyFrom(quad);
            plan.addReplacement(target);
            plan.replacementLayer(layerPolicy.replacementLayer(
                    blockId,
                    quad.getRenderType()));
            return plan.hasWork();
        }
        long remapStart = ArgusBenchmark.start();
        remapSprite(quad, sourceSprite, target);
        ArgusBenchmark.record(ArgusBenchmark.CTM_UV_REMAP, remapStart);
        applyReplacementLayer(quad, blockId);
        return plan.hasOverlays();
    }

    private void buildCompactReplacementPlan(CtmRenderSelection selection,
                                             CtmMaterialTable materialTable,
                                             TextureAtlasSprite sourceSprite,
                                             CtmSodiumQuadPlan plan) {
        int fullTile = CtmTileResolver.compactFullTileIndex(
                selection.primaryTileIndex());
        for (int quadrant = 0;
             quadrant < CompactCtmTiles.QUADRANT_COUNT;
             quadrant++) {
            int sourceTile = CompactCtmTiles.sourceTileIndexForQuadrant(
                    fullTile, quadrant);
            CtmMaterialEntry material = materialTable.findOrNull(
                    selection.rule(), sourceTile);
            if (material == null) {
                plan.clear();
                return;
            }
            TextureAtlasSprite sprite = spriteLookup.sprite(
                    material.sprite());
            if (sprite == null) {
                plan.clear();
                return;
            }
            plan.addReplacement(sprite);
        }
    }

    private void buildGeneratedCompactReplacementPlan(
            CtmRenderSelection selection,
            CtmMaterialTable materialTable,
            CtmSodiumQuadPlan plan) {
        CtmMaterialEntry material = materialTable.findOrNull(
                selection.rule(), selection.primaryTileIndex());
        if (material == null) {
            return;
        }
        TextureAtlasSprite sprite = spriteLookup.sprite(
                material.sprite());
        if (sprite == null) {
            return;
        }
        for (int quadrant = 0;
             quadrant < CompactCtmTiles.QUADRANT_COUNT;
             quadrant++) {
            plan.addReplacement(sprite);
        }
    }

    private static void debugCompactPlan(String blockId,
                                         BlockPos pos,
                                         Direction direction,
                                         TextureAtlasSprite sourceSprite,
                                         CtmRenderSelection selection,
                                         CtmSodiumQuadPlan plan) {
        if (!ctmDebugLoggingActive()) {
            return;
        }
        if (isGlassDebugBlock(blockId)) {
            debugGlassCompactPlan(pos, direction, sourceSprite, selection, plan);
            return;
        }
        if (!("minecraft:iron_block".equals(blockId)
                || "minecraft:gold_block".equals(blockId))) {
            return;
        }
        int index = DEBUG_LOGS.getAndIncrement();
        if (index >= DEBUG_LOG_LIMIT) {
            return;
        }
        int fullTile = CtmTileResolver.compactFullTileIndex(
                selection.primaryTileIndex());
        LOGGER.info("[argus] Sodium compact CTM block={} pos={} face={} "
                        + "base={} primaryTile={} fullTile={} replacements={} "
                        + "q0={} q1={} q2={} q3={}",
                blockId,
                pos,
                direction,
                sourceSprite.contents().name(),
                selection.primaryTileIndex(),
                fullTile,
                plan.replacementCount(),
                compactDebugSprite(plan, 0),
                compactDebugSprite(plan, 1),
                compactDebugSprite(plan, 2),
                compactDebugSprite(plan, 3));
    }

    private static void debugGlassCompactPlan(
            BlockPos pos,
            Direction direction,
            TextureAtlasSprite sourceSprite,
            CtmRenderSelection selection,
            CtmSodiumQuadPlan plan) {
        if (!ctmDebugLoggingActive()) {
            return;
        }
        int index = GLASS_DEBUG_LOGS.getAndIncrement();
        if (index >= GLASS_DEBUG_LOG_LIMIT) {
            return;
        }
        int fullTile = CtmTileResolver.compactFullTileIndex(
                selection.primaryTileIndex());
        LOGGER.info("[argus] Sodium glass compact pos={} face={} "
                        + "base={} primaryTile={} fullTile={} "
                        + "replacements={} q0={} q1={} q2={} q3={}",
                pos,
                direction,
                sourceSprite.contents().name(),
                selection.primaryTileIndex(),
                fullTile,
                plan.replacementCount(),
                compactDebugSprite(plan, 0),
                compactDebugSprite(plan, 1),
                compactDebugSprite(plan, 2),
                compactDebugSprite(plan, 3));
    }

    private static void debugReplacement(String blockId,
                                         BlockPos pos,
                                         Direction direction,
                                         TextureAtlasSprite sourceSprite,
                                         CtmRenderSelection selection,
                                         TextureAtlasSprite target) {
        if (!ctmDebugLoggingActive()) {
            return;
        }
        if (!("minecraft:iron_block".equals(blockId)
                || "minecraft:gold_block".equals(blockId))) {
            return;
        }
        int index = DEBUG_LOGS.getAndIncrement();
        if (index >= DEBUG_LOG_LIMIT) {
            return;
        }
        LOGGER.info("[argus] Sodium CTM replacement block={} pos={} face={} "
                        + "base={} primaryTile={} target={}",
                blockId,
                pos,
                direction,
                sourceSprite.contents().name(),
                selection.primaryTileIndex(),
                target.contents().name());
    }

    private static void debugSeenQuad(String blockId,
                                      BlockPos pos,
                                      Direction direction,
                                      TextureAtlasSprite sourceSprite,
                                      @Nullable CtmRenderSelection selection,
                                      CtmMinecraftNeighborView neighborView,
                                      String reason) {
        if (!ctmDebugLoggingActive()) {
            return;
        }
        if (!("minecraft:iron_block".equals(blockId)
                || "minecraft:gold_block".equals(blockId))) {
            return;
        }
        int index = DEBUG_LOGS.getAndIncrement();
        if (index >= DEBUG_LOG_LIMIT) {
            return;
        }
        LOGGER.info("[argus] Sodium CTM skipped block={} pos={} face={} "
                        + "base={} reason={} rule={} neighbours="
                        + "W:{} E:{} N:{} S:{} NW:{} NE:{} SW:{} SE:{}",
                blockId,
                pos,
                direction,
                sourceSprite.contents().name(),
                reason,
                selection == null
                        ? "<none>"
                        : selection.rule().sourceFile().orElse("<unknown>"),
                neighborView.blockId(-1, 0, 0),
                neighborView.blockId(1, 0, 0),
                neighborView.blockId(0, 0, -1),
                neighborView.blockId(0, 0, 1),
                neighborView.blockId(-1, 0, -1),
                neighborView.blockId(1, 0, -1),
                neighborView.blockId(-1, 0, 1),
                neighborView.blockId(1, 0, 1));
    }

    private static String compactDebugSprite(CtmSodiumQuadPlan plan,
                                             int index) {
        if (index >= plan.replacementCount()) {
            return "<none>";
        }
        return plan.replacementSprite(index).contents().name().toString();
    }

    private static void debugGlassDecision(MutableQuadViewImpl quad,
                                           String blockId,
                                           BlockPos pos,
                                           Direction direction,
                                           TextureAtlasSprite sourceSprite,
                                           CtmRenderSelection selection,
                                           @Nullable TextureAtlasSprite target,
                                           CtmMinecraftNeighborView neighborView,
                                           String action) {
        if (!ctmDebugLoggingActive()) {
            return;
        }
        if (!isGlassDebugBlock(blockId)) {
            return;
        }
        int index = GLASS_DEBUG_LOGS.getAndIncrement();
        if (index >= GLASS_DEBUG_LOG_LIMIT) {
            return;
        }
        int[] d = Faces.delta(direction.get3DDataValue());
        LOGGER.info("[argus] Sodium glass CTM action={} pos={} face={} "
                        + "cull={} nominal={} light={} renderType={} "
                        + "base={} primaryTile={} target={} neighbor={} "
                        + "side0={} side1={} side2={} side3={} "
                        + "diag0={} diag1={} diag2={} diag3={}",
                action,
                pos,
                direction,
                quad.getCullFace(),
                quad.getNominalFace(),
                quad.getLightFace(),
                quad.getRenderType(),
                sourceSprite.contents().name(),
                selection.primaryTileIndex(),
                target == null ? "<none>" : target.contents().name(),
                neighborView.blockId(d[0], d[1], d[2]),
                localSideBlock(neighborView, direction, 0),
                localSideBlock(neighborView, direction, 1),
                localSideBlock(neighborView, direction, 2),
                localSideBlock(neighborView, direction, 3),
                localDiagonalBlock(neighborView, direction, 0),
                localDiagonalBlock(neighborView, direction, 1),
                localDiagonalBlock(neighborView, direction, 2),
                localDiagonalBlock(neighborView, direction, 3));
    }

    private static void debugGlassEarlySkip(MutableQuadViewImpl quad,
                                            String blockId,
                                            BlockPos pos,
                                            TextureAtlasSprite sourceSprite,
                                            String reason) {
        if (!ctmDebugLoggingActive()) {
            return;
        }
        if (!isGlassDebugBlock(blockId)
                && !sourceSprite.contents().name().getPath().contains("glass")) {
            return;
        }
        int index = GLASS_DEBUG_LOGS.getAndIncrement();
        if (index >= GLASS_DEBUG_LOG_LIMIT) {
            return;
        }
        LOGGER.info("[argus] Sodium glass early-skip block={} pos={} "
                        + "reason={} cull={} nominal={} light={} "
                        + "renderType={} base={} bounds=[{}..{}, {}..{}, {}..{}]",
                blockId,
                pos,
                reason,
                quad.getCullFace(),
                quad.getNominalFace(),
                quad.getLightFace(),
                quad.getRenderType(),
                sourceSprite.contents().name(),
                minX(quad),
                maxX(quad),
                minY(quad),
                maxY(quad),
                minZ(quad),
                maxZ(quad));
    }

    private static boolean isGlassDebugBlock(String blockId) {
        return blockId.contains("glass");
    }

    private static boolean ctmDebugLoggingActive() {
        return ArgusConfigHolder.get().ctmDebugLogging();
    }

    private static boolean shouldPlanTranslucentReplacementBackface(
            MutableQuadViewImpl quad) {
        return quad.getRenderType() == ChunkSectionLayer.TRANSLUCENT
                && ArgusConfigHolder.get().duplicateTranslucentBackfaces();
    }

    private static String localSideBlock(CtmMinecraftNeighborView neighborView,
                                         Direction direction,
                                         int index) {
        int[] sides = Faces.orthogonalSides(direction.get3DDataValue());
        int[] d = Faces.delta(sides[index]);
        return neighborView.blockId(d[0], d[1], d[2]);
    }

    private static String localDiagonalBlock(
            CtmMinecraftNeighborView neighborView,
            Direction direction,
            int index) {
        int[] d = Faces.diagonals(direction.get3DDataValue())[index];
        return neighborView.blockId(d[0], d[1], d[2]);
    }

    private void buildOverlayPlan(CtmRenderScratch scratch,
                                  CtmMaterialTable materialTable,
                                  BlockState state,
                                  BlockAndTintGetter level,
                                  BlockPos pos,
                                  @Nullable ChunkSectionLayer sourceLayer,
                                  CtmSodiumQuadPlan plan) {
        for (int i = 0; i < scratch.overlayCount(); i++) {
            CtmOverlayTile overlayTile = scratch.overlay(i);
            CtmMaterialEntry material = materialTable.findOrNull(
                    overlayTile.rule(), overlayTile.tileIndex());
            if (material == null) {
                continue;
            }
            TextureAtlasSprite sprite = spriteLookup.sprite(
                    material.sprite());
            if (sprite == null) {
                continue;
            }
            int color = overlayTint.color(
                    overlayTile.rule(), state, level, pos);
            ChunkSectionLayer overlayLayer = layerPolicy.overlayLayer(
                    overlayTile.rule(), sourceLayer);
            plan.addOverlay(sprite, color, overlayLayer);
        }
    }

    private CtmMinecraftNeighborView neighborView(BlockAndTintGetter level,
                                                  BlockPos pos,
                                                  BlockState state) {
        CtmMinecraftNeighborView current = view;
        if (current == null || viewSource != level) {
            current = new CtmMinecraftNeighborView(level);
            view = current;
            viewSource = level;
        }
        current.reset(pos, state);
        return current;
    }

    private static @Nullable Direction faceDirection(MutableQuadViewImpl quad) {
        Direction direction = quad.getCullFace();
        if (direction != null) {
            return direction;
        }
        direction = quad.getNominalFace();
        if (direction != null) {
            return direction;
        }
        return quad.getLightFace();
    }

    private static @Nullable TextureAtlasSprite sourceSprite(
            MutableQuadViewImpl quad) {
        TextureAtlasSprite sprite = quad.cachedSprite();
        if (sprite != null) {
            return sprite;
        }
        return quad.sprite(SpriteFinderCache.forBlockAtlas());
    }

    private static NamespaceId namespaceId(TextureAtlasSprite sprite) {
        Identifier id = sprite.contents().name();
        NamespaceId cached = SPRITE_IDS.get(id);
        if (cached != null) {
            return cached;
        }
        NamespaceId created = new NamespaceId(id.getNamespace(), id.getPath());
        NamespaceId previous = SPRITE_IDS.putIfAbsent(id, created);
        return previous == null ? created : previous;
    }

    private static String blockId(BlockState state) {
        Block block = state.getBlock();
        String cached = BLOCK_IDS.get(block);
        if (cached != null) {
            return cached;
        }
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        String created = id == null ? "" : id.toString();
        String previous = BLOCK_IDS.putIfAbsent(block, created);
        return previous == null ? created : previous;
    }

    private static boolean isConnectedGlassInteriorFace(
            MutableQuadViewImpl quad,
            BlockState state,
            Direction direction,
            CtmMinecraftNeighborView neighborView,
            CtmRenderSelection selection,
            String blockId) {
        if (selection.isOverlay()) {
            return false;
        }
        if (!isGlassLikeBlock(blockId)) {
            return false;
        }
        int[] d = Faces.delta(direction.get3DDataValue());
        String neighborBlock = neighborView.blockId(d[0], d[1], d[2]);
        return blockId.equals(neighborBlock);
    }

    private static boolean isOppositeNominalGlassDuplicate(
            MutableQuadViewImpl quad,
            String blockId,
            Direction direction) {
        if (!isGlassLikeBlock(blockId)) {
            return false;
        }
        Direction nominal = quad.getNominalFace();
        return nominal != null && nominal == direction.getOpposite();
    }

    private void applyReplacementLayer(MutableQuadViewImpl quad,
                                       String blockId) {
        ChunkSectionLayer replacementLayer = layerPolicy.replacementLayer(
                blockId,
                quad.getRenderType());
        if (replacementLayer != quad.getRenderType()) {
            quad.setRenderType(replacementLayer);
        }
    }

    private static boolean isGlassLikeBlock(String blockId) {
        return blockId.endsWith("_glass")
                || blockId.endsWith("_stained_glass")
                || "minecraft:glass".equals(blockId)
                || "minecraft:tinted_glass".equals(blockId);
    }

    private static boolean sameSprite(TextureAtlasSprite a,
                                      TextureAtlasSprite b) {
        return a.contents().name().equals(b.contents().name());
    }

    /**
     * Remaps already-baked atlas UVs from {@code source} into {@code target}.
     */
    public static void remapSprite(MutableQuadViewImpl quad,
                                   TextureAtlasSprite source,
                                   TextureAtlasSprite target) {
        float srcU0 = source.getU0();
        float srcV0 = source.getV0();
        float srcDu = source.getU1() - srcU0;
        float srcDv = source.getV1() - srcV0;
        if (srcDu == 0.0F || srcDv == 0.0F) {
            return;
        }
        float tgtU0 = target.getU0();
        float tgtV0 = target.getV0();
        float scaleU = (target.getU1() - tgtU0) / srcDu;
        float scaleV = (target.getV1() - tgtV0) / srcDv;
        for (int i = 0; i < 4; i++) {
            float u = tgtU0 + (quad.getTexU(i) - srcU0) * scaleU;
            float v = tgtV0 + (quad.getTexV(i) - srcV0) * scaleV;
            quad.setUV(i, u, v);
        }
        quad.cachedSprite(target);
    }

    /**
     * Returns true only for quads that cover one complete exterior block face.
     */
    private static boolean isFullBlockExteriorFace(MutableQuadViewImpl quad,
                                                   Direction direction) {
        float minX = minX(quad);
        float minY = minY(quad);
        float minZ = minZ(quad);
        float maxX = maxX(quad);
        float maxY = maxY(quad);
        float maxZ = maxZ(quad);
        return switch (direction.get3DDataValue()) {
            case Faces.DOWN -> near(minY, 0.0F)
                    && spansUnit(minX, maxX) && spansUnit(minZ, maxZ);
            case Faces.UP -> near(maxY, 1.0F)
                    && spansUnit(minX, maxX) && spansUnit(minZ, maxZ);
            case Faces.NORTH -> near(minZ, 0.0F)
                    && spansUnit(minX, maxX) && spansUnit(minY, maxY);
            case Faces.SOUTH -> near(maxZ, 1.0F)
                    && spansUnit(minX, maxX) && spansUnit(minY, maxY);
            case Faces.WEST -> near(minX, 0.0F)
                    && spansUnit(minZ, maxZ) && spansUnit(minY, maxY);
            case Faces.EAST -> near(maxX, 1.0F)
                    && spansUnit(minZ, maxZ) && spansUnit(minY, maxY);
            default -> false;
        };
    }

    /**
     * Returns true for a full square face on the requested plane, even when the
     * plane is not an exterior cube boundary. This keeps overlay CTM available
     * for lowered full-top models such as farmland, while replacement CTM still
     * requires {@link #isFullBlockExteriorFace(MutableQuadViewImpl, Direction)}.
     */
    private static boolean isFullPlanarFace(MutableQuadViewImpl quad,
                                            Direction direction) {
        float minX = minX(quad);
        float minY = minY(quad);
        float minZ = minZ(quad);
        float maxX = maxX(quad);
        float maxY = maxY(quad);
        float maxZ = maxZ(quad);
        return switch (direction.get3DDataValue()) {
            case Faces.DOWN, Faces.UP -> near(minY, maxY)
                    && spansUnit(minX, maxX) && spansUnit(minZ, maxZ);
            case Faces.NORTH, Faces.SOUTH -> near(minZ, maxZ)
                    && spansUnit(minX, maxX) && spansUnit(minY, maxY);
            case Faces.WEST, Faces.EAST -> near(minX, maxX)
                    && spansUnit(minZ, maxZ) && spansUnit(minY, maxY);
            default -> false;
        };
    }

    private static boolean spansUnit(float min, float max) {
        return near(min, 0.0F) && near(max, 1.0F);
    }

    private static boolean near(float value, float expected) {
        return Math.abs(value - expected) <= FULL_FACE_EPSILON;
    }

    private static float minX(MutableQuadViewImpl quad) {
        float min = Float.POSITIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            min = Math.min(min, quad.getX(i));
        }
        return min;
    }

    private static float maxX(MutableQuadViewImpl quad) {
        float max = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            max = Math.max(max, quad.getX(i));
        }
        return max;
    }

    private static float minY(MutableQuadViewImpl quad) {
        float min = Float.POSITIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            min = Math.min(min, quad.getY(i));
        }
        return min;
    }

    private static float maxY(MutableQuadViewImpl quad) {
        float max = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            max = Math.max(max, quad.getY(i));
        }
        return max;
    }

    private static float minZ(MutableQuadViewImpl quad) {
        float min = Float.POSITIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            min = Math.min(min, quad.getZ(i));
        }
        return min;
    }

    private static float maxZ(MutableQuadViewImpl quad) {
        float max = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            max = Math.max(max, quad.getZ(i));
        }
        return max;
    }
}
