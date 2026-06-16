package com.argus.client.mixin;

import com.argus.fabric.customcolors.CustomColorsRuntime;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Target: {@link ContextualBar#extractExperienceLevel}.
 *
 * <p>Purpose: applies {@code text.xpbar} to the player's level number while
 * preserving vanilla outline rendering.
 *
 * <p>Risk: low. Static GUI text color constant only.
 */
@Mixin(ContextualBar.class)
public interface ContextualBarCustomColorsMixin {

    @ModifyConstant(method = "extractExperienceLevel",
            constant = @Constant(intValue = -8323296))
    private static int argus$customXpBarText(int color) {
        return CustomColorsRuntime.overrideArgb("text.xpbar", color);
    }
}
