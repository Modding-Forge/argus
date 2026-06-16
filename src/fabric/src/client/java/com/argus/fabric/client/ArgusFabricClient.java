package com.argus.fabric.client;

import com.argus.Constants;
import com.argus.platform.Platforms;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-only entrypoint. Renderer hooks and any client-resource reload
 * listeners will be registered from here in later phases.
 */
public final class ArgusFabricClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID + "/client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] initialized (client, loader={})",
                Constants.MOD_NAME, Platforms.get().id());
        if (net.fabricmc.loader.api.FabricLoader.getInstance()
                .isModLoaded("sodium")) {
            LOGGER.info("[{}] Sodium detected; Sodium CTM mesh path is active "
                            + "and vanilla terrain CTM hooks are disabled",
                    Constants.MOD_NAME);
        }

        ArgusBuiltinResourcePacks.register();

        // Phase 5: load the config file
        // (config/argus.properties) into the shared holder.
        // The path uses the standard Fabric config dir; if the
        // file is missing we silently use the defaults.
        try {
            java.nio.file.Path configDir = net.fabricmc.loader.api.FabricLoader
                    .getInstance().getConfigDir();
            com.argus.fabric.config.FabricConfigLoader
                    .loadAndInstall(configDir);
        } catch (RuntimeException e) {
            LOGGER.warn("[{}] config load failed; using defaults: {}",
                    Constants.MOD_NAME, e.getMessage());
        }

        // Phase 3: register the CTM resource reload listener. The
        // listener walks assets/*/optifine/ctm/*.properties and
        // assets/*/continuity/ctm/*.properties, parses them, and
        // atomically swaps the result into Platforms.get().ctmRegistry().
        com.argus.fabric.ctm.CtmReloadListener.register();
        com.argus.fabric.bettergrass.BetterGrassReloadListener.register();
        com.argus.fabric.emissive.EmissiveReloadListener.register();
        com.argus.fabric.cit.CitReloadListener.register();
        com.argus.fabric.customgui.CustomGuiReloadListener.register();
        com.argus.fabric.animation.CustomAnimationReloadListener.register();
        com.argus.fabric.customcolors.CustomColorsReloadListener.register();
        com.argus.fabric.customsky.CustomSkyReloadListener.register();
        com.argus.fabric.natural.NaturalTexturesReloadListener.register();
        com.argus.fabric.randomentity.RandomEntityReloadListener.register();
        com.argus.fabric.cem.CemReloadListener.register();
        com.argus.client.command.ArgusClientCommands.register();

        // The old CPU quad-swap CTM path is intentionally not
        // installed by default. Argus's production CTM renderer is
        // being moved toward backend-native terrain material data
        // instead of per-quad sprite mutation on section-build threads.
        com.argus.fabric.quad.BlockAtlasTracker.register();

        // Phase 7: tile injection is wired through a
        // custom SpriteSource registered via a Mixin into
        // SpriteSources.bootstrap() and consumed by the
        // vanilla block atlas definition shipped at
        // assets/minecraft/atlases/blocks.json. No
        // listener registration is needed here: the
        // SpriteSource is consulted on every atlas
        // stitch automatically.
    }
}
