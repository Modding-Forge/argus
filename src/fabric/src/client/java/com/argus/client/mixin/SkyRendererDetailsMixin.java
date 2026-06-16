package com.argus.client.mixin;

import com.argus.config.ArgusConfig;
import com.argus.config.ArgusConfigHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Target: {@link SkyRenderer} detail rendering methods.
 *
 * <p>Purpose: implements Argus's Sodium-Extra-style sky detail toggles for
 * sky disc, End sky, sun, moon, stars and sunrise/sunset. Preserved behaviour:
 * every method runs vanilla unchanged when the relevant toggle is enabled.
 *
 * <p>Risk: low-medium. The hooks cancel small leaf render methods and do not
 * alter FrameGraph setup or world state.
 */
@Mixin(SkyRenderer.class)
public abstract class SkyRendererDetailsMixin {

    @Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true,
            require = 0)
    private void argus$hideSkyDisc(int skyColor, CallbackInfo ci) {
        if (!ArgusConfigHolder.get().detailsSkyEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderEndSky", at = @At("HEAD"), cancellable = true,
            require = 0)
    private void argus$hideEndSky(CallbackInfo ci) {
        if (!ArgusConfigHolder.get().detailsSkyEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSun", at = @At("HEAD"), cancellable = true,
            require = 0)
    private void argus$hideSun(float rainBrightness,
                                PoseStack poseStack,
                                CallbackInfo ci) {
        ArgusConfig cfg = ArgusConfigHolder.get();
        if (!cfg.detailsSkyEnabled() || !cfg.detailsSunEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true,
            require = 0)
    private void argus$hideMoon(MoonPhase moonPhase,
                                 float rainBrightness,
                                 PoseStack poseStack,
                                 CallbackInfo ci) {
        ArgusConfig cfg = ArgusConfigHolder.get();
        if (!cfg.detailsSkyEnabled() || !cfg.detailsMoonEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStars", at = @At("HEAD"), cancellable = true,
            require = 0)
    private void argus$hideStars(float starBrightness,
                                  PoseStack poseStack,
                                  CallbackInfo ci) {
        ArgusConfig cfg = ArgusConfigHolder.get();
        if (!cfg.detailsSkyEnabled() || !cfg.detailsStarsEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSunriseAndSunset", at = @At("HEAD"),
            cancellable = true, require = 0)
    private void argus$hideSunrise(PoseStack poseStack,
                                    float sunAngle,
                                    int sunriseAndSunsetColor,
                                    CallbackInfo ci) {
        if (!ArgusConfigHolder.get().detailsSkyEnabled()) {
            ci.cancel();
        }
    }
}
