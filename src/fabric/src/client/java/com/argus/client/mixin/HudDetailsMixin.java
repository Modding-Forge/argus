package com.argus.client.mixin;

import com.argus.client.hud.ArgusHudOverlay;
import com.argus.config.ArgusConfigHolder;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Target: {@link Hud#extractRenderState}.
 *
 * <p>Purpose: extracts Argus's HUD overlay. Vanilla/Sodium remain the owners
 * of the built-in vignette option to avoid duplicate settings.
 *
 * <p>Risk: low. Cancels only vignette extraction.
 */
@Mixin(Hud.class)
public abstract class HudDetailsMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"), require = 0)
    private void argus$extractOverlay(GuiGraphicsExtractor graphics,
                                       DeltaTracker deltaTracker,
                                       CallbackInfo ci) {
        ArgusHudOverlay.extract(graphics, Minecraft.getInstance(),
                ArgusConfigHolder.get());
    }
}
