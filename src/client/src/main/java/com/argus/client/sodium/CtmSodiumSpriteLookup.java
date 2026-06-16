package com.argus.client.sodium;

import com.argus.resource.NamespaceId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Resolves Argus CTM material sprite ids against Minecraft's live block atlas.
 *
 * <p>Sodium still renders terrain from the stitched block atlas, so the
 * Sodium-native CTM mesh path can use the same generated and explicit sprites
 * that the previous atlas-backed renderer path injected during resource reload.
 *
 * <h2>Threading</h2>
 *
 * <p>This class deliberately depends only on Minecraft client classes, Sodium
 * callers, and Argus shared metadata. It has no Fabric imports so the same
 * logic can move to another loader's client source set later.
 *
 * <h2>Performance</h2>
 *
 * <p>Performance: HOT PATH. Allocation policy: one Minecraft identifier per
 * unique CTM sprite and block-atlas generation. Sodium can rebuild sections
 * frequently after a block update, and loader implementations may create fresh
 * renderer contexts, so atlas results are cached process-wide and cleared when
 * the live block atlas instance changes.
 */
public final class CtmSodiumSpriteLookup {

    private static final Object CACHE_LOCK = new Object();
    private static final ConcurrentMap<NamespaceId, TextureAtlasSprite> SPRITES =
            new ConcurrentHashMap<>();
    private static final Set<NamespaceId> MISSING_SPRITES =
            ConcurrentHashMap.newKeySet();

    private static volatile @Nullable TextureAtlas atlas;

    /**
     * Looks up a CTM sprite in the current block atlas.
     */
    public @Nullable TextureAtlasSprite sprite(NamespaceId spriteId) {
        if (spriteId == null) {
            return null;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getAtlasManager() == null) {
            return null;
        }
        TextureAtlas atlas;
        try {
            atlas = minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return null;
        }
        if (atlas == null) {
            return null;
        }
        ensureCurrentAtlas(atlas);
        TextureAtlasSprite cached = SPRITES.get(spriteId);
        if (cached != null) {
            return cached;
        }
        if (MISSING_SPRITES.contains(spriteId)) {
            return null;
        }
        Identifier id = Identifier.fromNamespaceAndPath(
                spriteId.namespace(), spriteId.path());
        TextureAtlasSprite sprite = atlas.getSprite(id);
        if (sprite == null
                || MissingTextureAtlasSprite.getLocation()
                .equals(sprite.contents().name())) {
            MISSING_SPRITES.add(spriteId);
            return null;
        }
        TextureAtlasSprite previous = SPRITES.putIfAbsent(spriteId, sprite);
        if (previous != null) {
            return previous;
        }
        return sprite;
    }

    private static void ensureCurrentAtlas(TextureAtlas currentAtlas) {
        if (atlas == currentAtlas) {
            return;
        }
        synchronized (CACHE_LOCK) {
            if (atlas == currentAtlas) {
                return;
            }
            atlas = currentAtlas;
            SPRITES.clear();
            MISSING_SPRITES.clear();
        }
    }
}
