package com.argus.client.mixin;

import com.argus.config.ArgusConfig;
import com.argus.config.ArgusConfigHolder;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Target: {@link LevelRenderer#addCloudsPass}.
 *
 * <p>Purpose: implements Argus's cloud-height control while preserving
 * Sodium's vanilla cloud visibility option as the only cloud on/off switch.
 *
 * <p>Risk: medium. The hook touches the cloud pass only and fails safe through
 * {@code require = 0} if Mojang changes the private method signature.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererDetailsMixin {

    @ModifyVariable(method = "addCloudsPass", at = @At("HEAD"),
            argsOnly = true, ordinal = 1, require = 0)
    private float argus$cloudHeight(float cloudHeight) {
        ArgusConfig cfg = ArgusConfigHolder.get();
        return cfg.enabled()
                ? (float) cfg.detailsCloudHeight()
                : cloudHeight;
    }
}
