package com.argus.config;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArgusConfigLoaderTest {

    @Test
    void parse_emptyFile_returnsDefaults() {
        ArgusConfig cfg = ArgusConfigLoader.load(new StringReader(""));
        assertEquals(ArgusConfigDefaults.ENABLED, cfg.enabled());
        assertEquals(ArgusConfigDefaults.SAFE_MODE, cfg.safeMode());
        assertEquals(ArgusConfigDefaults.VERIFY_MODE, cfg.verifyMode());
        assertEquals(ArgusConfigDefaults.CTM_ENABLED, cfg.ctmEnabled());
        assertEquals(ArgusConfigDefaults.CTM_DEBUG_LOGGING,
                cfg.ctmDebugLogging());
        assertEquals(ArgusConfigDefaults.DUPLICATE_TRANSLUCENT_BACKFACES,
                cfg.duplicateTranslucentBackfaces());
        assertEquals(ArgusConfigDefaults.BETTER_GRASS_MODE,
                cfg.betterGrassMode());
        assertEquals(ArgusConfigDefaults.BETTER_GRASS_IGNORE_RESOURCE_PACK,
                cfg.betterGrassIgnoreResourcePack());
        assertEquals(ArgusConfigDefaults.BETTER_GRASS_GRASS_BLOCK,
                cfg.betterGrassGrassBlock());
        assertEquals(ArgusConfigDefaults.BETTER_GRASS_SNOWY_GRASS_BLOCK,
                cfg.betterGrassSnowyGrassBlock());
        assertEquals(ArgusConfigDefaults.BETTER_GRASS_DIRT_PATH,
                cfg.betterGrassDirtPath());
        assertEquals(ArgusConfigDefaults.BETTER_GRASS_FARMLAND,
                cfg.betterGrassFarmland());
        assertEquals(ArgusConfigDefaults.BETTER_GRASS_MYCELIUM,
                cfg.betterGrassMycelium());
        assertEquals(ArgusConfigDefaults.BETTER_GRASS_PODZOL,
                cfg.betterGrassPodzol());
        assertEquals(ArgusConfigDefaults.BETTER_GRASS_CRIMSON_NYLIUM,
                cfg.betterGrassCrimsonNylium());
        assertEquals(ArgusConfigDefaults.BETTER_GRASS_WARPED_NYLIUM,
                cfg.betterGrassWarpedNylium());
        assertEquals(ArgusConfigDefaults.NATURAL_TEXTURES_ENABLED,
                cfg.naturalTexturesEnabled());
        assertEquals(ArgusConfigDefaults.BETTER_SNOW_ENABLED,
                cfg.betterSnowEnabled());
        assertEquals(ArgusConfigDefaults.CUSTOM_ANIMATIONS_ENABLED,
                cfg.customAnimationsEnabled());
        assertEquals(ArgusConfigDefaults.ENTITY_TEXTURES_ENABLED,
                cfg.entityTexturesEnabled());
        assertEquals(ArgusConfigDefaults.RANDOM_BLOCK_ENTITY_TEXTURES,
                cfg.randomBlockEntityTextures());
        assertEquals(ArgusConfigDefaults.ENTITY_EMISSIVE_TEXTURES,
                cfg.entityEmissiveTextures());
        assertEquals(ArgusConfigDefaults.BLOCK_ENTITY_EMISSIVE_TEXTURES,
                cfg.blockEntityEmissiveTextures());
        assertEquals(ArgusConfigDefaults.CUSTOM_ANIMATION_MIPMAP_DISTANCE,
                cfg.customAnimationMipmapDistance());
        assertEquals(ArgusConfigDefaults.DETAILS_SKY_ENABLED,
                cfg.detailsSkyEnabled());
        assertEquals(ArgusConfigDefaults.DETAILS_SUN_ENABLED,
                cfg.detailsSunEnabled());
        assertEquals(ArgusConfigDefaults.DETAILS_MOON_ENABLED,
                cfg.detailsMoonEnabled());
        assertEquals(ArgusConfigDefaults.DETAILS_STARS_ENABLED,
                cfg.detailsStarsEnabled());
        assertEquals(ArgusConfigDefaults.DETAILS_CLOUDS_ENABLED,
                cfg.detailsCloudsEnabled());
        assertEquals(ArgusConfigDefaults.DETAILS_CLOUD_HEIGHT,
                cfg.detailsCloudHeight());
        assertEquals(ArgusConfigDefaults.ANIMATIONS_ENABLED,
                cfg.animationsEnabled());
        assertEquals(ArgusConfigDefaults.PARTICLES_ENABLED,
                cfg.particlesEnabled());
        assertEquals(ArgusConfigDefaults.PARTICLE_RAIN_SPLASH,
                cfg.particleRainSplash());
        assertEquals(ArgusConfigDefaults.PARTICLE_BLOCK_BREAK,
                cfg.particleBlockBreak());
        assertEquals(ArgusConfigDefaults.PARTICLE_BLOCK_BREAKING,
                cfg.particleBlockBreaking());
        assertEquals(ArgusConfigDefaults.PARTICLE_EXPLOSION,
                cfg.particleExplosion());
        assertEquals(ArgusConfigDefaults.PARTICLE_WATER,
                cfg.particleWater());
        assertEquals(ArgusConfigDefaults.PARTICLE_SMOKE,
                cfg.particleSmoke());
        assertEquals(ArgusConfigDefaults.PARTICLE_POTION,
                cfg.particlePotion());
        assertEquals(ArgusConfigDefaults.PARTICLE_PORTAL,
                cfg.particlePortal());
        assertEquals(ArgusConfigDefaults.PARTICLE_FLAME,
                cfg.particleFlame());
        assertEquals(ArgusConfigDefaults.PARTICLE_REDSTONE,
                cfg.particleRedstone());
        assertEquals(ArgusConfigDefaults.PARTICLE_DRIPPING,
                cfg.particleDripping());
        assertEquals(ArgusConfigDefaults.PARTICLE_FIREWORK,
                cfg.particleFirework());
        assertEquals(ArgusConfigDefaults.FOG_ENABLED, cfg.fogEnabled());
        assertEquals(ArgusConfigDefaults.FOG_WATER, cfg.fogWater());
        assertEquals(ArgusConfigDefaults.FOG_LAVA, cfg.fogLava());
        assertEquals(ArgusConfigDefaults.FOG_POWDER_SNOW,
                cfg.fogPowderSnow());
        assertEquals(ArgusConfigDefaults.FOG_AIR, cfg.fogAir());
        assertEquals(ArgusConfigDefaults.ENTITY_SHADOWS_ENABLED,
                cfg.entityShadowsEnabled());
        assertEquals(ArgusConfigDefaults.ENTITY_PLAYER_NAME_TAGS,
                cfg.entityPlayerNameTags());
        assertEquals(ArgusConfigDefaults.ENTITY_ITEM_FRAMES,
                cfg.entityItemFrames());
        assertEquals(ArgusConfigDefaults.ENTITY_PAINTINGS,
                cfg.entityPaintings());
        assertEquals(ArgusConfigDefaults.ENTITY_PISTON_ANIMATIONS,
                cfg.entityPistonAnimations());
        assertEquals(ArgusConfigDefaults.ENTITY_BEACON_BEAM,
                cfg.entityBeaconBeam());
        assertEquals(ArgusConfigDefaults.ENTITY_ENCHANTING_TABLE_BOOK,
                cfg.entityEnchantingTableBook());
        assertEquals(ArgusConfigDefaults.SHOW_FPS, cfg.showFps());
        assertEquals(ArgusConfigDefaults.OVERLAY_CORNER,
                cfg.overlayCorner());
        assertEquals(ArgusConfigDefaults.TEXT_CONTRAST,
                cfg.textContrast());
        assertEquals(ArgusConfigDefaults.TOAST_SYSTEM,
                cfg.toastSystem());
        assertEquals(ArgusConfigDefaults.BIOME_COLORS_ENABLED,
                cfg.biomeColorsEnabled());
    }

    @Test
    void parse_booleanTrue_overridesDefault() {
        String body = "argus.enabled = true\n"
                + "argus.safe_mode = true\n"
                + "argus.verify_mode = true\n"
                + "argus.ctm.enabled = false\n"
                + "argus.ctm.debug_logging = true\n"
                + "argus.general.duplicate_translucent_backfaces = true\n"
                + "argus.better_grass.mode = fancy\n"
                + "argus.better_grass.ignore_resource_pack = true\n"
                + "argus.better_grass.grass_block = false\n"
                + "argus.better_grass.snowy_grass_block = false\n"
                + "argus.better_grass.dirt_path = false\n"
                + "argus.better_grass.farmland = false\n"
                + "argus.better_grass.mycelium = false\n"
                + "argus.better_grass.podzol = false\n"
                + "argus.better_grass.crimson_nylium = false\n"
                + "argus.better_grass.warped_nylium = false\n"
                + "argus.natural_textures.enabled = false\n"
                + "argus.better_snow.enabled = false\n"
                + "argus.custom_animations.enabled = false\n"
                + "argus.entity_textures.enabled = false\n"
                + "argus.entity_textures.random_entities = false\n"
                + "argus.entity_textures.random_block_entities = false\n"
                + "argus.entity_textures.emissive_entities = false\n"
                + "argus.entity_textures.emissive_block_entities = false\n"
                + "argus.entity_textures.debug = true\n"
                + "argus.custom_animations.mipmap_distance = 2\n"
                + "argus.details.sky.enabled = false\n"
                + "argus.details.cloud_height = 240\n"
                + "argus.animations.enabled = false\n"
                + "argus.animations.water = false\n"
                + "argus.particles.enabled = false\n"
                + "argus.particles.rain_splash = false\n"
                + "argus.particles.block_break = false\n"
                + "argus.particles.block_breaking = false\n"
                + "argus.particles.explosion = false\n"
                + "argus.particles.water = false\n"
                + "argus.particles.smoke = false\n"
                + "argus.particles.potion = false\n"
                + "argus.particles.portal = false\n"
                + "argus.particles.flame = false\n"
                + "argus.particles.redstone = false\n"
                + "argus.particles.dripping = false\n"
                + "argus.particles.firework = false\n"
                + "argus.fog.enabled = false\n"
                + "argus.fog.water = false\n"
                + "argus.fog.lava = false\n"
                + "argus.fog.powder_snow = false\n"
                + "argus.fog.air = false\n"
                + "argus.entities.shadows.enabled = false\n"
                + "argus.entities.player_name_tags = false\n"
                + "argus.entities.item_frames = false\n"
                + "argus.entities.paintings = false\n"
                + "argus.entities.piston_animations = false\n"
                + "argus.entities.beacon_beam = false\n"
                + "argus.entities.enchanting_table_book = false\n"
                + "argus.hud.fps = true\n"
                + "argus.hud.fps_extended = true\n"
                + "argus.hud.coords = true\n"
                + "argus.hud.corner = bottom_right\n"
                + "argus.hud.text_contrast = backdrop\n"
                + "argus.hud.steady_debug = true\n"
                + "argus.hud.steady_debug_refresh_interval = 20\n"
                + "argus.toasts.advancement = false\n"
                + "argus.toasts.recipe = false\n"
                + "argus.toasts.system = false\n"
                + "argus.toasts.tutorial = false\n"
                + "argus.extras.instant_sneak = true\n"
                + "argus.extras.fullscreen_mode = borderless\n"
                + "argus.colors.biome.enabled = false\n";
        ArgusConfig cfg = ArgusConfigLoader.load(new StringReader(body));
        assertTrue(cfg.enabled());
        assertTrue(cfg.safeMode());
        assertTrue(cfg.verifyMode());
        assertFalse(cfg.ctmEnabled());
        assertTrue(cfg.ctmDebugLogging());
        assertTrue(cfg.duplicateTranslucentBackfaces());
        assertEquals(BetterGrassMode.FANCY, cfg.betterGrassMode());
        assertTrue(cfg.betterGrassIgnoreResourcePack());
        assertFalse(cfg.betterGrassGrassBlock());
        assertFalse(cfg.betterGrassSnowyGrassBlock());
        assertFalse(cfg.betterGrassDirtPath());
        assertFalse(cfg.betterGrassFarmland());
        assertFalse(cfg.betterGrassMycelium());
        assertFalse(cfg.betterGrassPodzol());
        assertFalse(cfg.betterGrassCrimsonNylium());
        assertFalse(cfg.betterGrassWarpedNylium());
        assertFalse(cfg.naturalTexturesEnabled());
        assertFalse(cfg.betterSnowEnabled());
        assertFalse(cfg.customAnimationsEnabled());
        assertFalse(cfg.entityTexturesEnabled());
        assertFalse(cfg.randomEntitiesEnabled());
        assertFalse(cfg.randomBlockEntityTextures());
        assertFalse(cfg.entityEmissiveTextures());
        assertFalse(cfg.blockEntityEmissiveTextures());
        assertTrue(cfg.entityTextureDebug());
        assertEquals(2, cfg.customAnimationMipmapDistance());
        assertFalse(cfg.detailsSkyEnabled());
        assertEquals(240, cfg.detailsCloudHeight());
        assertFalse(cfg.animationsEnabled());
        assertFalse(cfg.animationWater());
        assertFalse(cfg.particlesEnabled());
        assertFalse(cfg.particleRainSplash());
        assertFalse(cfg.particleBlockBreak());
        assertFalse(cfg.particleBlockBreaking());
        assertFalse(cfg.particleExplosion());
        assertFalse(cfg.particleWater());
        assertFalse(cfg.particleSmoke());
        assertFalse(cfg.particlePotion());
        assertFalse(cfg.particlePortal());
        assertFalse(cfg.particleFlame());
        assertFalse(cfg.particleRedstone());
        assertFalse(cfg.particleDripping());
        assertFalse(cfg.particleFirework());
        assertFalse(cfg.fogEnabled());
        assertFalse(cfg.fogWater());
        assertFalse(cfg.fogLava());
        assertFalse(cfg.fogPowderSnow());
        assertFalse(cfg.fogAir());
        assertFalse(cfg.entityShadowsEnabled());
        assertFalse(cfg.entityPlayerNameTags());
        assertFalse(cfg.entityItemFrames());
        assertFalse(cfg.entityPaintings());
        assertFalse(cfg.entityPistonAnimations());
        assertFalse(cfg.entityBeaconBeam());
        assertFalse(cfg.entityEnchantingTableBook());
        assertTrue(cfg.showFps());
        assertTrue(cfg.showFpsExtended());
        assertTrue(cfg.showCoords());
        assertEquals(OverlayCorner.BOTTOM_RIGHT, cfg.overlayCorner());
        assertEquals(TextContrast.BACKDROP, cfg.textContrast());
        assertTrue(cfg.steadyDebugHud());
        assertEquals(20, cfg.steadyDebugHudRefreshInterval());
        assertFalse(cfg.toastAdvancement());
        assertFalse(cfg.toastRecipe());
        assertFalse(cfg.toastSystem());
        assertFalse(cfg.toastTutorial());
        assertTrue(cfg.instantSneak());
        assertEquals(FullscreenMode.BORDERLESS, cfg.fullscreenMode());
        assertFalse(cfg.biomeColorsEnabled());
    }

    @Test
    void parse_booleanFalse_overridesDefault() {
        String body = "argus.enabled = false\n"
                + "argus.safe_mode = true\n";
        ArgusConfig cfg = ArgusConfigLoader.load(new StringReader(body));
        assertFalse(cfg.enabled());
        assertTrue(cfg.safeMode());
        // Unset keys keep their defaults.
        assertEquals(ArgusConfigDefaults.VERIFY_MODE, cfg.verifyMode());
        assertEquals(ArgusConfigDefaults.CTM_ENABLED, cfg.ctmEnabled());
        assertEquals(ArgusConfigDefaults.CTM_DEBUG_LOGGING,
                cfg.ctmDebugLogging());
        assertEquals(ArgusConfigDefaults.DUPLICATE_TRANSLUCENT_BACKFACES,
                cfg.duplicateTranslucentBackfaces());
        assertEquals(ArgusConfigDefaults.BETTER_GRASS_MODE,
                cfg.betterGrassMode());
    }

    @Test
    void parse_unknownKey_isIgnored() {
        String body = "argus.unknown = true\n"
                + "argus.enabled = false\n";
        ArgusConfig cfg = ArgusConfigLoader.load(new StringReader(body));
        assertFalse(cfg.enabled());
    }

    @Test
    void parse_malformedBoolean_usesDefault() {
        // "yes" is not recognised; the loader falls back to the
        // default for the argus.enabled key, and the other
        // keys (missing) keep their defaults too.
        String body = "argus.enabled = yes\n";
        ArgusConfig cfg = ArgusConfigLoader.load(new StringReader(body));
        assertEquals(ArgusConfigDefaults.ENABLED, cfg.enabled());
    }

    @Test
    void parse_numericBooleans_areAccepted() {
        // "0" and "1" are common synonyms for false and true.
        String body = "argus.enabled = 0\n"
                + "argus.safe_mode = 1\n";
        ArgusConfig cfg = ArgusConfigLoader.load(new StringReader(body));
        assertFalse(cfg.enabled());
        assertTrue(cfg.safeMode());
    }

    @Test
    void parse_stream_utf8() {
        // Test the InputStream overload, which is the path the
        // Fabric adapter uses for file I/O.
        String body = "argus.enabled = false\n"
                + "argus.verify_mode = true\n";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ArgusConfig cfg = ArgusConfigLoader.load(
                new ByteArrayInputStream(bytes));
        assertFalse(cfg.enabled());
        assertTrue(cfg.verifyMode());
    }

    @Test
    void ctmActive_requiresBothMasterAndFeature() {
        ArgusConfig on = new ArgusConfig(true, false, false, true, false,
                BetterGrassMode.FAST);
        assertTrue(on.ctmActive());
        ArgusConfig featureOff = new ArgusConfig(true, false, false, false,
                false, BetterGrassMode.FAST);
        assertFalse(featureOff.ctmActive());
        ArgusConfig masterOff = new ArgusConfig(false, false, false, true,
                false, BetterGrassMode.FAST);
        assertFalse(masterOff.ctmActive());
    }

    @Test
    void parse_malformedMipmapDistance_usesDefault() {
        String body = "argus.custom_animations.mipmap_distance = 9\n";
        ArgusConfig cfg = ArgusConfigLoader.load(new StringReader(body));
        assertEquals(ArgusConfigDefaults.CUSTOM_ANIMATION_MIPMAP_DISTANCE,
                cfg.customAnimationMipmapDistance());
    }

    @Test
    void parse_malformedCloudHeight_usesDefault() {
        String body = "argus.details.cloud_height = 999\n";
        ArgusConfig cfg = ArgusConfigLoader.load(new StringReader(body));
        assertEquals(ArgusConfigDefaults.DETAILS_CLOUD_HEIGHT,
                cfg.detailsCloudHeight());
    }

    @Test
    void builder_preservesUnchangedFields() {
        ArgusConfig base = ArgusConfigDefaults.defaults();
        ArgusConfig changed = base.toBuilder()
                .detailsSkyEnabled(false)
                .entityTexturesEnabled(false)
                .animationWater(false)
                .particleFirework(false)
                .overlayCorner(OverlayCorner.BOTTOM_LEFT)
                .build();
        assertFalse(changed.detailsSkyEnabled());
        assertFalse(changed.entityTexturesEnabled());
        assertFalse(changed.animationWater());
        assertFalse(changed.particleFirework());
        assertEquals(OverlayCorner.BOTTOM_LEFT, changed.overlayCorner());
        assertEquals(base.ctmEnabled(), changed.ctmEnabled());
        assertEquals(base.betterGrassMode(), changed.betterGrassMode());
        assertEquals(base.customAnimationMipmapDistance(),
                changed.customAnimationMipmapDistance());
    }

    @Test
    void randomEntitiesActive_requiresEntityTextureMaster() {
        ArgusConfig disabled = ArgusConfigDefaults.defaults().toBuilder()
                .entityTexturesEnabled(false)
                .randomEntitiesEnabled(true)
                .build();
        assertFalse(disabled.randomEntitiesActive());
        ArgusConfig enabled = ArgusConfigDefaults.defaults().toBuilder()
                .entityTexturesEnabled(true)
                .randomEntitiesEnabled(true)
                .build();
        assertTrue(enabled.randomEntitiesActive());
    }

    @Test
    void betterGrassActive_requiresMasterAndNonOffMode() {
        ArgusConfig fast = new ArgusConfig(true, false, false, true, false,
                BetterGrassMode.FAST);
        assertTrue(fast.betterGrassActive());
        ArgusConfig off = new ArgusConfig(true, false, false, true, false,
                BetterGrassMode.OFF);
        assertFalse(off.betterGrassActive());
        ArgusConfig masterOff = new ArgusConfig(false, false, false, true,
                false, BetterGrassMode.FAST);
        assertFalse(masterOff.betterGrassActive());
    }

    @Test
    void anyBetterGrassBlockEnabled_tracksFamilyToggles() {
        ArgusConfig allOff = new ArgusConfig(true, false, false, true,
                false, BetterGrassMode.FAST, false, false, false, false,
                false, false, false, false);
        assertFalse(allOff.anyBetterGrassBlockEnabled());
        ArgusConfig oneOn = new ArgusConfig(true, false, false, true,
                false, BetterGrassMode.FAST, false, false, false, true,
                false, false, false, false);
        assertTrue(oneOn.anyBetterGrassBlockEnabled());
    }
}
