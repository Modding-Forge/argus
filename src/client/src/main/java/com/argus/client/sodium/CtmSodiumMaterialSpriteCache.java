package com.argus.client.sodium;

import com.argus.ctm.CtmMaterialEntry;
import com.argus.ctm.CtmMaterialTable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;

/**
 * Per-processor cache for CTM material-to-Sodium-sprite realization.
 *
 * <p>Purpose: section builds often resolve the same CTM material many times
 * while emitting a chunk. The shared material table already maps
 * {@code (rule,tile)} to {@link CtmMaterialEntry}; this client-side cache
 * avoids repeating the atlas sprite lookup for the same immutable material
 * entry.
 *
 * <p>Threading: owned by one {@link CtmSodiumQuadProcessor}. It is not shared
 * between Sodium renderer workers.
 *
 * <p>Performance: HOT PATH. Allocation policy: one lazy
 * {@link IdentityHashMap} per processor after the first CTM hit. The map is
 * cleared when the immutable material table snapshot changes.
 */
final class CtmSodiumMaterialSpriteCache {

    private final CtmSodiumSpriteLookup spriteLookup;
    private final IdentityHashMap<CtmMaterialEntry, TextureAtlasSprite> sprites =
            new IdentityHashMap<>();

    private @Nullable CtmMaterialTable table;

    CtmSodiumMaterialSpriteCache(CtmSodiumSpriteLookup spriteLookup) {
        this.spriteLookup = spriteLookup;
    }

    @Nullable TextureAtlasSprite sprite(CtmMaterialTable currentTable,
                                        CtmMaterialEntry material) {
        if (table != currentTable) {
            table = currentTable;
            sprites.clear();
        }
        TextureAtlasSprite cached = sprites.get(material);
        if (cached != null) {
            return cached;
        }
        TextureAtlasSprite resolved = spriteLookup.sprite(material.sprite());
        if (resolved != null) {
            sprites.put(material, resolved);
        }
        return resolved;
    }
}
