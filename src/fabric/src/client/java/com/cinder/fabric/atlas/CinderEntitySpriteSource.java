package com.cinder.fabric.atlas;

import com.cinder.Constants;
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
 * Generic atlas sprite source for OptiFine Random Entity and Random Tile
 * Entity variant textures.
 *
 * <p>Purpose: chest and shulker block-entity renderers select atlas sprites,
 * not standalone entity textures. Resource-pack variants under
 * {@code optifine/random/entity} therefore need to be stitched into the
 * relevant Mojang atlases before runtime remapping can work.
 *
 * <p>Threading: reload-time only.
 *
 * <p>Performance: reload path only; runtime selection never touches the
 * resource manager.
 */
public record CinderEntitySpriteSource() implements SpriteSource {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(Constants.MOD_ID + "/entity-source");

    public static final Identifier TYPE_ID =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,
                    "random_entity_sprites");

    public static final MapCodec<CinderEntitySpriteSource> MAP_CODEC =
            MapCodec.unit(CinderEntitySpriteSource::new);

    private static final String OPTIFINE_RANDOM_ENTITY =
            "optifine/random/entity";

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        Map<Identifier, Resource> resources = resourceManager.listResources(
                OPTIFINE_RANDOM_ENTITY,
                id -> id.getPath().endsWith(".png"));
        int added = 0;
        for (Identifier id : resources.keySet()) {
            Optional<Resource> resource = resourceManager.getResource(id);
            if (resource.isEmpty()) {
                continue;
            }
            output.add(spriteId(id), resource.get());
            Identifier alias = atlasAlias(id);
            if (alias != null) {
                output.add(alias, resource.get());
            }
            added++;
        }
        if (added > 0) {
            LOGGER.info("[{}] injected {} Random Entity sprites into atlas",
                    Constants.MOD_NAME, added);
        }
    }

    @Override
    public MapCodec<CinderEntitySpriteSource> codec() {
        return MAP_CODEC;
    }

    private static Identifier spriteId(Identifier texture) {
        String path = texture.getPath();
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - 4);
        }
        return Identifier.fromNamespaceAndPath(texture.getNamespace(), path);
    }

    private static Identifier atlasAlias(Identifier texture) {
        String path = texture.getPath();
        if (path.startsWith("optifine/random/entity/")) {
            path = "entity/" + path.substring(
                    "optifine/random/entity/".length());
        } else if (path.startsWith("optifine/mob/")) {
            path = "entity/" + path.substring("optifine/mob/".length());
        } else {
            return null;
        }
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - 4);
        }
        return Identifier.fromNamespaceAndPath(texture.getNamespace(), path);
    }
}
