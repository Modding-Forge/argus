package com.argus.client.mixin;

import com.argus.fabric.randomentity.ArgusRandomEntityState;
import com.argus.fabric.randomentity.RandomEntityRuntime;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Target: {@code BlockEntityRenderDispatcher#tryExtractRenderState}.
 *
 * <p>Purpose: captures OptiFine Random Tile Entity facts exactly once during
 * render-state extraction and stores them on the per-block-entity state.
 *
 * <p>Preserved behavior: the vanilla renderer still creates and fills the
 * render state; Argus only attaches metadata after the original extraction.
 *
 * <p>Compatibility: non-required narrow wrap around the renderer extraction
 * call.
 *
 * <p>Risk: low.
 */
@Mixin(net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherRandomEntityMixin {
    @WrapOperation(method = "tryExtractRenderState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/blockentity/"
                            + "BlockEntityRenderer;extractRenderState"
                            + "(Lnet/minecraft/world/level/block/entity/"
                            + "BlockEntity;Lnet/minecraft/client/renderer/"
                            + "blockentity/state/BlockEntityRenderState;"
                            + "FLnet/minecraft/world/phys/Vec3;"
                            + "Lnet/minecraft/client/renderer/feature/"
                            + "ModelFeatureRenderer$CrumblingOverlay;)V"),
            require = 0)
    private <E extends BlockEntity, S extends BlockEntityRenderState>
    void argus$captureRandomBlockEntityContext(
            BlockEntityRenderer<E, S> renderer,
            E blockEntity,
            S state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress,
            Operation<Void> original) {
        original.call(renderer, blockEntity, state, partialTicks,
                cameraPosition, breakProgress);
        if (state instanceof ArgusRandomEntityState argusState) {
            argusState.argus$setRandomEntityContext(
                    RandomEntityRuntime.capture(blockEntity));
        }
    }
}
