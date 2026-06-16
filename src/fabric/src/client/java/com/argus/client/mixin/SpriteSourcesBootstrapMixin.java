package com.argus.client.mixin;

import com.argus.Constants;
import com.argus.fabric.atlas.ArgusEntitySpriteSource;
import com.argus.fabric.atlas.ArgusItemSpriteSource;
import com.argus.fabric.atlas.ArgusPaintingSpriteSource;
import com.argus.fabric.atlas.ArgusTileSpriteSource;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mixin that registers the {@code argus:tiles}
 * SpriteSource codec with Mojang's
 * {@link SpriteSources#bootstrap()} call.
 *
 * <h2>Target</h2>
 *
 * <p>{@link SpriteSources#bootstrap()} - the static
 * initializer that registers all built-in sprite sources
 * with the {@code ID_MAPPER}.
 *
 * <h2>Why a TAIL inject?</h2>
 *
 * <p>Injecting at TAIL runs after the vanilla registrations
 * so we can use the
 * {@link SpriteSourcesAccessor#getIdMapper()} accessor to
 * add the argus entry alongside the built-ins. The
 * alternative (an alternative static block) is harder to
 * order correctly relative to the vanilla {@code bootstrap}.
 *
 * <h2>Risk</h2>
 *
 * <p>Low. The vanilla bootstrap method is a small static
 * initializer; adding one more {@code put} call to its
 * end cannot break the existing registrations. The
 * {@code argus:tiles} id is namespaced to {@code argus}
 * so it cannot collide with any built-in.
 */
@Mixin(SpriteSources.class)
public final class SpriteSourcesBootstrapMixin {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    Constants.MOD_ID + "/sprite-sources-mixin");

    private SpriteSourcesBootstrapMixin() {
    }

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void argus$registerTileSource(CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends SpriteSource>> mapper =
                (ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends SpriteSource>>)
                        (ExtraCodecs.LateBoundIdMapper<?, ?>)
                                SpriteSourcesAccessor.getIdMapper();
        mapper.put(
                ArgusTileSpriteSource.TYPE_ID,
                (MapCodec<? extends SpriteSource>)
                        (MapCodec<?>) ArgusTileSpriteSource.MAP_CODEC);
        mapper.put(
                ArgusItemSpriteSource.TYPE_ID,
                (MapCodec<? extends SpriteSource>)
                        (MapCodec<?>) ArgusItemSpriteSource.MAP_CODEC);
        mapper.put(
                ArgusPaintingSpriteSource.TYPE_ID,
                (MapCodec<? extends SpriteSource>)
                        (MapCodec<?>) ArgusPaintingSpriteSource.MAP_CODEC);
        mapper.put(
                ArgusEntitySpriteSource.TYPE_ID,
                (MapCodec<? extends SpriteSource>)
                        (MapCodec<?>) ArgusEntitySpriteSource.MAP_CODEC);
        LOGGER.info("[{}] registered sprite source type: {}",
                Constants.MOD_NAME, ArgusTileSpriteSource.TYPE_ID);
        LOGGER.info("[{}] registered sprite source type: {}",
                Constants.MOD_NAME, ArgusItemSpriteSource.TYPE_ID);
        LOGGER.info("[{}] registered sprite source type: {}",
                Constants.MOD_NAME, ArgusPaintingSpriteSource.TYPE_ID);
        LOGGER.info("[{}] registered sprite source type: {}",
                Constants.MOD_NAME, ArgusEntitySpriteSource.TYPE_ID);
    }
}
