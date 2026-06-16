package com.argus.client.mixin;

import com.argus.config.ArgusConfig;
import com.argus.config.ArgusConfigHolder;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Target: {@link SpriteContents#createAnimationState}.
 *
 * <p>Purpose: implements Argus's atlas animation toggles before animated
 * sprites allocate their animation state. Custom animation uploads are gated
 * separately through {@link ArgusConfig#customAnimationsActive()}.
 *
 * <p>Risk: medium-low. The hook only returns {@code null} for disabled sprite
 * categories, matching the vanilla representation of a non-animated sprite.
 */
@Mixin(SpriteContents.class)
public abstract class SpriteContentsAnimationDetailsMixin {

    @Shadow
    @Final
    private Identifier name;

    @Inject(method = "createAnimationState", at = @At("HEAD"),
            cancellable = true, require = 0)
    private void argus$disableAnimation(GpuBufferSlice uboSlice,
                                         int spriteUboSize,
                                         CallbackInfoReturnable<SpriteContents.AnimationState> cir) {
        if (!shouldAnimate(this.name, ArgusConfigHolder.get())) {
            cir.setReturnValue(null);
        }
    }

    private static boolean shouldAnimate(Identifier id, ArgusConfig cfg) {
        if (!cfg.enabled() || !cfg.animationsEnabled()) {
            return false;
        }
        String path = id == null ? "" : id.getPath();
        if (path.contains("water")) {
            return cfg.animationWater();
        }
        if (path.contains("lava")) {
            return cfg.animationLava();
        }
        if (path.contains("fire")) {
            return cfg.animationFire();
        }
        if (path.contains("portal")) {
            return cfg.animationPortal();
        }
        if (path.contains("sculk_sensor")) {
            return cfg.animationSculkSensor();
        }
        return cfg.animationBlocks();
    }
}
