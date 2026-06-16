package com.argus.client.atlas;

import com.argus.Constants;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 * Painting-atlas sprite source for OptiFine Random Painting variants.
 *
 * <p>Purpose: OptiFine-style random painting textures live under
 * {@code optifine/random/painting}, outside the vanilla painting atlas source
 * folders. This source injects those PNGs into Mojang's painting atlas so the
 * runtime can select them by sprite id.
 *
 * <p>Threading: reload-time only, called by Mojang's atlas loader.
 *
 * <p>Performance: reload path only. Runtime painting selection performs no
 * resource lookups.
 */
public record ArgusPaintingSpriteSource() implements SpriteSource {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(Constants.MOD_ID + "/painting-source");

    public static final Identifier TYPE_ID =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,
                    "random_paintings");

    public static final MapCodec<ArgusPaintingSpriteSource> MAP_CODEC =
            MapCodec.unit(ArgusPaintingSpriteSource::new);

    private static final String OPTIFINE_RANDOM_PAINTING =
            "optifine/random/painting";

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        Map<Identifier, Resource> resources = resourceManager.listResources(
                OPTIFINE_RANDOM_PAINTING,
                id -> id.getPath().endsWith(".png"));
        int added = 0;
        for (Identifier id : resources.keySet()) {
            Optional<Resource> resource = resourceManager.getResource(id);
            if (resource.isEmpty()) {
                continue;
            }
            output.add(spriteId(id), resource.get());
            added++;
        }
        if (added > 0) {
            LOGGER.info("[{}] injected {} Random Painting sprites into the "
                    + "painting atlas", Constants.MOD_NAME, added);
        }
    }

    @Override
    public MapCodec<ArgusPaintingSpriteSource> codec() {
        return MAP_CODEC;
    }

    private static Identifier spriteId(Identifier texture) {
        String path = texture.getPath();
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - 4);
        }
        return Identifier.fromNamespaceAndPath(texture.getNamespace(), path);
    }
}
