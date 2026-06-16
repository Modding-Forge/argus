package com.argus.config;

import com.argus.resource.OptifinePropertyParsers;
import com.argus.resource.PropertiesFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Parses a {@code .properties}-style configuration file into a
 * {@link ArgusConfig}. The format is intentionally minimal:
 * one key per line, value is a boolean. Unknown keys are
 * ignored; malformed values fall back to the default and the
 * loader does not throw.
 *
 * <p>File format example:
 * <pre>
 * # Argus configuration
 * argus.enabled = true
 * argus.safe_mode = false
 * argus.verify_mode = false
 * argus.ctm.enabled = true
 * argus.ctm.debug_logging = false
 * argus.general.duplicate_translucent_backfaces = false
 * argus.better_grass.mode = fast
 * argus.better_grass.ignore_resource_pack = false
 * argus.better_grass.grass_block = true
 * argus.better_grass.snowy_grass_block = true
 * argus.better_grass.dirt_path = true
 * argus.better_grass.farmland = true
 * argus.better_grass.mycelium = true
 * argus.better_grass.podzol = true
 * argus.better_grass.crimson_nylium = true
 * argus.better_grass.warped_nylium = true
 * argus.cit.enabled = true
 * argus.custom_gui.enabled = true
 * argus.custom_colors.enabled = true
 * argus.custom_sky.enabled = true
 * argus.natural_textures.enabled = true
 * argus.better_snow.enabled = true
 * argus.custom_animations.enabled = true
 * argus.custom_animations.mipmap_distance = 4
 * </pre>
 *
 * <p>Performance: O(file size). Called once at config load and
 * once per reload; not in any hot path.
 */
public final class ArgusConfigLoader {

    private ArgusConfigLoader() {
    }

