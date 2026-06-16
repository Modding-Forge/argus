package com.argus.client.mixin;

import com.argus.config.ArgusConfigHolder;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Target: {@link BeaconRenderer#submit}.
 *
 * <p>Purpose: gates beacon beam render submission. The height-limit option is
 * exposed separately and currently only documents the preferred conservative
 * policy; full section clamping will remain fail-safe until Mojang's beam
 * state layout is stable.
 *
 * <p>Risk: low. Cancels only render submission.
 */
@Mixin(BeaconRenderer.class)
public abstract class BeaconRendererDetailsMixin {
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true,
            require = 0)
    private void argus$hideBeaconBeam(CallbackInfo ci) {
        if (!ArgusConfigHolder.get().entityBeaconBeam()) {
            ci.cancel();
        }
    }
}
