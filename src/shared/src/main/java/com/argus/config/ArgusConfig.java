package com.argus.config;

import java.util.Objects;

/**
 * Immutable configuration of the Argus mod. The
 * configuration is a flat record of booleans; new toggles are
 * added by extending the record and the corresponding default
 * constant in {@link ArgusConfigDefaults}.
 *
 * <p>The class is a {@code record} so that instances are
 * automatically immutable, value-equal, and easy to log. Tests
 * should use the public accessors only.
 *
 * <h2>Why a flat boolean record?</h2>
 *
 * <p>The current Phase 4 scope is small enough that a single
 * record of booleans is the simplest representation. When the
 * number of toggles grows (Tier 2-4 features), the structure
 * will be split per feature (CTM, Natural Textures, etc.) but
 * stay immutable.
 *
 * <p>Performance: a {@code ArgusConfig} is built once at
 * config-load time and read on every selection. The record's
 * accessors are inlined by the JIT.
 */
public record ArgusConfig(
        boolean enabled,
        boolean safeMode,
        boolean verifyMode,
        boolean ctmEnabled,
        boolean ctmDebugLogging,
        boolean duplicateTranslucentBackfaces,
        BetterGrassMode betterGrassMode,
        boolean betterGrassIgnoreResourcePack,
        boolean betterGrassGrassBlock,
        boolean betterGrassSnowyGrassBlock,
        boolean betterGrassDirtPath,
        boolean betterGrassFarmland,
        boolean betterGrassMycelium,
        boolean betterGrassPodzol,
        boolean betterGrassCrimsonNylium,
        boolean betterGrassWarpedNylium,
        boolean citEnabled,
        boolean customGuiEnabled,
        boolean customColorsEnabled,
        boolean customSkyEnabled,
        boolean naturalTexturesEnabled,
        boolean betterSnowEnabled,
        boolean customAnimationsEnabled,
        boolean randomEntitiesEnabled,
        boolean entityTexturesEnabled,
        boolean randomBlockEntityTextures,
        boolean entityEmissiveTextures,
        boolean blockEntityEmissiveTextures,
        boolean entityTextureDebug,
        boolean customEntityModelsEnabled,
        int customAnimationMipmapDistance,
        boolean detailsSkyEnabled,
        boolean detailsSunEnabled,
        boolean detailsMoonEnabled,
        boolean detailsStarsEnabled,
        boolean detailsCloudsEnabled,
        int detailsCloudHeight,
        boolean detailsRainSnowEnabled,
        boolean detailsVignetteEnabled,
        boolean animationsEnabled,
        boolean animationWater,
        boolean animationLava,
        boolean animationFire,
        boolean animationPortal,
        boolean animationSculkSensor,
        boolean animationBlocks,
        boolean particlesEnabled,
        boolean particleRainSplash,
        boolean particleBlockBreak,
        boolean particleBlockBreaking,
        boolean particleExplosion,
        boolean particleWater,
        boolean particleSmoke,
        boolean particlePotion,
        boolean particlePortal,
        boolean particleFlame,
        boolean particleRedstone,
        boolean particleDripping,
        boolean particleFirework,
        boolean fogEnabled,
        boolean fogWater,
        boolean fogLava,
        boolean fogPowderSnow,
        boolean fogAir,
        boolean entityShadowsEnabled,
        boolean entityNameTagsEnabled,
        boolean entityPlayerNameTags,
        boolean entityItemFrames,
        boolean entityPaintings,
        boolean entityPistonAnimations,
        boolean entityBeaconBeam,
        boolean entityLimitBeaconBeamHeight,
        boolean entityEnchantingTableBook,
        boolean showFps,
        boolean showFpsExtended,
        boolean showCoords,
        OverlayCorner overlayCorner,
        TextContrast textContrast,
        boolean steadyDebugHud,
        int steadyDebugHudRefreshInterval,
        boolean toastAdvancement,
        boolean toastRecipe,
        boolean toastSystem,
        boolean toastTutorial,
        boolean instantSneak,
        FullscreenMode fullscreenMode,
        boolean biomeColorsEnabled,
        boolean skyColorsEnabled) {

    public ArgusConfig {
        // Defensive copies not needed: record components are
        // already final. We only validate that the booleans are
        // non-null (records reject nulls by default, but the
        // explicit check is documentation).
        Objects.requireNonNull(enabled, "enabled");
        Objects.requireNonNull(safeMode, "safeMode");
        Objects.requireNonNull(verifyMode, "verifyMode");
        Objects.requireNonNull(ctmEnabled, "ctmEnabled");
        Objects.requireNonNull(ctmDebugLogging, "ctmDebugLogging");
        Objects.requireNonNull(duplicateTranslucentBackfaces,
                "duplicateTranslucentBackfaces");
        Objects.requireNonNull(betterGrassMode, "betterGrassMode");
        Objects.requireNonNull(betterGrassIgnoreResourcePack,
                "betterGrassIgnoreResourcePack");
        Objects.requireNonNull(betterGrassGrassBlock, "betterGrassGrassBlock");
        Objects.requireNonNull(betterGrassSnowyGrassBlock,
                "betterGrassSnowyGrassBlock");
        Objects.requireNonNull(betterGrassDirtPath, "betterGrassDirtPath");
        Objects.requireNonNull(betterGrassFarmland, "betterGrassFarmland");
        Objects.requireNonNull(betterGrassMycelium, "betterGrassMycelium");
        Objects.requireNonNull(betterGrassPodzol, "betterGrassPodzol");
        Objects.requireNonNull(betterGrassCrimsonNylium,
                "betterGrassCrimsonNylium");
        Objects.requireNonNull(betterGrassWarpedNylium,
                "betterGrassWarpedNylium");
        Objects.requireNonNull(citEnabled, "citEnabled");
        Objects.requireNonNull(customGuiEnabled, "customGuiEnabled");
        Objects.requireNonNull(customColorsEnabled, "customColorsEnabled");
        Objects.requireNonNull(customSkyEnabled, "customSkyEnabled");
        Objects.requireNonNull(naturalTexturesEnabled,
                "naturalTexturesEnabled");
        Objects.requireNonNull(betterSnowEnabled, "betterSnowEnabled");
        Objects.requireNonNull(customAnimationsEnabled,
                "customAnimationsEnabled");
        Objects.requireNonNull(randomEntitiesEnabled,
                "randomEntitiesEnabled");
        Objects.requireNonNull(entityTexturesEnabled,
                "entityTexturesEnabled");
        Objects.requireNonNull(randomBlockEntityTextures,
                "randomBlockEntityTextures");
        Objects.requireNonNull(entityEmissiveTextures,
                "entityEmissiveTextures");
        Objects.requireNonNull(blockEntityEmissiveTextures,
                "blockEntityEmissiveTextures");
        Objects.requireNonNull(entityTextureDebug,
                "entityTextureDebug");
        Objects.requireNonNull(customEntityModelsEnabled,
                "customEntityModelsEnabled");
        if (customAnimationMipmapDistance < 0
                || customAnimationMipmapDistance > 4) {
            throw new IllegalArgumentException(
                    "customAnimationMipmapDistance must be 0-4");
        }
        if (detailsCloudHeight < 0 || detailsCloudHeight > 512) {
            throw new IllegalArgumentException(
                    "detailsCloudHeight must be 0-512");
        }
        Objects.requireNonNull(overlayCorner, "overlayCorner");
        Objects.requireNonNull(textContrast, "textContrast");
        Objects.requireNonNull(fullscreenMode, "fullscreenMode");
        if (steadyDebugHudRefreshInterval < 1
                || steadyDebugHudRefreshInterval > 200) {
            throw new IllegalArgumentException(
                    "steadyDebugHudRefreshInterval must be 1-200");
        }
    }

    public ArgusConfig(boolean enabled,
                        boolean safeMode,
                        boolean verifyMode,
                        boolean ctmEnabled,
                        boolean ctmDebugLogging,
                        BetterGrassMode betterGrassMode) {
        this(enabled, safeMode, verifyMode, ctmEnabled, ctmDebugLogging,
                ArgusConfigDefaults.DUPLICATE_TRANSLUCENT_BACKFACES,
                betterGrassMode,
                ArgusConfigDefaults.BETTER_GRASS_IGNORE_RESOURCE_PACK,
                ArgusConfigDefaults.BETTER_GRASS_GRASS_BLOCK,
                ArgusConfigDefaults.BETTER_GRASS_SNOWY_GRASS_BLOCK,
                ArgusConfigDefaults.BETTER_GRASS_DIRT_PATH,
                ArgusConfigDefaults.BETTER_GRASS_FARMLAND,
                ArgusConfigDefaults.BETTER_GRASS_MYCELIUM,
                ArgusConfigDefaults.BETTER_GRASS_PODZOL,
                ArgusConfigDefaults.BETTER_GRASS_CRIMSON_NYLIUM,
                ArgusConfigDefaults.BETTER_GRASS_WARPED_NYLIUM,
                ArgusConfigDefaults.CIT_ENABLED,
                ArgusConfigDefaults.CUSTOM_GUI_ENABLED,
                ArgusConfigDefaults.CUSTOM_COLORS_ENABLED,
                ArgusConfigDefaults.CUSTOM_SKY_ENABLED,
                ArgusConfigDefaults.NATURAL_TEXTURES_ENABLED,
                ArgusConfigDefaults.BETTER_SNOW_ENABLED,
                ArgusConfigDefaults.CUSTOM_ANIMATIONS_ENABLED,
                ArgusConfigDefaults.RANDOM_ENTITIES_ENABLED,
                ArgusConfigDefaults.ENTITY_TEXTURES_ENABLED,
                ArgusConfigDefaults.RANDOM_BLOCK_ENTITY_TEXTURES,
                ArgusConfigDefaults.ENTITY_EMISSIVE_TEXTURES,
                ArgusConfigDefaults.BLOCK_ENTITY_EMISSIVE_TEXTURES,
                ArgusConfigDefaults.ENTITY_TEXTURE_DEBUG,
                ArgusConfigDefaults.CUSTOM_ENTITY_MODELS_ENABLED,
                ArgusConfigDefaults.CUSTOM_ANIMATION_MIPMAP_DISTANCE,
                ArgusConfigDefaults.DETAILS_SKY_ENABLED,
                ArgusConfigDefaults.DETAILS_SUN_ENABLED,
                ArgusConfigDefaults.DETAILS_MOON_ENABLED,
                ArgusConfigDefaults.DETAILS_STARS_ENABLED,
                ArgusConfigDefaults.DETAILS_CLOUDS_ENABLED,
                ArgusConfigDefaults.DETAILS_CLOUD_HEIGHT,
                ArgusConfigDefaults.DETAILS_RAIN_SNOW_ENABLED,
                ArgusConfigDefaults.DETAILS_VIGNETTE_ENABLED,
                ArgusConfigDefaults.ANIMATIONS_ENABLED,
                ArgusConfigDefaults.ANIMATION_WATER,
                ArgusConfigDefaults.ANIMATION_LAVA,
                ArgusConfigDefaults.ANIMATION_FIRE,
                ArgusConfigDefaults.ANIMATION_PORTAL,
                ArgusConfigDefaults.ANIMATION_SCULK_SENSOR,
                ArgusConfigDefaults.ANIMATION_BLOCKS,
                ArgusConfigDefaults.PARTICLES_ENABLED,
                ArgusConfigDefaults.PARTICLE_RAIN_SPLASH,
                ArgusConfigDefaults.PARTICLE_BLOCK_BREAK,
                ArgusConfigDefaults.PARTICLE_BLOCK_BREAKING,
                ArgusConfigDefaults.PARTICLE_EXPLOSION,
                ArgusConfigDefaults.PARTICLE_WATER,
                ArgusConfigDefaults.PARTICLE_SMOKE,
                ArgusConfigDefaults.PARTICLE_POTION,
                ArgusConfigDefaults.PARTICLE_PORTAL,
                ArgusConfigDefaults.PARTICLE_FLAME,
                ArgusConfigDefaults.PARTICLE_REDSTONE,
                ArgusConfigDefaults.PARTICLE_DRIPPING,
                ArgusConfigDefaults.PARTICLE_FIREWORK,
                ArgusConfigDefaults.FOG_ENABLED,
                ArgusConfigDefaults.FOG_WATER,
                ArgusConfigDefaults.FOG_LAVA,
                ArgusConfigDefaults.FOG_POWDER_SNOW,
                ArgusConfigDefaults.FOG_AIR,
                ArgusConfigDefaults.ENTITY_SHADOWS_ENABLED,
                ArgusConfigDefaults.ENTITY_NAME_TAGS_ENABLED,
                ArgusConfigDefaults.ENTITY_PLAYER_NAME_TAGS,
                ArgusConfigDefaults.ENTITY_ITEM_FRAMES,
                ArgusConfigDefaults.ENTITY_PAINTINGS,
                ArgusConfigDefaults.ENTITY_PISTON_ANIMATIONS,
                ArgusConfigDefaults.ENTITY_BEACON_BEAM,
                ArgusConfigDefaults.ENTITY_LIMIT_BEACON_BEAM_HEIGHT,
                ArgusConfigDefaults.ENTITY_ENCHANTING_TABLE_BOOK,
                ArgusConfigDefaults.SHOW_FPS,
                ArgusConfigDefaults.SHOW_FPS_EXTENDED,
                ArgusConfigDefaults.SHOW_COORDS,
                ArgusConfigDefaults.OVERLAY_CORNER,
                ArgusConfigDefaults.TEXT_CONTRAST,
                ArgusConfigDefaults.STEADY_DEBUG_HUD,
                ArgusConfigDefaults.STEADY_DEBUG_HUD_REFRESH_INTERVAL,
                ArgusConfigDefaults.TOAST_ADVANCEMENT,
                ArgusConfigDefaults.TOAST_RECIPE,
                ArgusConfigDefaults.TOAST_SYSTEM,
                ArgusConfigDefaults.TOAST_TUTORIAL,
                ArgusConfigDefaults.INSTANT_SNEAK,
                ArgusConfigDefaults.FULLSCREEN_MODE,
                ArgusConfigDefaults.BIOME_COLORS_ENABLED,
                ArgusConfigDefaults.SKY_COLORS_ENABLED);
    }

    public ArgusConfig(boolean enabled,
                        boolean safeMode,
                        boolean verifyMode,
                        boolean ctmEnabled,
                        boolean ctmDebugLogging,
                        BetterGrassMode betterGrassMode,
                        boolean betterGrassIgnoreResourcePack,
                        boolean betterGrassGrassBlock,
                        boolean betterGrassSnowyGrassBlock,
                        boolean betterGrassDirtPath,
                        boolean betterGrassFarmland,
                        boolean betterGrassMycelium,
                        boolean betterGrassPodzol,
                        boolean betterGrassCrimsonNylium,
                        boolean betterGrassWarpedNylium) {
        this(enabled, safeMode, verifyMode, ctmEnabled, ctmDebugLogging,
                ArgusConfigDefaults.DUPLICATE_TRANSLUCENT_BACKFACES,
                betterGrassMode,
                betterGrassIgnoreResourcePack,
                betterGrassGrassBlock, betterGrassSnowyGrassBlock,
                betterGrassDirtPath, betterGrassFarmland, betterGrassMycelium,
                betterGrassPodzol, betterGrassCrimsonNylium,
                betterGrassWarpedNylium,
                ArgusConfigDefaults.CIT_ENABLED,
                ArgusConfigDefaults.CUSTOM_GUI_ENABLED,
                ArgusConfigDefaults.CUSTOM_COLORS_ENABLED,
                ArgusConfigDefaults.CUSTOM_SKY_ENABLED,
                ArgusConfigDefaults.NATURAL_TEXTURES_ENABLED,
                ArgusConfigDefaults.BETTER_SNOW_ENABLED,
                ArgusConfigDefaults.CUSTOM_ANIMATIONS_ENABLED,
                ArgusConfigDefaults.RANDOM_ENTITIES_ENABLED,
                ArgusConfigDefaults.ENTITY_TEXTURES_ENABLED,
                ArgusConfigDefaults.RANDOM_BLOCK_ENTITY_TEXTURES,
                ArgusConfigDefaults.ENTITY_EMISSIVE_TEXTURES,
                ArgusConfigDefaults.BLOCK_ENTITY_EMISSIVE_TEXTURES,
                ArgusConfigDefaults.ENTITY_TEXTURE_DEBUG,
                ArgusConfigDefaults.CUSTOM_ENTITY_MODELS_ENABLED,
                ArgusConfigDefaults.CUSTOM_ANIMATION_MIPMAP_DISTANCE,
                ArgusConfigDefaults.DETAILS_SKY_ENABLED,
                ArgusConfigDefaults.DETAILS_SUN_ENABLED,
                ArgusConfigDefaults.DETAILS_MOON_ENABLED,
                ArgusConfigDefaults.DETAILS_STARS_ENABLED,
                ArgusConfigDefaults.DETAILS_CLOUDS_ENABLED,
                ArgusConfigDefaults.DETAILS_CLOUD_HEIGHT,
                ArgusConfigDefaults.DETAILS_RAIN_SNOW_ENABLED,
                ArgusConfigDefaults.DETAILS_VIGNETTE_ENABLED,
                ArgusConfigDefaults.ANIMATIONS_ENABLED,
                ArgusConfigDefaults.ANIMATION_WATER,
                ArgusConfigDefaults.ANIMATION_LAVA,
                ArgusConfigDefaults.ANIMATION_FIRE,
                ArgusConfigDefaults.ANIMATION_PORTAL,
                ArgusConfigDefaults.ANIMATION_SCULK_SENSOR,
                ArgusConfigDefaults.ANIMATION_BLOCKS,
                ArgusConfigDefaults.PARTICLES_ENABLED,
                ArgusConfigDefaults.PARTICLE_RAIN_SPLASH,
                ArgusConfigDefaults.PARTICLE_BLOCK_BREAK,
                ArgusConfigDefaults.PARTICLE_BLOCK_BREAKING,
                ArgusConfigDefaults.PARTICLE_EXPLOSION,
                ArgusConfigDefaults.PARTICLE_WATER,
                ArgusConfigDefaults.PARTICLE_SMOKE,
                ArgusConfigDefaults.PARTICLE_POTION,
                ArgusConfigDefaults.PARTICLE_PORTAL,
                ArgusConfigDefaults.PARTICLE_FLAME,
                ArgusConfigDefaults.PARTICLE_REDSTONE,
                ArgusConfigDefaults.PARTICLE_DRIPPING,
                ArgusConfigDefaults.PARTICLE_FIREWORK,
                ArgusConfigDefaults.FOG_ENABLED,
                ArgusConfigDefaults.FOG_WATER,
                ArgusConfigDefaults.FOG_LAVA,
                ArgusConfigDefaults.FOG_POWDER_SNOW,
                ArgusConfigDefaults.FOG_AIR,
                ArgusConfigDefaults.ENTITY_SHADOWS_ENABLED,
                ArgusConfigDefaults.ENTITY_NAME_TAGS_ENABLED,
                ArgusConfigDefaults.ENTITY_PLAYER_NAME_TAGS,
                ArgusConfigDefaults.ENTITY_ITEM_FRAMES,
                ArgusConfigDefaults.ENTITY_PAINTINGS,
                ArgusConfigDefaults.ENTITY_PISTON_ANIMATIONS,
                ArgusConfigDefaults.ENTITY_BEACON_BEAM,
                ArgusConfigDefaults.ENTITY_LIMIT_BEACON_BEAM_HEIGHT,
                ArgusConfigDefaults.ENTITY_ENCHANTING_TABLE_BOOK,
                ArgusConfigDefaults.SHOW_FPS,
                ArgusConfigDefaults.SHOW_FPS_EXTENDED,
                ArgusConfigDefaults.SHOW_COORDS,
                ArgusConfigDefaults.OVERLAY_CORNER,
                ArgusConfigDefaults.TEXT_CONTRAST,
                ArgusConfigDefaults.STEADY_DEBUG_HUD,
                ArgusConfigDefaults.STEADY_DEBUG_HUD_REFRESH_INTERVAL,
                ArgusConfigDefaults.TOAST_ADVANCEMENT,
                ArgusConfigDefaults.TOAST_RECIPE,
                ArgusConfigDefaults.TOAST_SYSTEM,
                ArgusConfigDefaults.TOAST_TUTORIAL,
                ArgusConfigDefaults.INSTANT_SNEAK,
                ArgusConfigDefaults.FULLSCREEN_MODE,
                ArgusConfigDefaults.BIOME_COLORS_ENABLED,
                ArgusConfigDefaults.SKY_COLORS_ENABLED);
    }

    public ArgusConfig(boolean enabled,
                        boolean safeMode,
                        boolean verifyMode,
                        boolean ctmEnabled,
                        boolean ctmDebugLogging,
                        BetterGrassMode betterGrassMode,
                        boolean betterGrassGrassBlock,
                        boolean betterGrassSnowyGrassBlock,
                        boolean betterGrassDirtPath,
                        boolean betterGrassFarmland,
                        boolean betterGrassMycelium,
                        boolean betterGrassPodzol,
                        boolean betterGrassCrimsonNylium,
                        boolean betterGrassWarpedNylium) {
        this(enabled, safeMode, verifyMode, ctmEnabled, ctmDebugLogging,
                ArgusConfigDefaults.DUPLICATE_TRANSLUCENT_BACKFACES,
                betterGrassMode,
                ArgusConfigDefaults.BETTER_GRASS_IGNORE_RESOURCE_PACK,
                betterGrassGrassBlock, betterGrassSnowyGrassBlock,
                betterGrassDirtPath, betterGrassFarmland, betterGrassMycelium,
                betterGrassPodzol, betterGrassCrimsonNylium,
                betterGrassWarpedNylium,
                ArgusConfigDefaults.CIT_ENABLED,
                ArgusConfigDefaults.CUSTOM_GUI_ENABLED,
                ArgusConfigDefaults.CUSTOM_COLORS_ENABLED,
                ArgusConfigDefaults.CUSTOM_SKY_ENABLED,
                ArgusConfigDefaults.NATURAL_TEXTURES_ENABLED,
                ArgusConfigDefaults.BETTER_SNOW_ENABLED,
                ArgusConfigDefaults.CUSTOM_ANIMATIONS_ENABLED,
                ArgusConfigDefaults.RANDOM_ENTITIES_ENABLED,
                ArgusConfigDefaults.ENTITY_TEXTURES_ENABLED,
                ArgusConfigDefaults.RANDOM_BLOCK_ENTITY_TEXTURES,
                ArgusConfigDefaults.ENTITY_EMISSIVE_TEXTURES,
                ArgusConfigDefaults.BLOCK_ENTITY_EMISSIVE_TEXTURES,
                ArgusConfigDefaults.ENTITY_TEXTURE_DEBUG,
                ArgusConfigDefaults.CUSTOM_ENTITY_MODELS_ENABLED,
                ArgusConfigDefaults.CUSTOM_ANIMATION_MIPMAP_DISTANCE,
                ArgusConfigDefaults.DETAILS_SKY_ENABLED,
                ArgusConfigDefaults.DETAILS_SUN_ENABLED,
                ArgusConfigDefaults.DETAILS_MOON_ENABLED,
                ArgusConfigDefaults.DETAILS_STARS_ENABLED,
                ArgusConfigDefaults.DETAILS_CLOUDS_ENABLED,
                ArgusConfigDefaults.DETAILS_CLOUD_HEIGHT,
                ArgusConfigDefaults.DETAILS_RAIN_SNOW_ENABLED,
                ArgusConfigDefaults.DETAILS_VIGNETTE_ENABLED,
                ArgusConfigDefaults.ANIMATIONS_ENABLED,
                ArgusConfigDefaults.ANIMATION_WATER,
                ArgusConfigDefaults.ANIMATION_LAVA,
                ArgusConfigDefaults.ANIMATION_FIRE,
                ArgusConfigDefaults.ANIMATION_PORTAL,
                ArgusConfigDefaults.ANIMATION_SCULK_SENSOR,
                ArgusConfigDefaults.ANIMATION_BLOCKS,
                ArgusConfigDefaults.PARTICLES_ENABLED,
                ArgusConfigDefaults.PARTICLE_RAIN_SPLASH,
                ArgusConfigDefaults.PARTICLE_BLOCK_BREAK,
                ArgusConfigDefaults.PARTICLE_BLOCK_BREAKING,
                ArgusConfigDefaults.PARTICLE_EXPLOSION,
                ArgusConfigDefaults.PARTICLE_WATER,
                ArgusConfigDefaults.PARTICLE_SMOKE,
                ArgusConfigDefaults.PARTICLE_POTION,
                ArgusConfigDefaults.PARTICLE_PORTAL,
                ArgusConfigDefaults.PARTICLE_FLAME,
                ArgusConfigDefaults.PARTICLE_REDSTONE,
                ArgusConfigDefaults.PARTICLE_DRIPPING,
                ArgusConfigDefaults.PARTICLE_FIREWORK,
                ArgusConfigDefaults.FOG_ENABLED,
                ArgusConfigDefaults.FOG_WATER,
                ArgusConfigDefaults.FOG_LAVA,
                ArgusConfigDefaults.FOG_POWDER_SNOW,
                ArgusConfigDefaults.FOG_AIR,
                ArgusConfigDefaults.ENTITY_SHADOWS_ENABLED,
                ArgusConfigDefaults.ENTITY_NAME_TAGS_ENABLED,
                ArgusConfigDefaults.ENTITY_PLAYER_NAME_TAGS,
                ArgusConfigDefaults.ENTITY_ITEM_FRAMES,
                ArgusConfigDefaults.ENTITY_PAINTINGS,
                ArgusConfigDefaults.ENTITY_PISTON_ANIMATIONS,
                ArgusConfigDefaults.ENTITY_BEACON_BEAM,
                ArgusConfigDefaults.ENTITY_LIMIT_BEACON_BEAM_HEIGHT,
                ArgusConfigDefaults.ENTITY_ENCHANTING_TABLE_BOOK,
                ArgusConfigDefaults.SHOW_FPS,
                ArgusConfigDefaults.SHOW_FPS_EXTENDED,
                ArgusConfigDefaults.SHOW_COORDS,
                ArgusConfigDefaults.OVERLAY_CORNER,
                ArgusConfigDefaults.TEXT_CONTRAST,
                ArgusConfigDefaults.STEADY_DEBUG_HUD,
                ArgusConfigDefaults.STEADY_DEBUG_HUD_REFRESH_INTERVAL,
                ArgusConfigDefaults.TOAST_ADVANCEMENT,
                ArgusConfigDefaults.TOAST_RECIPE,
                ArgusConfigDefaults.TOAST_SYSTEM,
                ArgusConfigDefaults.TOAST_TUTORIAL,
                ArgusConfigDefaults.INSTANT_SNEAK,
                ArgusConfigDefaults.FULLSCREEN_MODE,
                ArgusConfigDefaults.BIOME_COLORS_ENABLED,
                ArgusConfigDefaults.SKY_COLORS_ENABLED);
    }

    public ArgusConfig(boolean enabled,
                        boolean safeMode,
                        boolean verifyMode,
                        boolean ctmEnabled,
                        boolean ctmDebugLogging,
                        boolean duplicateTranslucentBackfaces,
                        BetterGrassMode betterGrassMode,
                        boolean betterGrassIgnoreResourcePack,
                        boolean betterGrassGrassBlock,
                        boolean betterGrassSnowyGrassBlock,
                        boolean betterGrassDirtPath,
                        boolean betterGrassFarmland,
                        boolean betterGrassMycelium,
                        boolean betterGrassPodzol,
                        boolean betterGrassCrimsonNylium,
                        boolean betterGrassWarpedNylium) {
        this(enabled, safeMode, verifyMode, ctmEnabled, ctmDebugLogging,
                duplicateTranslucentBackfaces, betterGrassMode,
                betterGrassIgnoreResourcePack,
                betterGrassGrassBlock, betterGrassSnowyGrassBlock,
                betterGrassDirtPath, betterGrassFarmland, betterGrassMycelium,
                betterGrassPodzol, betterGrassCrimsonNylium,
                betterGrassWarpedNylium,
                ArgusConfigDefaults.CIT_ENABLED,
                ArgusConfigDefaults.CUSTOM_GUI_ENABLED,
                ArgusConfigDefaults.CUSTOM_COLORS_ENABLED,
                ArgusConfigDefaults.CUSTOM_SKY_ENABLED,
                ArgusConfigDefaults.NATURAL_TEXTURES_ENABLED,
                ArgusConfigDefaults.BETTER_SNOW_ENABLED,
                ArgusConfigDefaults.CUSTOM_ANIMATIONS_ENABLED,
                ArgusConfigDefaults.RANDOM_ENTITIES_ENABLED,
                ArgusConfigDefaults.ENTITY_TEXTURES_ENABLED,
                ArgusConfigDefaults.RANDOM_BLOCK_ENTITY_TEXTURES,
                ArgusConfigDefaults.ENTITY_EMISSIVE_TEXTURES,
                ArgusConfigDefaults.BLOCK_ENTITY_EMISSIVE_TEXTURES,
                ArgusConfigDefaults.ENTITY_TEXTURE_DEBUG,
                ArgusConfigDefaults.CUSTOM_ENTITY_MODELS_ENABLED,
                ArgusConfigDefaults.CUSTOM_ANIMATION_MIPMAP_DISTANCE,
                ArgusConfigDefaults.DETAILS_SKY_ENABLED,
                ArgusConfigDefaults.DETAILS_SUN_ENABLED,
                ArgusConfigDefaults.DETAILS_MOON_ENABLED,
                ArgusConfigDefaults.DETAILS_STARS_ENABLED,
                ArgusConfigDefaults.DETAILS_CLOUDS_ENABLED,
                ArgusConfigDefaults.DETAILS_CLOUD_HEIGHT,
                ArgusConfigDefaults.DETAILS_RAIN_SNOW_ENABLED,
                ArgusConfigDefaults.DETAILS_VIGNETTE_ENABLED,
                ArgusConfigDefaults.ANIMATIONS_ENABLED,
                ArgusConfigDefaults.ANIMATION_WATER,
                ArgusConfigDefaults.ANIMATION_LAVA,
                ArgusConfigDefaults.ANIMATION_FIRE,
                ArgusConfigDefaults.ANIMATION_PORTAL,
                ArgusConfigDefaults.ANIMATION_SCULK_SENSOR,
                ArgusConfigDefaults.ANIMATION_BLOCKS,
                ArgusConfigDefaults.PARTICLES_ENABLED,
                ArgusConfigDefaults.PARTICLE_RAIN_SPLASH,
                ArgusConfigDefaults.PARTICLE_BLOCK_BREAK,
                ArgusConfigDefaults.PARTICLE_BLOCK_BREAKING,
                ArgusConfigDefaults.PARTICLE_EXPLOSION,
                ArgusConfigDefaults.PARTICLE_WATER,
                ArgusConfigDefaults.PARTICLE_SMOKE,
                ArgusConfigDefaults.PARTICLE_POTION,
                ArgusConfigDefaults.PARTICLE_PORTAL,
                ArgusConfigDefaults.PARTICLE_FLAME,
                ArgusConfigDefaults.PARTICLE_REDSTONE,
                ArgusConfigDefaults.PARTICLE_DRIPPING,
                ArgusConfigDefaults.PARTICLE_FIREWORK,
                ArgusConfigDefaults.FOG_ENABLED,
                ArgusConfigDefaults.FOG_WATER,
                ArgusConfigDefaults.FOG_LAVA,
                ArgusConfigDefaults.FOG_POWDER_SNOW,
                ArgusConfigDefaults.FOG_AIR,
                ArgusConfigDefaults.ENTITY_SHADOWS_ENABLED,
                ArgusConfigDefaults.ENTITY_NAME_TAGS_ENABLED,
                ArgusConfigDefaults.ENTITY_PLAYER_NAME_TAGS,
                ArgusConfigDefaults.ENTITY_ITEM_FRAMES,
                ArgusConfigDefaults.ENTITY_PAINTINGS,
                ArgusConfigDefaults.ENTITY_PISTON_ANIMATIONS,
                ArgusConfigDefaults.ENTITY_BEACON_BEAM,
                ArgusConfigDefaults.ENTITY_LIMIT_BEACON_BEAM_HEIGHT,
                ArgusConfigDefaults.ENTITY_ENCHANTING_TABLE_BOOK,
                ArgusConfigDefaults.SHOW_FPS,
                ArgusConfigDefaults.SHOW_FPS_EXTENDED,
                ArgusConfigDefaults.SHOW_COORDS,
                ArgusConfigDefaults.OVERLAY_CORNER,
                ArgusConfigDefaults.TEXT_CONTRAST,
                ArgusConfigDefaults.STEADY_DEBUG_HUD,
                ArgusConfigDefaults.STEADY_DEBUG_HUD_REFRESH_INTERVAL,
                ArgusConfigDefaults.TOAST_ADVANCEMENT,
                ArgusConfigDefaults.TOAST_RECIPE,
                ArgusConfigDefaults.TOAST_SYSTEM,
                ArgusConfigDefaults.TOAST_TUTORIAL,
                ArgusConfigDefaults.INSTANT_SNEAK,
                ArgusConfigDefaults.FULLSCREEN_MODE,
                ArgusConfigDefaults.BIOME_COLORS_ENABLED,
                ArgusConfigDefaults.SKY_COLORS_ENABLED);
    }

    public ArgusConfig(boolean enabled,
                        boolean safeMode,
                        boolean verifyMode,
                        boolean ctmEnabled,
                        boolean ctmDebugLogging,
                        boolean duplicateTranslucentBackfaces,
                        BetterGrassMode betterGrassMode,
                        boolean betterGrassIgnoreResourcePack,
                        boolean betterGrassGrassBlock,
                        boolean betterGrassSnowyGrassBlock,
                        boolean betterGrassDirtPath,
                        boolean betterGrassFarmland,
                        boolean betterGrassMycelium,
                        boolean betterGrassPodzol,
                        boolean betterGrassCrimsonNylium,
                        boolean betterGrassWarpedNylium,
                        boolean citEnabled,
                        boolean customGuiEnabled,
                        boolean customColorsEnabled,
                        boolean customAnimationsEnabled,
                        int customAnimationMipmapDistance) {
        this(enabled, safeMode, verifyMode, ctmEnabled, ctmDebugLogging,
                duplicateTranslucentBackfaces, betterGrassMode,
                betterGrassIgnoreResourcePack, betterGrassGrassBlock,
                betterGrassSnowyGrassBlock, betterGrassDirtPath,
                betterGrassFarmland, betterGrassMycelium, betterGrassPodzol,
                betterGrassCrimsonNylium, betterGrassWarpedNylium,
                citEnabled, customGuiEnabled, customColorsEnabled,
                ArgusConfigDefaults.CUSTOM_SKY_ENABLED,
                ArgusConfigDefaults.NATURAL_TEXTURES_ENABLED,
                ArgusConfigDefaults.BETTER_SNOW_ENABLED,
                customAnimationsEnabled,
                ArgusConfigDefaults.RANDOM_ENTITIES_ENABLED,
                ArgusConfigDefaults.ENTITY_TEXTURES_ENABLED,
                ArgusConfigDefaults.RANDOM_BLOCK_ENTITY_TEXTURES,
                ArgusConfigDefaults.ENTITY_EMISSIVE_TEXTURES,
                ArgusConfigDefaults.BLOCK_ENTITY_EMISSIVE_TEXTURES,
                ArgusConfigDefaults.ENTITY_TEXTURE_DEBUG,
                ArgusConfigDefaults.CUSTOM_ENTITY_MODELS_ENABLED,
                customAnimationMipmapDistance,
                ArgusConfigDefaults.DETAILS_SKY_ENABLED,
                ArgusConfigDefaults.DETAILS_SUN_ENABLED,
                ArgusConfigDefaults.DETAILS_MOON_ENABLED,
                ArgusConfigDefaults.DETAILS_STARS_ENABLED,
                ArgusConfigDefaults.DETAILS_CLOUDS_ENABLED,
                ArgusConfigDefaults.DETAILS_CLOUD_HEIGHT,
                ArgusConfigDefaults.DETAILS_RAIN_SNOW_ENABLED,
                ArgusConfigDefaults.DETAILS_VIGNETTE_ENABLED,
                ArgusConfigDefaults.ANIMATIONS_ENABLED,
                ArgusConfigDefaults.ANIMATION_WATER,
                ArgusConfigDefaults.ANIMATION_LAVA,
                ArgusConfigDefaults.ANIMATION_FIRE,
                ArgusConfigDefaults.ANIMATION_PORTAL,
                ArgusConfigDefaults.ANIMATION_SCULK_SENSOR,
                ArgusConfigDefaults.ANIMATION_BLOCKS,
                ArgusConfigDefaults.PARTICLES_ENABLED,
                ArgusConfigDefaults.PARTICLE_RAIN_SPLASH,
                ArgusConfigDefaults.PARTICLE_BLOCK_BREAK,
                ArgusConfigDefaults.PARTICLE_BLOCK_BREAKING,
                ArgusConfigDefaults.PARTICLE_EXPLOSION,
                ArgusConfigDefaults.PARTICLE_WATER,
                ArgusConfigDefaults.PARTICLE_SMOKE,
                ArgusConfigDefaults.PARTICLE_POTION,
                ArgusConfigDefaults.PARTICLE_PORTAL,
                ArgusConfigDefaults.PARTICLE_FLAME,
                ArgusConfigDefaults.PARTICLE_REDSTONE,
                ArgusConfigDefaults.PARTICLE_DRIPPING,
                ArgusConfigDefaults.PARTICLE_FIREWORK,
                ArgusConfigDefaults.FOG_ENABLED,
                ArgusConfigDefaults.FOG_WATER,
                ArgusConfigDefaults.FOG_LAVA,
                ArgusConfigDefaults.FOG_POWDER_SNOW,
                ArgusConfigDefaults.FOG_AIR,
                ArgusConfigDefaults.ENTITY_SHADOWS_ENABLED,
                ArgusConfigDefaults.ENTITY_NAME_TAGS_ENABLED,
                ArgusConfigDefaults.ENTITY_PLAYER_NAME_TAGS,
                ArgusConfigDefaults.ENTITY_ITEM_FRAMES,
                ArgusConfigDefaults.ENTITY_PAINTINGS,
                ArgusConfigDefaults.ENTITY_PISTON_ANIMATIONS,
                ArgusConfigDefaults.ENTITY_BEACON_BEAM,
                ArgusConfigDefaults.ENTITY_LIMIT_BEACON_BEAM_HEIGHT,
                ArgusConfigDefaults.ENTITY_ENCHANTING_TABLE_BOOK,
                ArgusConfigDefaults.SHOW_FPS,
                ArgusConfigDefaults.SHOW_FPS_EXTENDED,
                ArgusConfigDefaults.SHOW_COORDS,
                ArgusConfigDefaults.OVERLAY_CORNER,
                ArgusConfigDefaults.TEXT_CONTRAST,
                ArgusConfigDefaults.STEADY_DEBUG_HUD,
                ArgusConfigDefaults.STEADY_DEBUG_HUD_REFRESH_INTERVAL,
                ArgusConfigDefaults.TOAST_ADVANCEMENT,
                ArgusConfigDefaults.TOAST_RECIPE,
                ArgusConfigDefaults.TOAST_SYSTEM,
                ArgusConfigDefaults.TOAST_TUTORIAL,
                ArgusConfigDefaults.INSTANT_SNEAK,
                ArgusConfigDefaults.FULLSCREEN_MODE,
                ArgusConfigDefaults.BIOME_COLORS_ENABLED,
                ArgusConfigDefaults.SKY_COLORS_ENABLED);
    }

    public ArgusConfig(boolean enabled,
                        boolean safeMode,
                        boolean verifyMode,
                        boolean ctmEnabled,
                        boolean ctmDebugLogging,
                        boolean duplicateTranslucentBackfaces,
                        BetterGrassMode betterGrassMode,
                        boolean betterGrassIgnoreResourcePack,
                        boolean betterGrassGrassBlock,
                        boolean betterGrassSnowyGrassBlock,
                        boolean betterGrassDirtPath,
                        boolean betterGrassFarmland,
                        boolean betterGrassMycelium,
                        boolean betterGrassPodzol,
                        boolean betterGrassCrimsonNylium,
                        boolean betterGrassWarpedNylium,
                        boolean citEnabled,
                        boolean customGuiEnabled,
                        boolean customColorsEnabled,
                        boolean customSkyEnabled,
                        boolean customAnimationsEnabled,
                        int customAnimationMipmapDistance) {
        this(enabled, safeMode, verifyMode, ctmEnabled, ctmDebugLogging,
                duplicateTranslucentBackfaces, betterGrassMode,
                betterGrassIgnoreResourcePack, betterGrassGrassBlock,
                betterGrassSnowyGrassBlock, betterGrassDirtPath,
                betterGrassFarmland, betterGrassMycelium, betterGrassPodzol,
                betterGrassCrimsonNylium, betterGrassWarpedNylium,
                citEnabled, customGuiEnabled, customColorsEnabled,
                customSkyEnabled,
                ArgusConfigDefaults.NATURAL_TEXTURES_ENABLED,
                ArgusConfigDefaults.BETTER_SNOW_ENABLED,
                customAnimationsEnabled,
                ArgusConfigDefaults.RANDOM_ENTITIES_ENABLED,
                ArgusConfigDefaults.ENTITY_TEXTURES_ENABLED,
                ArgusConfigDefaults.RANDOM_BLOCK_ENTITY_TEXTURES,
                ArgusConfigDefaults.ENTITY_EMISSIVE_TEXTURES,
                ArgusConfigDefaults.BLOCK_ENTITY_EMISSIVE_TEXTURES,
                ArgusConfigDefaults.ENTITY_TEXTURE_DEBUG,
                ArgusConfigDefaults.CUSTOM_ENTITY_MODELS_ENABLED,
                customAnimationMipmapDistance,
                ArgusConfigDefaults.DETAILS_SKY_ENABLED,
                ArgusConfigDefaults.DETAILS_SUN_ENABLED,
                ArgusConfigDefaults.DETAILS_MOON_ENABLED,
                ArgusConfigDefaults.DETAILS_STARS_ENABLED,
                ArgusConfigDefaults.DETAILS_CLOUDS_ENABLED,
                ArgusConfigDefaults.DETAILS_CLOUD_HEIGHT,
                ArgusConfigDefaults.DETAILS_RAIN_SNOW_ENABLED,
                ArgusConfigDefaults.DETAILS_VIGNETTE_ENABLED,
                ArgusConfigDefaults.ANIMATIONS_ENABLED,
                ArgusConfigDefaults.ANIMATION_WATER,
                ArgusConfigDefaults.ANIMATION_LAVA,
                ArgusConfigDefaults.ANIMATION_FIRE,
                ArgusConfigDefaults.ANIMATION_PORTAL,
                ArgusConfigDefaults.ANIMATION_SCULK_SENSOR,
                ArgusConfigDefaults.ANIMATION_BLOCKS,
                ArgusConfigDefaults.PARTICLES_ENABLED,
                ArgusConfigDefaults.PARTICLE_RAIN_SPLASH,
                ArgusConfigDefaults.PARTICLE_BLOCK_BREAK,
                ArgusConfigDefaults.PARTICLE_BLOCK_BREAKING,
                ArgusConfigDefaults.PARTICLE_EXPLOSION,
                ArgusConfigDefaults.PARTICLE_WATER,
                ArgusConfigDefaults.PARTICLE_SMOKE,
                ArgusConfigDefaults.PARTICLE_POTION,
                ArgusConfigDefaults.PARTICLE_PORTAL,
                ArgusConfigDefaults.PARTICLE_FLAME,
                ArgusConfigDefaults.PARTICLE_REDSTONE,
                ArgusConfigDefaults.PARTICLE_DRIPPING,
                ArgusConfigDefaults.PARTICLE_FIREWORK,
                ArgusConfigDefaults.FOG_ENABLED,
                ArgusConfigDefaults.FOG_WATER,
                ArgusConfigDefaults.FOG_LAVA,
                ArgusConfigDefaults.FOG_POWDER_SNOW,
                ArgusConfigDefaults.FOG_AIR,
                ArgusConfigDefaults.ENTITY_SHADOWS_ENABLED,
                ArgusConfigDefaults.ENTITY_NAME_TAGS_ENABLED,
                ArgusConfigDefaults.ENTITY_PLAYER_NAME_TAGS,
                ArgusConfigDefaults.ENTITY_ITEM_FRAMES,
                ArgusConfigDefaults.ENTITY_PAINTINGS,
                ArgusConfigDefaults.ENTITY_PISTON_ANIMATIONS,
                ArgusConfigDefaults.ENTITY_BEACON_BEAM,
                ArgusConfigDefaults.ENTITY_LIMIT_BEACON_BEAM_HEIGHT,
                ArgusConfigDefaults.ENTITY_ENCHANTING_TABLE_BOOK,
                ArgusConfigDefaults.SHOW_FPS,
                ArgusConfigDefaults.SHOW_FPS_EXTENDED,
                ArgusConfigDefaults.SHOW_COORDS,
                ArgusConfigDefaults.OVERLAY_CORNER,
                ArgusConfigDefaults.TEXT_CONTRAST,
                ArgusConfigDefaults.STEADY_DEBUG_HUD,
                ArgusConfigDefaults.STEADY_DEBUG_HUD_REFRESH_INTERVAL,
                ArgusConfigDefaults.TOAST_ADVANCEMENT,
                ArgusConfigDefaults.TOAST_RECIPE,
                ArgusConfigDefaults.TOAST_SYSTEM,
                ArgusConfigDefaults.TOAST_TUTORIAL,
                ArgusConfigDefaults.INSTANT_SNEAK,
                ArgusConfigDefaults.FULLSCREEN_MODE,
                ArgusConfigDefaults.BIOME_COLORS_ENABLED,
                ArgusConfigDefaults.SKY_COLORS_ENABLED);
    }

    /**
     * Returns {@code true} iff the CTM feature is on. Convenience
     * for the renderer-side code, which only has to test a single
     * flag instead of the master {@code enabled} + per-feature
     * {@code ctmEnabled} combination.
     */
    public boolean ctmActive() {
        return enabled && ctmEnabled;
    }

    /**
     * Returns {@code true} when Better Grass should run in the renderer.
     */
    public boolean betterGrassActive() {
        return enabled && betterGrassMode != BetterGrassMode.OFF;
    }

    /**
     * Returns {@code true} when the Better Grass feature has at least one
     * enabled block family.
     */
    public boolean anyBetterGrassBlockEnabled() {
        return betterGrassGrassBlock
                || betterGrassSnowyGrassBlock
                || betterGrassDirtPath
                || betterGrassFarmland
                || betterGrassMycelium
                || betterGrassPodzol
                || betterGrassCrimsonNylium
                || betterGrassWarpedNylium;
    }

    /**
     * Returns {@code true} when Custom Item Textures should run.
     */
    public boolean citActive() {
        return enabled && citEnabled;
    }

    /**
     * Returns {@code true} when Custom GUI texture replacement should run.
     */
    public boolean customGuiActive() {
        return enabled && customGuiEnabled;
    }

    /**
     * Returns {@code true} when Custom Colors and Colormaps should run.
     */
    public boolean customColorsActive() {
        return enabled && customColorsEnabled;
    }

    /**
     * Returns {@code true} when biome or terrain tint overrides may run.
     */
    public boolean customBiomeColorsActive() {
        return customColorsActive() && biomeColorsEnabled;
    }

    /**
     * Returns {@code true} when sky or fog color overrides may run.
     */
    public boolean customSkyColorsActive() {
        return customColorsActive() && skyColorsEnabled;
    }

    /**
     * Returns {@code true} when Custom Sky layers should render.
     */
    public boolean customSkyActive() {
        return enabled && customSkyEnabled && detailsSkyEnabled;
    }

    /**
     * Returns {@code true} when Natural Textures should alter terrain quads.
     */
    public boolean naturalTexturesActive() {
        return enabled && naturalTexturesEnabled;
    }

    /**
     * Returns {@code true} when Better Snow layer emission should run.
     */
    public boolean betterSnowActive() {
        return enabled && betterSnowEnabled;
    }

    /**
     * Returns {@code true} when Custom Animations should tick and upload.
     */
    public boolean customAnimationsActive() {
        return enabled && animationsEnabled && customAnimationsEnabled;
    }

    /**
     * Returns {@code true} when Random Entity Textures should run.
     */
    public boolean randomEntitiesActive() {
        return entityTextureFeaturesActive() && randomEntitiesEnabled;
    }

    /**
     * Returns {@code true} when Argus's OptiFine-style entity texture
     * features may run.
     */
    public boolean entityTextureFeaturesActive() {
        return enabled && entityTexturesEnabled;
    }

    /**
     * Returns {@code true} when random BlockEntity texture rules may run.
     */
    public boolean randomBlockEntityTexturesActive() {
        return entityTextureFeaturesActive() && randomBlockEntityTextures;
    }

    /**
     * Returns {@code true} when entity emissive overlays may render.
     */
    public boolean entityEmissiveTexturesActive() {
        return entityTextureFeaturesActive() && entityEmissiveTextures;
    }

    /**
     * Returns {@code true} when Custom Entity Models should run.
     */
    public boolean customEntityModelsActive() {
        return enabled && customEntityModelsEnabled;
    }

    /**
     * Returns a mutable builder initialized from this immutable snapshot.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Returns a mutable builder initialized from the supplied config.
     */
    public static Builder builder(ArgusConfig config) {
        return new Builder(config);
    }

    /**
     * Mutable copy builder for producing a new immutable config snapshot.
     *
     * <p>Threading: not thread-safe. It is used only by config parsing and UI
     * code before atomically publishing the finished {@link ArgusConfig}.
     *
     * <p>Performance: not in hot paths.
     */
    public static final class Builder {
        private boolean enabled;
        private boolean safeMode;
        private boolean verifyMode;
        private boolean ctmEnabled;
        private boolean ctmDebugLogging;
        private boolean duplicateTranslucentBackfaces;
        private BetterGrassMode betterGrassMode;
        private boolean betterGrassIgnoreResourcePack;
        private boolean betterGrassGrassBlock;
        private boolean betterGrassSnowyGrassBlock;
        private boolean betterGrassDirtPath;
        private boolean betterGrassFarmland;
        private boolean betterGrassMycelium;
        private boolean betterGrassPodzol;
        private boolean betterGrassCrimsonNylium;
        private boolean betterGrassWarpedNylium;
        private boolean citEnabled;
        private boolean customGuiEnabled;
        private boolean customColorsEnabled;
        private boolean customSkyEnabled;
        private boolean naturalTexturesEnabled;
        private boolean betterSnowEnabled;
        private boolean customAnimationsEnabled;
        private boolean randomEntitiesEnabled;
        private boolean entityTexturesEnabled;
        private boolean randomBlockEntityTextures;
        private boolean entityEmissiveTextures;
        private boolean blockEntityEmissiveTextures;
        private boolean entityTextureDebug;
        private boolean customEntityModelsEnabled;
        private int customAnimationMipmapDistance;
        private boolean detailsSkyEnabled;
        private boolean detailsSunEnabled;
        private boolean detailsMoonEnabled;
        private boolean detailsStarsEnabled;
        private boolean detailsCloudsEnabled;
        private int detailsCloudHeight;
        private boolean detailsRainSnowEnabled;
        private boolean detailsVignetteEnabled;
        private boolean animationsEnabled;
        private boolean animationWater;
        private boolean animationLava;
        private boolean animationFire;
        private boolean animationPortal;
        private boolean animationSculkSensor;
        private boolean animationBlocks;
        private boolean particlesEnabled;
        private boolean particleRainSplash;
        private boolean particleBlockBreak;
        private boolean particleBlockBreaking;
        private boolean particleExplosion;
        private boolean particleWater;
        private boolean particleSmoke;
        private boolean particlePotion;
        private boolean particlePortal;
        private boolean particleFlame;
        private boolean particleRedstone;
        private boolean particleDripping;
        private boolean particleFirework;
        private boolean fogEnabled;
        private boolean fogWater;
        private boolean fogLava;
        private boolean fogPowderSnow;
        private boolean fogAir;
        private boolean entityShadowsEnabled;
        private boolean entityNameTagsEnabled;
        private boolean entityPlayerNameTags;
        private boolean entityItemFrames;
        private boolean entityPaintings;
        private boolean entityPistonAnimations;
        private boolean entityBeaconBeam;
        private boolean entityLimitBeaconBeamHeight;
        private boolean entityEnchantingTableBook;
        private boolean showFps;
        private boolean showFpsExtended;
        private boolean showCoords;
        private OverlayCorner overlayCorner;
        private TextContrast textContrast;
        private boolean steadyDebugHud;
        private int steadyDebugHudRefreshInterval;
        private boolean toastAdvancement;
        private boolean toastRecipe;
        private boolean toastSystem;
        private boolean toastTutorial;
        private boolean instantSneak;
        private FullscreenMode fullscreenMode;
        private boolean biomeColorsEnabled;
        private boolean skyColorsEnabled;

        private Builder(ArgusConfig config) {
            this.enabled = config.enabled;
            this.safeMode = config.safeMode;
            this.verifyMode = config.verifyMode;
            this.ctmEnabled = config.ctmEnabled;
            this.ctmDebugLogging = config.ctmDebugLogging;
            this.duplicateTranslucentBackfaces =
                    config.duplicateTranslucentBackfaces;
            this.betterGrassMode = config.betterGrassMode;
            this.betterGrassIgnoreResourcePack =
                    config.betterGrassIgnoreResourcePack;
            this.betterGrassGrassBlock = config.betterGrassGrassBlock;
            this.betterGrassSnowyGrassBlock =
                    config.betterGrassSnowyGrassBlock;
            this.betterGrassDirtPath = config.betterGrassDirtPath;
            this.betterGrassFarmland = config.betterGrassFarmland;
            this.betterGrassMycelium = config.betterGrassMycelium;
            this.betterGrassPodzol = config.betterGrassPodzol;
            this.betterGrassCrimsonNylium = config.betterGrassCrimsonNylium;
            this.betterGrassWarpedNylium = config.betterGrassWarpedNylium;
            this.citEnabled = config.citEnabled;
            this.customGuiEnabled = config.customGuiEnabled;
            this.customColorsEnabled = config.customColorsEnabled;
            this.customSkyEnabled = config.customSkyEnabled;
            this.naturalTexturesEnabled = config.naturalTexturesEnabled;
            this.betterSnowEnabled = config.betterSnowEnabled;
            this.customAnimationsEnabled = config.customAnimationsEnabled;
            this.randomEntitiesEnabled = config.randomEntitiesEnabled;
            this.entityTexturesEnabled = config.entityTexturesEnabled;
            this.randomBlockEntityTextures = config.randomBlockEntityTextures;
            this.entityEmissiveTextures = config.entityEmissiveTextures;
            this.blockEntityEmissiveTextures =
                    config.blockEntityEmissiveTextures;
            this.entityTextureDebug = config.entityTextureDebug;
            this.customEntityModelsEnabled = config.customEntityModelsEnabled;
            this.customAnimationMipmapDistance =
                    config.customAnimationMipmapDistance;
            this.detailsSkyEnabled = config.detailsSkyEnabled;
            this.detailsSunEnabled = config.detailsSunEnabled;
            this.detailsMoonEnabled = config.detailsMoonEnabled;
            this.detailsStarsEnabled = config.detailsStarsEnabled;
            this.detailsCloudsEnabled = config.detailsCloudsEnabled;
            this.detailsCloudHeight = config.detailsCloudHeight;
            this.detailsRainSnowEnabled = config.detailsRainSnowEnabled;
            this.detailsVignetteEnabled = config.detailsVignetteEnabled;
            this.animationsEnabled = config.animationsEnabled;
            this.animationWater = config.animationWater;
            this.animationLava = config.animationLava;
            this.animationFire = config.animationFire;
            this.animationPortal = config.animationPortal;
            this.animationSculkSensor = config.animationSculkSensor;
            this.animationBlocks = config.animationBlocks;
            this.particlesEnabled = config.particlesEnabled;
            this.particleRainSplash = config.particleRainSplash;
            this.particleBlockBreak = config.particleBlockBreak;
            this.particleBlockBreaking = config.particleBlockBreaking;
            this.particleExplosion = config.particleExplosion;
            this.particleWater = config.particleWater;
            this.particleSmoke = config.particleSmoke;
            this.particlePotion = config.particlePotion;
            this.particlePortal = config.particlePortal;
            this.particleFlame = config.particleFlame;
            this.particleRedstone = config.particleRedstone;
            this.particleDripping = config.particleDripping;
            this.particleFirework = config.particleFirework;
            this.fogEnabled = config.fogEnabled;
            this.fogWater = config.fogWater;
            this.fogLava = config.fogLava;
            this.fogPowderSnow = config.fogPowderSnow;
            this.fogAir = config.fogAir;
            this.entityShadowsEnabled = config.entityShadowsEnabled;
            this.entityNameTagsEnabled = config.entityNameTagsEnabled;
            this.entityPlayerNameTags = config.entityPlayerNameTags;
            this.entityItemFrames = config.entityItemFrames;
            this.entityPaintings = config.entityPaintings;
            this.entityPistonAnimations = config.entityPistonAnimations;
            this.entityBeaconBeam = config.entityBeaconBeam;
            this.entityLimitBeaconBeamHeight =
                    config.entityLimitBeaconBeamHeight;
            this.entityEnchantingTableBook =
                    config.entityEnchantingTableBook;
            this.showFps = config.showFps;
            this.showFpsExtended = config.showFpsExtended;
            this.showCoords = config.showCoords;
            this.overlayCorner = config.overlayCorner;
            this.textContrast = config.textContrast;
            this.steadyDebugHud = config.steadyDebugHud;
            this.steadyDebugHudRefreshInterval =
                    config.steadyDebugHudRefreshInterval;
            this.toastAdvancement = config.toastAdvancement;
            this.toastRecipe = config.toastRecipe;
            this.toastSystem = config.toastSystem;
            this.toastTutorial = config.toastTutorial;
            this.instantSneak = config.instantSneak;
            this.fullscreenMode = config.fullscreenMode;
            this.biomeColorsEnabled = config.biomeColorsEnabled;
            this.skyColorsEnabled = config.skyColorsEnabled;
        }

        public Builder enabled(boolean value) { this.enabled = value; return this; }
        public Builder safeMode(boolean value) { this.safeMode = value; return this; }
        public Builder verifyMode(boolean value) { this.verifyMode = value; return this; }
        public Builder ctmEnabled(boolean value) { this.ctmEnabled = value; return this; }
        public Builder ctmDebugLogging(boolean value) { this.ctmDebugLogging = value; return this; }
        public Builder duplicateTranslucentBackfaces(boolean value) { this.duplicateTranslucentBackfaces = value; return this; }
        public Builder betterGrassMode(BetterGrassMode value) { this.betterGrassMode = value; return this; }
        public Builder betterGrassIgnoreResourcePack(boolean value) { this.betterGrassIgnoreResourcePack = value; return this; }
        public Builder betterGrassGrassBlock(boolean value) { this.betterGrassGrassBlock = value; return this; }
        public Builder betterGrassSnowyGrassBlock(boolean value) { this.betterGrassSnowyGrassBlock = value; return this; }
        public Builder betterGrassDirtPath(boolean value) { this.betterGrassDirtPath = value; return this; }
        public Builder betterGrassFarmland(boolean value) { this.betterGrassFarmland = value; return this; }
        public Builder betterGrassMycelium(boolean value) { this.betterGrassMycelium = value; return this; }
        public Builder betterGrassPodzol(boolean value) { this.betterGrassPodzol = value; return this; }
        public Builder betterGrassCrimsonNylium(boolean value) { this.betterGrassCrimsonNylium = value; return this; }
        public Builder betterGrassWarpedNylium(boolean value) { this.betterGrassWarpedNylium = value; return this; }
        public Builder citEnabled(boolean value) { this.citEnabled = value; return this; }
        public Builder customGuiEnabled(boolean value) { this.customGuiEnabled = value; return this; }
        public Builder customColorsEnabled(boolean value) { this.customColorsEnabled = value; return this; }
        public Builder customSkyEnabled(boolean value) { this.customSkyEnabled = value; return this; }
        public Builder naturalTexturesEnabled(boolean value) { this.naturalTexturesEnabled = value; return this; }
        public Builder betterSnowEnabled(boolean value) { this.betterSnowEnabled = value; return this; }
        public Builder customAnimationsEnabled(boolean value) { this.customAnimationsEnabled = value; return this; }
        public Builder randomEntitiesEnabled(boolean value) { this.randomEntitiesEnabled = value; return this; }
        public Builder entityTexturesEnabled(boolean value) { this.entityTexturesEnabled = value; return this; }
        public Builder randomBlockEntityTextures(boolean value) { this.randomBlockEntityTextures = value; return this; }
        public Builder entityEmissiveTextures(boolean value) { this.entityEmissiveTextures = value; return this; }
        public Builder blockEntityEmissiveTextures(boolean value) { this.blockEntityEmissiveTextures = value; return this; }
        public Builder entityTextureDebug(boolean value) { this.entityTextureDebug = value; return this; }
        public Builder customEntityModelsEnabled(boolean value) { this.customEntityModelsEnabled = value; return this; }
        public Builder customAnimationMipmapDistance(int value) { this.customAnimationMipmapDistance = value; return this; }
        public Builder detailsSkyEnabled(boolean value) { this.detailsSkyEnabled = value; return this; }
        public Builder detailsSunEnabled(boolean value) { this.detailsSunEnabled = value; return this; }
        public Builder detailsMoonEnabled(boolean value) { this.detailsMoonEnabled = value; return this; }
        public Builder detailsStarsEnabled(boolean value) { this.detailsStarsEnabled = value; return this; }
        public Builder detailsCloudsEnabled(boolean value) { this.detailsCloudsEnabled = value; return this; }
        public Builder detailsCloudHeight(int value) { this.detailsCloudHeight = value; return this; }
        public Builder detailsRainSnowEnabled(boolean value) { this.detailsRainSnowEnabled = value; return this; }
        public Builder detailsVignetteEnabled(boolean value) { this.detailsVignetteEnabled = value; return this; }
        public Builder animationsEnabled(boolean value) { this.animationsEnabled = value; return this; }
        public Builder animationWater(boolean value) { this.animationWater = value; return this; }
        public Builder animationLava(boolean value) { this.animationLava = value; return this; }
        public Builder animationFire(boolean value) { this.animationFire = value; return this; }
        public Builder animationPortal(boolean value) { this.animationPortal = value; return this; }
        public Builder animationSculkSensor(boolean value) { this.animationSculkSensor = value; return this; }
        public Builder animationBlocks(boolean value) { this.animationBlocks = value; return this; }
        public Builder particlesEnabled(boolean value) { this.particlesEnabled = value; return this; }
        public Builder particleRainSplash(boolean value) { this.particleRainSplash = value; return this; }
        public Builder particleBlockBreak(boolean value) { this.particleBlockBreak = value; return this; }
        public Builder particleBlockBreaking(boolean value) { this.particleBlockBreaking = value; return this; }
        public Builder particleExplosion(boolean value) { this.particleExplosion = value; return this; }
        public Builder particleWater(boolean value) { this.particleWater = value; return this; }
        public Builder particleSmoke(boolean value) { this.particleSmoke = value; return this; }
        public Builder particlePotion(boolean value) { this.particlePotion = value; return this; }
        public Builder particlePortal(boolean value) { this.particlePortal = value; return this; }
        public Builder particleFlame(boolean value) { this.particleFlame = value; return this; }
        public Builder particleRedstone(boolean value) { this.particleRedstone = value; return this; }
        public Builder particleDripping(boolean value) { this.particleDripping = value; return this; }
        public Builder particleFirework(boolean value) { this.particleFirework = value; return this; }
        public Builder fogEnabled(boolean value) { this.fogEnabled = value; return this; }
        public Builder fogWater(boolean value) { this.fogWater = value; return this; }
        public Builder fogLava(boolean value) { this.fogLava = value; return this; }
        public Builder fogPowderSnow(boolean value) { this.fogPowderSnow = value; return this; }
        public Builder fogAir(boolean value) { this.fogAir = value; return this; }
        public Builder entityShadowsEnabled(boolean value) { this.entityShadowsEnabled = value; return this; }
        public Builder entityNameTagsEnabled(boolean value) { this.entityNameTagsEnabled = value; return this; }
        public Builder entityPlayerNameTags(boolean value) { this.entityPlayerNameTags = value; return this; }
        public Builder entityItemFrames(boolean value) { this.entityItemFrames = value; return this; }
        public Builder entityPaintings(boolean value) { this.entityPaintings = value; return this; }
        public Builder entityPistonAnimations(boolean value) { this.entityPistonAnimations = value; return this; }
        public Builder entityBeaconBeam(boolean value) { this.entityBeaconBeam = value; return this; }
        public Builder entityLimitBeaconBeamHeight(boolean value) { this.entityLimitBeaconBeamHeight = value; return this; }
        public Builder entityEnchantingTableBook(boolean value) { this.entityEnchantingTableBook = value; return this; }
        public Builder showFps(boolean value) { this.showFps = value; return this; }
        public Builder showFpsExtended(boolean value) { this.showFpsExtended = value; return this; }
        public Builder showCoords(boolean value) { this.showCoords = value; return this; }
        public Builder overlayCorner(OverlayCorner value) { this.overlayCorner = value; return this; }
        public Builder textContrast(TextContrast value) { this.textContrast = value; return this; }
        public Builder steadyDebugHud(boolean value) { this.steadyDebugHud = value; return this; }
        public Builder steadyDebugHudRefreshInterval(int value) { this.steadyDebugHudRefreshInterval = value; return this; }
        public Builder toastAdvancement(boolean value) { this.toastAdvancement = value; return this; }
        public Builder toastRecipe(boolean value) { this.toastRecipe = value; return this; }
        public Builder toastSystem(boolean value) { this.toastSystem = value; return this; }
        public Builder toastTutorial(boolean value) { this.toastTutorial = value; return this; }
        public Builder instantSneak(boolean value) { this.instantSneak = value; return this; }
        public Builder fullscreenMode(FullscreenMode value) { this.fullscreenMode = value; return this; }
        public Builder biomeColorsEnabled(boolean value) { this.biomeColorsEnabled = value; return this; }
        public Builder skyColorsEnabled(boolean value) { this.skyColorsEnabled = value; return this; }

        public ArgusConfig build() {
            return new ArgusConfig(enabled, safeMode, verifyMode,
                    ctmEnabled, ctmDebugLogging,
                    duplicateTranslucentBackfaces, betterGrassMode,
                    betterGrassIgnoreResourcePack, betterGrassGrassBlock,
                    betterGrassSnowyGrassBlock, betterGrassDirtPath,
                    betterGrassFarmland, betterGrassMycelium,
                    betterGrassPodzol, betterGrassCrimsonNylium,
                    betterGrassWarpedNylium, citEnabled, customGuiEnabled,
                    customColorsEnabled, customSkyEnabled,
                    naturalTexturesEnabled, betterSnowEnabled,
                    customAnimationsEnabled, randomEntitiesEnabled,
                    entityTexturesEnabled, randomBlockEntityTextures,
                    entityEmissiveTextures, blockEntityEmissiveTextures,
                    entityTextureDebug,
                    customEntityModelsEnabled, customAnimationMipmapDistance,
                    detailsSkyEnabled, detailsSunEnabled, detailsMoonEnabled,
                    detailsStarsEnabled, detailsCloudsEnabled,
                    detailsCloudHeight, detailsRainSnowEnabled,
                    detailsVignetteEnabled, animationsEnabled,
                    animationWater, animationLava, animationFire,
                    animationPortal, animationSculkSensor, animationBlocks,
                    particlesEnabled, particleRainSplash, particleBlockBreak,
                    particleBlockBreaking, particleExplosion, particleWater,
                    particleSmoke, particlePotion, particlePortal,
                    particleFlame, particleRedstone, particleDripping,
                    particleFirework, fogEnabled,
                    fogWater, fogLava, fogPowderSnow, fogAir,
                    entityShadowsEnabled,
                    entityNameTagsEnabled, entityPlayerNameTags,
                    entityItemFrames, entityPaintings,
                    entityPistonAnimations, entityBeaconBeam,
                    entityLimitBeaconBeamHeight,
                    entityEnchantingTableBook, showFps, showFpsExtended,
                    showCoords, overlayCorner, textContrast, steadyDebugHud,
                    steadyDebugHudRefreshInterval, toastAdvancement,
                    toastRecipe, toastSystem, toastTutorial, instantSneak,
                    fullscreenMode, biomeColorsEnabled,
                    skyColorsEnabled);
        }
    }
}
