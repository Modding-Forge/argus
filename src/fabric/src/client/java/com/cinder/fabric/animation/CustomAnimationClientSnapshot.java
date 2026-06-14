package com.cinder.fabric.animation;

import com.cinder.animation.CustomAnimationFrame;
import com.cinder.animation.CustomAnimationRule;
import com.cinder.animation.CustomAnimationRuleSet;
import com.cinder.resource.NamespaceId;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Fabric runtime snapshot for custom texture animations.
 *
 * <p>Purpose: owns decoded source images and compact runtime rule data. The
 * snapshot is atomically replaced on reload and closed when superseded.
 *
 * <p>Threading: created on the reload prepare executor, ticked on the client
 * thread only after publication.
 */
public final class CustomAnimationClientSnapshot implements AutoCloseable {

    private static final RuntimeRule[] NO_RULES = new RuntimeRule[0];

    private final RuntimeRule[] rules;
    private final Set<NamespaceId> targetSprites;

    private CustomAnimationClientSnapshot(RuntimeRule[] rules,
                                          Set<NamespaceId> targetSprites) {
        this.rules = rules;
        this.targetSprites = targetSprites;
    }

    public static CustomAnimationClientSnapshot empty() {
        return new CustomAnimationClientSnapshot(NO_RULES, Set.of());
    }

    public static CustomAnimationClientSnapshot from(
            CustomAnimationRuleSet ruleSet,
            ResourceManager resourceManager,
            Logger logger) {
        if (ruleSet == null || ruleSet.isEmpty()) {
            return empty();
        }
        ArrayList<RuntimeRule> rules = new ArrayList<>();
        for (CustomAnimationRule rule : ruleSet.all()) {
            RuntimeRule runtimeRule = build(rule, resourceManager, logger);
            if (runtimeRule != null) {
                rules.add(runtimeRule);
            }
        }
        if (rules.isEmpty()) {
            return empty();
        }
        RuntimeRule[] runtimeRules = rules.toArray(RuntimeRule[]::new);
        HashSet<NamespaceId> targets = new HashSet<>();
        for (RuntimeRule rule : runtimeRules) {
            targets.add(rule.targetSprite());
        }
        return new CustomAnimationClientSnapshot(runtimeRules,
                Set.copyOf(targets));
    }

    public boolean isEmpty() {
        return rules.length == 0;
    }

    public int size() {
        return rules.length;
    }

    RuntimeRule[] rules() {
        return rules;
    }

    public boolean animatesSprite(NamespaceId sprite) {
        return targetSprites.contains(sprite);
    }

    @Override
    public void close() {
        for (RuntimeRule rule : rules) {
            for (NativeImage[] images : rule.frameMipImages()) {
                for (NativeImage image : images) {
                    image.close();
                }
            }
        }
    }

