package com.argus.client.config;

import com.argus.Constants;
import com.argus.config.ArgusConfig;
import com.argus.config.ArgusConfigDefaults;
import com.argus.config.ArgusConfigHolder;
import com.argus.config.ArgusConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Fabric-side config loader. Reads
 * {@code <config-dir>/argus.properties} at startup and on
 * every config reload.
 *
 * <p>The config directory is the Fabric-config standard path
 * (typically {@code .minecraft/config/} for the client). If
 * the file does not exist, the defaults are used and a debug
 * message is logged. Parse errors fall back to the defaults
 * with a warning.
 *
 * <p>This class lives in the {@code client} source set because
 * the config is only consulted by client-side features (the
 * CTM engine, the renderer). The dedicated server does not
 * need it.
 */
public final class ArgusClientConfigLoader {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Constants.MOD_ID + "/config");

    private static final String FILE_NAME = "argus.properties";

    private ArgusClientConfigLoader() {
    }

    /**
     * Loads the config from the given directory and installs it
     * in the {@link ArgusConfigHolder}. Returns the loaded
     * config (also returned by {@link ArgusConfigHolder#get()}
     * after the call).
     */
    public static ArgusConfig loadAndInstall(Path configDir) {
        ArgusConfig cfg = load(configDir);
        ArgusConfigHolder.replace(cfg);
        return cfg;
    }

    /**
     * Loads the config from the given directory. Returns the
     * defaults if the file is missing or malformed.
     */
    public static ArgusConfig load(Path configDir) {
        if (configDir == null) {
            return ArgusConfigDefaults.defaults();
        }
        Path file = configDir.resolve(FILE_NAME);
        if (!Files.isRegularFile(file)) {
            LOGGER.debug("[{}] no {} found, using defaults",
                    Constants.MOD_NAME, file);
            return ArgusConfigDefaults.defaults();
        }
        try (InputStream in = Files.newInputStream(file)) {
            ArgusConfig cfg = ArgusConfigLoader.load(in);
            LOGGER.info("[{}] loaded config from {}: enabled={} "
                        + "ctm={} betterGrass={} cit={} customGui={} "
                        + "customColors={} customSky={} naturalTextures={} "
                        + "betterSnow={} customAnimations={} "
                        + "randomEntities={} cem={} detailsSky={}",
                Constants.MOD_NAME, file, cfg.enabled(), cfg.ctmEnabled(),
                cfg.betterGrassMode(), cfg.citEnabled(),
                cfg.customGuiEnabled(), cfg.customColorsEnabled(),
                cfg.customSkyEnabled(), cfg.naturalTexturesEnabled(),
                cfg.betterSnowEnabled(), cfg.customAnimationsEnabled(),
                cfg.randomEntitiesEnabled(), cfg.customEntityModelsEnabled(),
                cfg.detailsSkyEnabled());
            return cfg;
        } catch (Exception e) {
            LOGGER.warn("[{}] failed to read config from {}; "
                            + "falling back to defaults: {}",
                    Constants.MOD_NAME, file, e.getMessage());
            return ArgusConfigDefaults.defaults();
        }
    }

    /**
     * Writes the given config to the Fabric config directory.
     *
     * <p>Thread expectations: called from client UI/config code,
     * not from the renderer hot path. The active holder is not
     * changed here; callers should install the same immutable
     * snapshot before or after saving.
     */
    public static void save(Path configDir, ArgusConfig config) {
        if (configDir == null || config == null) {
            return;
        }
        Path file = configDir.resolve(FILE_NAME);
        Properties props = new Properties();
        props.setProperty("argus.enabled",
                Boolean.toString(config.enabled()));
        props.setProperty("argus.safe_mode",
                Boolean.toString(config.safeMode()));
        props.setProperty("argus.verify_mode",
                Boolean.toString(config.verifyMode()));
        props.setProperty("argus.ctm.enabled",
                Boolean.toString(config.ctmEnabled()));
        props.setProperty("argus.ctm.debug_logging",
                Boolean.toString(config.ctmDebugLogging()));
        props.setProperty("argus.general.duplicate_translucent_backfaces",
                Boolean.toString(config.duplicateTranslucentBackfaces()));
        props.setProperty("argus.better_grass.mode",
                config.betterGrassMode().name().toLowerCase());
        props.setProperty("argus.better_grass.ignore_resource_pack",
                Boolean.toString(config.betterGrassIgnoreResourcePack()));
        props.setProperty("argus.better_grass.grass_block",
                Boolean.toString(config.betterGrassGrassBlock()));
        props.setProperty("argus.better_grass.snowy_grass_block",
                Boolean.toString(config.betterGrassSnowyGrassBlock()));
        props.setProperty("argus.better_grass.dirt_path",
                Boolean.toString(config.betterGrassDirtPath()));
        props.setProperty("argus.better_grass.farmland",
                Boolean.toString(config.betterGrassFarmland()));
        props.setProperty("argus.better_grass.mycelium",
                Boolean.toString(config.betterGrassMycelium()));
        props.setProperty("argus.better_grass.podzol",
                Boolean.toString(config.betterGrassPodzol()));
        props.setProperty("argus.better_grass.crimson_nylium",
                Boolean.toString(config.betterGrassCrimsonNylium()));
        props.setProperty("argus.better_grass.warped_nylium",
                Boolean.toString(config.betterGrassWarpedNylium()));
        props.setProperty("argus.cit.enabled",
                Boolean.toString(config.citEnabled()));
        props.setProperty("argus.custom_gui.enabled",
                Boolean.toString(config.customGuiEnabled()));
        props.setProperty("argus.custom_colors.enabled",
                Boolean.toString(config.customColorsEnabled()));
        props.setProperty("argus.custom_sky.enabled",
                Boolean.toString(config.customSkyEnabled()));
        props.setProperty("argus.natural_textures.enabled",
                Boolean.toString(config.naturalTexturesEnabled()));
        props.setProperty("argus.better_snow.enabled",
                Boolean.toString(config.betterSnowEnabled()));
        props.setProperty("argus.custom_animations.enabled",
                Boolean.toString(config.customAnimationsEnabled()));
        props.setProperty("argus.random_entities.enabled",
                Boolean.toString(config.randomEntitiesEnabled()));
        props.setProperty("argus.entity_textures.enabled",
                Boolean.toString(config.entityTexturesEnabled()));
        props.setProperty("argus.entity_textures.random_entities",
                Boolean.toString(config.randomEntitiesEnabled()));
        props.setProperty("argus.entity_textures.random_block_entities",
                Boolean.toString(config.randomBlockEntityTextures()));
        props.setProperty("argus.entity_textures.emissive_entities",
                Boolean.toString(config.entityEmissiveTextures()));
        props.setProperty("argus.entity_textures.emissive_block_entities",
                Boolean.toString(config.blockEntityEmissiveTextures()));
        props.setProperty("argus.entity_textures.debug",
                Boolean.toString(config.entityTextureDebug()));
        props.setProperty("argus.custom_entity_models.enabled",
                Boolean.toString(config.customEntityModelsEnabled()));
        props.setProperty("argus.custom_animations.mipmap_distance",
                Integer.toString(config.customAnimationMipmapDistance()));
        props.setProperty("argus.details.sky.enabled",
                Boolean.toString(config.detailsSkyEnabled()));
        props.setProperty("argus.details.sun.enabled",
                Boolean.toString(config.detailsSunEnabled()));
        props.setProperty("argus.details.moon.enabled",
                Boolean.toString(config.detailsMoonEnabled()));
        props.setProperty("argus.details.stars.enabled",
                Boolean.toString(config.detailsStarsEnabled()));
        props.setProperty("argus.details.clouds.enabled",
                Boolean.toString(config.detailsCloudsEnabled()));
        props.setProperty("argus.details.cloud_height",
                Integer.toString(config.detailsCloudHeight()));
        props.setProperty("argus.details.rain_snow.enabled",
                Boolean.toString(config.detailsRainSnowEnabled()));
        props.setProperty("argus.details.vignette.enabled",
                Boolean.toString(config.detailsVignetteEnabled()));
        props.setProperty("argus.animations.enabled",
                Boolean.toString(config.animationsEnabled()));
        props.setProperty("argus.animations.water",
                Boolean.toString(config.animationWater()));
        props.setProperty("argus.animations.lava",
                Boolean.toString(config.animationLava()));
        props.setProperty("argus.animations.fire",
                Boolean.toString(config.animationFire()));
        props.setProperty("argus.animations.portal",
                Boolean.toString(config.animationPortal()));
        props.setProperty("argus.animations.sculk_sensor",
                Boolean.toString(config.animationSculkSensor()));
        props.setProperty("argus.animations.blocks",
                Boolean.toString(config.animationBlocks()));
        props.setProperty("argus.particles.enabled",
                Boolean.toString(config.particlesEnabled()));
        props.setProperty("argus.particles.rain_splash",
                Boolean.toString(config.particleRainSplash()));
        props.setProperty("argus.particles.block_break",
                Boolean.toString(config.particleBlockBreak()));
        props.setProperty("argus.particles.block_breaking",
                Boolean.toString(config.particleBlockBreaking()));
        props.setProperty("argus.particles.explosion",
                Boolean.toString(config.particleExplosion()));
        props.setProperty("argus.particles.water",
                Boolean.toString(config.particleWater()));
        props.setProperty("argus.particles.smoke",
                Boolean.toString(config.particleSmoke()));
        props.setProperty("argus.particles.potion",
                Boolean.toString(config.particlePotion()));
        props.setProperty("argus.particles.portal",
                Boolean.toString(config.particlePortal()));
        props.setProperty("argus.particles.flame",
                Boolean.toString(config.particleFlame()));
        props.setProperty("argus.particles.redstone",
                Boolean.toString(config.particleRedstone()));
        props.setProperty("argus.particles.dripping",
                Boolean.toString(config.particleDripping()));
        props.setProperty("argus.particles.firework",
                Boolean.toString(config.particleFirework()));
        props.setProperty("argus.fog.enabled",
                Boolean.toString(config.fogEnabled()));
        props.setProperty("argus.fog.water",
                Boolean.toString(config.fogWater()));
        props.setProperty("argus.fog.lava",
                Boolean.toString(config.fogLava()));
        props.setProperty("argus.fog.powder_snow",
                Boolean.toString(config.fogPowderSnow()));
        props.setProperty("argus.fog.air",
                Boolean.toString(config.fogAir()));
        props.setProperty("argus.entities.shadows.enabled",
                Boolean.toString(config.entityShadowsEnabled()));
        props.setProperty("argus.entities.name_tags.enabled",
                Boolean.toString(config.entityNameTagsEnabled()));
        props.setProperty("argus.entities.player_name_tags",
                Boolean.toString(config.entityPlayerNameTags()));
        props.setProperty("argus.entities.item_frames",
                Boolean.toString(config.entityItemFrames()));
        props.setProperty("argus.entities.paintings",
                Boolean.toString(config.entityPaintings()));
        props.setProperty("argus.entities.piston_animations",
                Boolean.toString(config.entityPistonAnimations()));
        props.setProperty("argus.entities.beacon_beam",
                Boolean.toString(config.entityBeaconBeam()));
        props.setProperty("argus.entities.limit_beacon_beam_height",
                Boolean.toString(config.entityLimitBeaconBeamHeight()));
        props.setProperty("argus.entities.enchanting_table_book",
                Boolean.toString(config.entityEnchantingTableBook()));
        props.setProperty("argus.hud.fps",
                Boolean.toString(config.showFps()));
        props.setProperty("argus.hud.fps_extended",
                Boolean.toString(config.showFpsExtended()));
        props.setProperty("argus.hud.coords",
                Boolean.toString(config.showCoords()));
        props.setProperty("argus.hud.corner",
                config.overlayCorner().name().toLowerCase());
        props.setProperty("argus.hud.text_contrast",
                config.textContrast().name().toLowerCase());
        props.setProperty("argus.hud.steady_debug",
                Boolean.toString(config.steadyDebugHud()));
        props.setProperty("argus.hud.steady_debug_refresh_interval",
                Integer.toString(config.steadyDebugHudRefreshInterval()));
        props.setProperty("argus.toasts.advancement",
                Boolean.toString(config.toastAdvancement()));
        props.setProperty("argus.toasts.recipe",
                Boolean.toString(config.toastRecipe()));
        props.setProperty("argus.toasts.system",
                Boolean.toString(config.toastSystem()));
        props.setProperty("argus.toasts.tutorial",
                Boolean.toString(config.toastTutorial()));
        props.setProperty("argus.extras.instant_sneak",
                Boolean.toString(config.instantSneak()));
        props.setProperty("argus.extras.fullscreen_mode",
                config.fullscreenMode().name().toLowerCase());
        props.setProperty("argus.colors.biome.enabled",
                Boolean.toString(config.biomeColorsEnabled()));
        props.setProperty("argus.colors.sky.enabled",
                Boolean.toString(config.skyColorsEnabled()));
        try {
            Files.createDirectories(configDir);
            try (OutputStream out = Files.newOutputStream(file)) {
                props.store(out, "Argus configuration");
            }
            LOGGER.info("[{}] saved config to {}", Constants.MOD_NAME, file);
        } catch (Exception e) {
            LOGGER.warn("[{}] failed to save config to {}: {}",
                    Constants.MOD_NAME, file, e.getMessage());
        }
    }
}
