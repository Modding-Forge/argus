package com.argus.client.mixin;

import com.argus.client.randomentity.ArgusRandomEntityState;
import com.argus.client.randomentity.RandomEntityRuntime;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.blockentity.state.ShulkerBoxRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Target: {@link ShulkerBoxRenderer#submit}.
 *
 * <p>Purpose: remaps the final shulker sprite id for OptiFine Random Tile
 * Entity variants while preserving vanilla color selection, lid animation,
 * facing transform, lighting and break overlay behavior.
 *
 * <p>Compatibility: non-required wrap around the private submit helper.
 *
 * <p>Risk: low. Only the sprite id argument is replaced.
 */
@Mixin(ShulkerBoxRenderer.class)
public abstract class ShulkerBoxRendererRandomEntityMixin {
    @WrapOperation(method = "submit(Lnet/minecraft/client/renderer/"
            + "blockentity/state/ShulkerBoxRenderState;"
            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
            + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
            + "Lnet/minecraft/client/renderer/state/level/"
            + "CameraRenderState;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/blockentity/"
                            + "ShulkerBoxRenderer;submit"
                            + "(Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/"
                            + "SubmitNodeCollector;IILnet/minecraft/core/"
                            + "Direction;FLnet/minecraft/client/renderer/"
                            + "feature/ModelFeatureRenderer$"
                            + "CrumblingOverlay;"
                            + "Lnet/minecraft/client/resources/model/"
                            + "sprite/SpriteId;I)V"),
            require = 0)
    private void argus$randomShulkerSprite(
            ShulkerBoxRenderer renderer,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            int overlayCoords,
            Direction direction,
            float progress,
            ModelFeatureRenderer.CrumblingOverlay breakProgress,
            SpriteId sprite,
            int outlineColor,
            Operation<Void> original,
            ShulkerBoxRenderState state,
            PoseStack originalPoseStack,
            SubmitNodeCollector originalSubmitNodeCollector,
            CameraRenderState camera) {
        SpriteId remapped = sprite;
        if (state instanceof ArgusRandomEntityState argusState) {
            remapped = RandomEntityRuntime.remapSprite(sprite,
                    argusState.argus$getRandomEntityContext(), true);
        }
        original.call(renderer, poseStack, submitNodeCollector, lightCoords,
                overlayCoords, direction, progress, breakProgress, remapped,
                outlineColor);
    }
}