    /**
     * Parses a configuration from an input stream of UTF-8 text.
     * The stream is consumed but not closed by this method.
     */
    public static ArgusConfig load(InputStream stream) {
        return load(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    /**
     * Parses a configuration from a {@link Reader}. The reader
     * is consumed but not closed by this method.
     */
    public static ArgusConfig load(Reader reader) {
        Objects.requireNonNull(reader, "reader");
        PropertiesFile props;
        try {
            props = PropertiesFile.parse(reader);
        } catch (IOException e) {
            return ArgusConfigDefaults.defaults();
        }
        return fromProperties(props);
    }

    /**
     * Builds a {@link ArgusConfig} from an already-parsed
     * {@link PropertiesFile}. Used by tests and by adapters
     * that have their own property-parsing layer.
     */
    public static ArgusConfig fromProperties(PropertiesFile props) {
        boolean enabled = readBool(props, "argus.enabled",
                ArgusConfigDefaults.ENABLED);
        boolean safeMode = readBool(props, "argus.safe_mode",
                ArgusConfigDefaults.SAFE_MODE);
        boolean verifyMode = readBool(props, "argus.verify_mode",
                ArgusConfigDefaults.VERIFY_MODE);
        boolean ctmEnabled = readBool(props, "argus.ctm.enabled",
                ArgusConfigDefaults.CTM_ENABLED);
        boolean ctmDebugLogging = readBool(props, "argus.ctm.debug_logging",
                ArgusConfigDefaults.CTM_DEBUG_LOGGING);
        boolean duplicateTranslucentBackfaces = readBool(props,
                "argus.general.duplicate_translucent_backfaces",
                ArgusConfigDefaults.DUPLICATE_TRANSLUCENT_BACKFACES);
        BetterGrassMode betterGrassMode = BetterGrassMode.parse(
                props.get("argus.better_grass.mode"),
                ArgusConfigDefaults.BETTER_GRASS_MODE);
        boolean betterGrassIgnoreResourcePack = readBool(props,
                "argus.better_grass.ignore_resource_pack",
                ArgusConfigDefaults.BETTER_GRASS_IGNORE_RESOURCE_PACK);
        boolean betterGrassGrassBlock = readBool(props,
                "argus.better_grass.grass_block",
                ArgusConfigDefaults.BETTER_GRASS_GRASS_BLOCK);
        boolean betterGrassSnowyGrassBlock = readBool(props,
                "argus.better_grass.snowy_grass_block",
                ArgusConfigDefaults.BETTER_GRASS_SNOWY_GRASS_BLOCK);
        boolean betterGrassDirtPath = readBool(props,
                "argus.better_grass.dirt_path",
                ArgusConfigDefaults.BETTER_GRASS_DIRT_PATH);
        boolean betterGrassFarmland = readBool(props,
                "argus.better_grass.farmland",
                ArgusConfigDefaults.BETTER_GRASS_FARMLAND);
        boolean betterGrassMycelium = readBool(props,
                "argus.better_grass.mycelium",
                ArgusConfigDefaults.BETTER_GRASS_MYCELIUM);
        boolean betterGrassPodzol = readBool(props,
                "argus.better_grass.podzol",
                ArgusConfigDefaults.BETTER_GRASS_PODZOL);
        boolean betterGrassCrimsonNylium = readBool(props,
                "argus.better_grass.crimson_nylium",
                ArgusConfigDefaults.BETTER_GRASS_CRIMSON_NYLIUM);
        boolean betterGrassWarpedNylium = readBool(props,
                "argus.better_grass.warped_nylium",
                ArgusConfigDefaults.BETTER_GRASS_WARPED_NYLIUM);
        boolean citEnabled = readBool(props, "argus.cit.enabled",
                ArgusConfigDefaults.CIT_ENABLED);
        boolean customGuiEnabled = readBool(props, "argus.custom_gui.enabled",
                ArgusConfigDefaults.CUSTOM_GUI_ENABLED);
        boolean customColorsEnabled = readBool(props,
                "argus.custom_colors.enabled",
                ArgusConfigDefaults.CUSTOM_COLORS_ENABLED);
        boolean customSkyEnabled = readBool(props,
                "argus.custom_sky.enabled",
                ArgusConfigDefaults.CUSTOM_SKY_ENABLED);
        boolean naturalTexturesEnabled = readBool(props,
                "argus.natural_textures.enabled",
                ArgusConfigDefaults.NATURAL_TEXTURES_ENABLED);
        boolean betterSnowEnabled = readBool(props,
                "argus.better_snow.enabled",
                ArgusConfigDefaults.BETTER_SNOW_ENABLED);
        boolean customAnimationsEnabled = readBool(props,
                "argus.custom_animations.enabled",
                ArgusConfigDefaults.CUSTOM_ANIMATIONS_ENABLED);
        boolean randomEntitiesEnabled = readBool(props,
                "argus.random_entities.enabled",
                ArgusConfigDefaults.RANDOM_ENTITIES_ENABLED);
        boolean entityTexturesEnabled = readBool(props,
                "argus.entity_textures.enabled",
                ArgusConfigDefaults.ENTITY_TEXTURES_ENABLED);
        boolean randomEntityTextures = readBool(props,
                "argus.entity_textures.random_entities",
                randomEntitiesEnabled);
        boolean randomBlockEntityTextures = readBool(props,
                "argus.entity_textures.random_block_entities",
                ArgusConfigDefaults.RANDOM_BLOCK_ENTITY_TEXTURES);
        boolean entityEmissiveTextures = readBool(props,
                "argus.entity_textures.emissive_entities",
                ArgusConfigDefaults.ENTITY_EMISSIVE_TEXTURES);
        boolean blockEntityEmissiveTextures = readBool(props,
                "argus.entity_textures.emissive_block_entities",
                ArgusConfigDefaults.BLOCK_ENTITY_EMISSIVE_TEXTURES);
        boolean entityTextureDebug = readBool(props,
                "argus.entity_textures.debug",
                ArgusConfigDefaults.ENTITY_TEXTURE_DEBUG);
        boolean customEntityModelsEnabled = readBool(props,
                "argus.custom_entity_models.enabled",
                ArgusConfigDefaults.CUSTOM_ENTITY_MODELS_ENABLED);
        int customAnimationMipmapDistance = readInt(props,
                "argus.custom_animations.mipmap_distance",
                ArgusConfigDefaults.CUSTOM_ANIMATION_MIPMAP_DISTANCE,
                0, 4);
        boolean detailsSkyEnabled = readBool(props,
                "argus.details.sky.enabled",
                ArgusConfigDefaults.DETAILS_SKY_ENABLED);
        boolean detailsSunEnabled = readBool(props,
                "argus.details.sun.enabled",
                ArgusConfigDefaults.DETAILS_SUN_ENABLED);
        boolean detailsMoonEnabled = readBool(props,
                "argus.details.moon.enabled",
                ArgusConfigDefaults.DETAILS_MOON_ENABLED);
        boolean detailsStarsEnabled = readBool(props,
                "argus.details.stars.enabled",
                ArgusConfigDefaults.DETAILS_STARS_ENABLED);
        boolean detailsCloudsEnabled = readBool(props,
                "argus.details.clouds.enabled",
                ArgusConfigDefaults.DETAILS_CLOUDS_ENABLED);
        int detailsCloudHeight = readInt(props,
                "argus.details.cloud_height",
                ArgusConfigDefaults.DETAILS_CLOUD_HEIGHT,
                0, 512);
        boolean detailsRainSnowEnabled = readBool(props,
                "argus.details.rain_snow.enabled",
                ArgusConfigDefaults.DETAILS_RAIN_SNOW_ENABLED);
        boolean detailsVignetteEnabled = readBool(props,
                "argus.details.vignette.enabled",
                ArgusConfigDefaults.DETAILS_VIGNETTE_ENABLED);
        boolean animationsEnabled = readBool(props,
                "argus.animations.enabled",
                ArgusConfigDefaults.ANIMATIONS_ENABLED);
        boolean animationWater = readBool(props,
                "argus.animations.water",
                ArgusConfigDefaults.ANIMATION_WATER);
        boolean animationLava = readBool(props,
                "argus.animations.lava",
                ArgusConfigDefaults.ANIMATION_LAVA);
        boolean animationFire = readBool(props,
                "argus.animations.fire",
                ArgusConfigDefaults.ANIMATION_FIRE);
        boolean animationPortal = readBool(props,
                "argus.animations.portal",
                ArgusConfigDefaults.ANIMATION_PORTAL);
        boolean animationSculkSensor = readBool(props,
                "argus.animations.sculk_sensor",
                ArgusConfigDefaults.ANIMATION_SCULK_SENSOR);
        boolean animationBlocks = readBool(props,
                "argus.animations.blocks",
                ArgusConfigDefaults.ANIMATION_BLOCKS);
        boolean particlesEnabled = readBool(props,
                "argus.particles.enabled",
                ArgusConfigDefaults.PARTICLES_ENABLED);
        boolean particleRainSplash = readBool(props,
                "argus.particles.rain_splash",
                ArgusConfigDefaults.PARTICLE_RAIN_SPLASH);
        boolean particleBlockBreak = readBool(props,
                "argus.particles.block_break",
                ArgusConfigDefaults.PARTICLE_BLOCK_BREAK);
        boolean particleBlockBreaking = readBool(props,
                "argus.particles.block_breaking",
                ArgusConfigDefaults.PARTICLE_BLOCK_BREAKING);
        boolean particleExplosion = readBool(props,
                "argus.particles.explosion",
                ArgusConfigDefaults.PARTICLE_EXPLOSION);
        boolean particleWater = readBool(props,
                "argus.particles.water",
                ArgusConfigDefaults.PARTICLE_WATER);
        boolean particleSmoke = readBool(props,
                "argus.particles.smoke",
                ArgusConfigDefaults.PARTICLE_SMOKE);
        boolean particlePotion = readBool(props,
                "argus.particles.potion",
                ArgusConfigDefaults.PARTICLE_POTION);
        boolean particlePortal = readBool(props,
                "argus.particles.portal",
                ArgusConfigDefaults.PARTICLE_PORTAL);
        boolean particleFlame = readBool(props,
                "argus.particles.flame",
                ArgusConfigDefaults.PARTICLE_FLAME);
        boolean particleRedstone = readBool(props,
                "argus.particles.redstone",
                ArgusConfigDefaults.PARTICLE_REDSTONE);
        boolean particleDripping = readBool(props,
                "argus.particles.dripping",
                ArgusConfigDefaults.PARTICLE_DRIPPING);
        boolean particleFirework = readBool(props,
                "argus.particles.firework",
                ArgusConfigDefaults.PARTICLE_FIREWORK);
        boolean fogEnabled = readBool(props,
                "argus.fog.enabled",
                ArgusConfigDefaults.FOG_ENABLED);
        boolean fogWater = readBool(props,
                "argus.fog.water",
                ArgusConfigDefaults.FOG_WATER);
        boolean fogLava = readBool(props,
                "argus.fog.lava",
                ArgusConfigDefaults.FOG_LAVA);
        boolean fogPowderSnow = readBool(props,
                "argus.fog.powder_snow",
                ArgusConfigDefaults.FOG_POWDER_SNOW);
        boolean fogAir = readBool(props,
                "argus.fog.air",
                ArgusConfigDefaults.FOG_AIR);
        boolean entityShadowsEnabled = readBool(props,
                "argus.entities.shadows.enabled",
                ArgusConfigDefaults.ENTITY_SHADOWS_ENABLED);
        boolean entityNameTagsEnabled = readBool(props,
                "argus.entities.name_tags.enabled",
                ArgusConfigDefaults.ENTITY_NAME_TAGS_ENABLED);
        boolean entityPlayerNameTags = readBool(props,
                "argus.entities.player_name_tags",
                ArgusConfigDefaults.ENTITY_PLAYER_NAME_TAGS);
        boolean entityItemFrames = readBool(props,
                "argus.entities.item_frames",
                ArgusConfigDefaults.ENTITY_ITEM_FRAMES);
        boolean entityPaintings = readBool(props,
                "argus.entities.paintings",
                ArgusConfigDefaults.ENTITY_PAINTINGS);
        boolean entityPistonAnimations = readBool(props,
                "argus.entities.piston_animations",
                ArgusConfigDefaults.ENTITY_PISTON_ANIMATIONS);
        boolean entityBeaconBeam = readBool(props,
                "argus.entities.beacon_beam",
                ArgusConfigDefaults.ENTITY_BEACON_BEAM);
        boolean entityLimitBeaconBeamHeight = readBool(props,
                "argus.entities.limit_beacon_beam_height",
                ArgusConfigDefaults.ENTITY_LIMIT_BEACON_BEAM_HEIGHT);
        boolean entityEnchantingTableBook = readBool(props,
                "argus.entities.enchanting_table_book",
                ArgusConfigDefaults.ENTITY_ENCHANTING_TABLE_BOOK);
        boolean showFps = readBool(props,
                "argus.hud.fps",
                ArgusConfigDefaults.SHOW_FPS);
        boolean showFpsExtended = readBool(props,
                "argus.hud.fps_extended",
                ArgusConfigDefaults.SHOW_FPS_EXTENDED);
        boolean showCoords = readBool(props,
                "argus.hud.coords",
                ArgusConfigDefaults.SHOW_COORDS);
        OverlayCorner overlayCorner = OverlayCorner.parse(
                props.get("argus.hud.corner"),
                ArgusConfigDefaults.OVERLAY_CORNER);
        TextContrast textContrast = TextContrast.parse(
                props.get("argus.hud.text_contrast"),
                ArgusConfigDefaults.TEXT_CONTRAST);
        boolean steadyDebugHud = readBool(props,
                "argus.hud.steady_debug",
                ArgusConfigDefaults.STEADY_DEBUG_HUD);
        int steadyDebugHudRefreshInterval = readInt(props,
                "argus.hud.steady_debug_refresh_interval",
                ArgusConfigDefaults.STEADY_DEBUG_HUD_REFRESH_INTERVAL,
                1, 200);
        boolean toastAdvancement = readBool(props,
                "argus.toasts.advancement",
                ArgusConfigDefaults.TOAST_ADVANCEMENT);
        boolean toastRecipe = readBool(props,
                "argus.toasts.recipe",
                ArgusConfigDefaults.TOAST_RECIPE);
        boolean toastSystem = readBool(props,
                "argus.toasts.system",
                ArgusConfigDefaults.TOAST_SYSTEM);
        boolean toastTutorial = readBool(props,
                "argus.toasts.tutorial",
                ArgusConfigDefaults.TOAST_TUTORIAL);
        boolean instantSneak = readBool(props,
                "argus.extras.instant_sneak",
                ArgusConfigDefaults.INSTANT_SNEAK);
        FullscreenMode fullscreenMode = FullscreenMode.parse(
                props.get("argus.extras.fullscreen_mode"),
                ArgusConfigDefaults.FULLSCREEN_MODE);
        boolean biomeColorsEnabled = readBool(props,
                "argus.colors.biome.enabled",
                ArgusConfigDefaults.BIOME_COLORS_ENABLED);
        boolean skyColorsEnabled = readBool(props,
                "argus.colors.sky.enabled",
                ArgusConfigDefaults.SKY_COLORS_ENABLED);
        return new ArgusConfig(enabled, safeMode, verifyMode, ctmEnabled,
                ctmDebugLogging, duplicateTranslucentBackfaces,
                betterGrassMode,
                betterGrassIgnoreResourcePack,
                betterGrassGrassBlock, betterGrassSnowyGrassBlock,
                betterGrassDirtPath,
                betterGrassFarmland, betterGrassMycelium, betterGrassPodzol,
                betterGrassCrimsonNylium, betterGrassWarpedNylium,
                citEnabled, customGuiEnabled, customColorsEnabled,
                customSkyEnabled,
                naturalTexturesEnabled,
                betterSnowEnabled,
                customAnimationsEnabled,
                randomEntityTextures,
                entityTexturesEnabled,
                randomBlockEntityTextures,
                entityEmissiveTextures,
                blockEntityEmissiveTextures,
                entityTextureDebug,
                customEntityModelsEnabled,
                customAnimationMipmapDistance,
                detailsSkyEnabled,
                detailsSunEnabled,
                detailsMoonEnabled,
                detailsStarsEnabled,
                detailsCloudsEnabled,
                detailsCloudHeight,
                detailsRainSnowEnabled,
                detailsVignetteEnabled,
                animationsEnabled,
                animationWater,
                animationLava,
                animationFire,
                animationPortal,
                animationSculkSensor,
                animationBlocks,
                particlesEnabled,
                particleRainSplash,
                particleBlockBreak,
                particleBlockBreaking,
                particleExplosion,
                particleWater,
                particleSmoke,
                particlePotion,
                particlePortal,
                particleFlame,
                particleRedstone,
                particleDripping,
                particleFirework,
                fogEnabled,
                fogWater,
                fogLava,
                fogPowderSnow,
                fogAir,
                entityShadowsEnabled,
                entityNameTagsEnabled,
                entityPlayerNameTags,
                entityItemFrames,
                entityPaintings,
                entityPistonAnimations,
                entityBeaconBeam,
                entityLimitBeaconBeamHeight,
                entityEnchantingTableBook,
                showFps,
                showFpsExtended,
                showCoords,
                overlayCorner,
                textContrast,
                steadyDebugHud,
                steadyDebugHudRefreshInterval,
                toastAdvancement,
                toastRecipe,
                toastSystem,
                toastTutorial,
                instantSneak,
                fullscreenMode,
                biomeColorsEnabled,
                skyColorsEnabled);
    }

    private static boolean readBool(PropertiesFile props, String key, boolean fallback) {
        return OptifinePropertyParsers.parseBoolean(props.get(key),
                fallback);
    }

    private static int readInt(PropertiesFile props,
                               String key,
                               int fallback,
                               int min,
                               int max) {
        return OptifinePropertyParsers.parseIntOrDefault(props.get(key),
                fallback, min, max);
    }
}
