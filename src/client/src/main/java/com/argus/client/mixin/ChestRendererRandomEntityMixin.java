package com.argus.client.mixin;

import com.argus.client.randomentity.ArgusRandomEntityState;
import com.argus.client.randomentity.RandomEntityRuntime;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Target: {@link ChestRenderer#submit}.
 *
 * <p>Purpose: remaps chest atlas sprites through Argus's OptiFine Random
 * Tile Entity snapshot while preserving vanilla chest model, animation,
 * lighting, Christmas selection, copper states and double-chest handling.
 *
 * <p>Compatibility: non-required expression hook around
 * {@link Sheets#chooseSprite}.
 *
 * <p>Risk: low. Only the selected sprite id changes.
 */
@Mixin(ChestRenderer.class)
public abstract class ChestRendererRandomEntityMixin {
    @ModifyExpressionValue(method = "submit",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/Sheets;"
                            + "chooseSprite"
                            + "(Lnet/minecraft/client/renderer/"
                            + "blockentity/state/ChestRenderState$"
                            + "ChestMaterialType;"
                            + "Lnet/minecraft/world/level/block/state/"
                            + "properties/ChestType;)"
                            + "Lnet/minecraft/client/resources/model/"
                            + "sprite/SpriteId;"),
            require = 0)
    private SpriteId argus$randomChestSprite(
            SpriteId original,
            ChestRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        if (!(state instanceof ArgusRandomEntityState argusState)) {
            return original;
        }
        return RandomEntityRuntime.remapSprite(original,
                argusState.argus$getRandomEntityContext(), true);
    }
}