    private static @Nullable RuntimeRule build(
            CustomAnimationRule rule,
            ResourceManager resourceManager,
            Logger logger) {
        Identifier sourceId = id(rule.fromTexture());
        Optional<Resource> sourceResource = resourceManager.getResource(
                sourceId);
        if (sourceResource.isEmpty()) {
            logger.warn("[Cinder] skipping custom animation {}: missing {}",
                    rule.sourceFile(), sourceId);
            return null;
        }
        NativeImage image;
        try (var in = sourceResource.get().open()) {
            image = NativeImage.read(in);
        } catch (Exception e) {
            logger.warn("[Cinder] skipping custom animation {}: failed to "
                            + "read {}: {}", rule.sourceFile(), sourceId,
                    e.getMessage());
            return null;
        }
        int width = rule.width() > 0 ? rule.width() : image.getWidth();
        int height = rule.height() > 0 ? rule.height() : image.getHeight();
        if (width <= 0 || height <= 0 || width > image.getWidth()
                || height > image.getHeight()) {
            image.close();
            logger.warn("[Cinder] skipping custom animation {}: invalid "
                            + "frame size {}x{} for source {}x{}",
                    rule.sourceFile(), width, height, image.getWidth(),
                    image.getHeight());
            return null;
        }
        int columns = image.getWidth() / width;
        int rows = image.getHeight() / height;
        int frameCapacity = columns * rows;
        if (columns <= 0 || rows <= 0 || frameCapacity <= 0) {
            image.close();
            logger.warn("[Cinder] skipping custom animation {}: source does "
                    + "not contain complete frames", rule.sourceFile());
            return null;
        }
        int[] tiles;
        int[] durations;
        if (rule.frames().isEmpty()) {
            tiles = new int[frameCapacity];
            durations = new int[frameCapacity];
            for (int i = 0; i < frameCapacity; i++) {
                tiles[i] = i;
                durations[i] = rule.defaultDuration();
            }
        } else {
            tiles = new int[rule.frames().size()];
            durations = new int[rule.frames().size()];
            for (int i = 0; i < rule.frames().size(); i++) {
                CustomAnimationFrame frame = rule.frames().get(i);
                if (frame.tileIndex() >= frameCapacity) {
                    image.close();
                    logger.warn("[Cinder] skipping custom animation {}: "
                                    + "tile {} outside {} available frames",
                            rule.sourceFile(), frame.tileIndex(),
                            frameCapacity);
                    return null;
                }
                tiles[i] = frame.tileIndex();
                durations[i] = frame.duration();
            }
        }
        NamespaceId targetSprite = targetSpriteId(rule.toTexture());
        if (targetSprite == null) {
            image.close();
            logger.warn("[Cinder] skipping custom animation {}: only "
                            + "textures/.../*.png atlas targets are supported "
                            + "in the MVP ({})",
                    rule.sourceFile(), rule.toTexture());
            return null;
        }
        NativeImage[][] frameMipImages = cropFrames(image, width, height, columns,
                tiles);
        image.close();
        return new RuntimeRule(rule.sourceFile(), frameMipImages, targetSprite,
                rule.x(), rule.y(), width, height, tiles, durations);
    }

    private static NativeImage[][] cropFrames(NativeImage source,
                                              int width,
                                              int height,
                                              int columns,
                                              int[] tiles) {
        NativeImage[][] out = new NativeImage[tiles.length][];
        int mipLevels = mipLevels(width, height);
        for (int i = 0; i < tiles.length; i++) {
            NativeImage[] levels = new NativeImage[mipLevels];
            levels[0] = new NativeImage(
                    NativeImage.Format.RGBA, width, height, true);
            int tile = tiles[i];
            int sourceX = (tile % columns) * width;
            int sourceY = (tile / columns) * height;
            source.copyRect(levels[0], sourceX, sourceY, 0, 0, width, height,
                    false, false);
            for (int level = 1; level < mipLevels; level++) {
                int levelWidth = Math.max(1, width >> level);
                int levelHeight = Math.max(1, height >> level);
                levels[level] = new NativeImage(NativeImage.Format.RGBA,
                        levelWidth, levelHeight, true);
                levels[0].resizeSubRectTo(0, 0, width, height,
                        levels[level]);
            }
            out[i] = levels;
        }
        return out;
    }

    private static int mipLevels(int width, int height) {
        int levels = 1;
        int w = width;
        int h = height;
        while (levels < 5 && (w > 1 || h > 1)) {
            w = Math.max(1, w >> 1);
            h = Math.max(1, h >> 1);
            levels++;
        }
        return levels;
    }

    private static Identifier id(NamespaceId id) {
        return Identifier.fromNamespaceAndPath(id.namespace(), id.path());
    }

    private static @Nullable NamespaceId targetSpriteId(NamespaceId texture) {
        String path = texture.path();
        if (!path.startsWith("textures/") || !path.endsWith(".png")) {
            return null;
        }
        String sprite = path.substring("textures/".length(),
                path.length() - 4);
        if (sprite.isEmpty()) {
            return null;
        }
        return new NamespaceId(texture.namespace(), sprite);
    }

    /**
     * Runtime-ready custom animation rule.
     */
    record RuntimeRule(
            String sourceFile,
            NativeImage[][] frameMipImages,
            NamespaceId targetSprite,
            int x,
            int y,
            int width,
            int height,
            int[] tiles,
            int[] durations) {

        Identifier targetIdentifier() {
            return Identifier.fromNamespaceAndPath(
                    targetSprite.namespace(), targetSprite.path());
        }

        boolean targetPresent(TextureAtlas atlas) {
            TextureAtlasSprite sprite = atlas.getSprite(targetIdentifier());
            return sprite != null
                    && sprite.contents().name().equals(targetIdentifier());
        }
    }
}
