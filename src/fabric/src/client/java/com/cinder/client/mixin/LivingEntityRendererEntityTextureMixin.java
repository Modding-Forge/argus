package com.cinder.client.mixin;

import com.cinder.fabric.randomentity.CinderRandomEntityState;
import com.cinder.fabric.randomentity.RandomEntityRuntime;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Target: {@link LivingEntityRenderer#submit}.
 *
 * <p>Purpose: emits Cinder's OptiFine-style emissive companion texture as a
 * second fullbright model pass after vanilla submits the base living-entity
 * model.
 *
 * <p>Preserved behavior: vanilla model submission, layer ordering, visibility,
 * outline handling, lighting, and animation setup remain unchanged. Cinder only
 * adds a pass when a matching companion texture exists in the immutable entity
 * texture snapshot.
 *
 * <p>Compatibility: {@code require = 0} makes the hook fail safe if Mojang
 * reshapes the renderer submit method.
 *
 * <p>Risk: medium; the hook sits in the common living-entity submit path, but
 * performs only config/snapshot gates and one optional additional submission.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererEntityTextureMixin<
        S extends LivingEntityRenderState> {
    private static final int CINDER_FULL_BRIGHT_LIGHT = 0x00F000F0;

    @Shadow
    protected EntityModel<? super S> model;

    @Shadow
    public abstract Identifier getTextureLocation(S state);

    @Inject(method = "submit",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/"
                            + "SubmitNodeCollector;submitModel"
                            + "(Lnet/minecraft/client/model/Model;"
                            + "Ljava/lang/Object;"
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/rendertype/"
                            + "RenderType;IIILnet/minecraft/client/"
                            + "renderer/texture/TextureAtlasSprite;I"
                            + "Lnet/minecraft/client/renderer/feature/"
                            + "ModelFeatureRenderer$CrumblingOverlay;)V",
                    shift = At.Shift.AFTER),
            require = 0)
    private void cinder$submitEntityEmissive(S state,
                                             PoseStack poseStack,
                                             SubmitNodeCollector nodes,
                                             CameraRenderState cameraState,
                                             CallbackInfo ci) {
        Identifier texture = cinder$currentEntityTexture(state);
        Identifier emissive = RandomEntityRuntime.emissiveTexture(texture);
        if (emissive == null) {
            return;
        }
        nodes.submitModel(this.model, state, poseStack,
                RenderTypes.entityTranslucentEmissive(emissive),
                CINDER_FULL_BRIGHT_LIGHT, OverlayTexture.NO_OVERLAY,
                -1, null, state.outlineColor, null);
    }

    private Identifier cinder$currentEntityTexture(S state) {
        Identifier base = getTextureLocation(state);
        if (state instanceof CinderRandomEntityState cinderState) {
            Identifier random = RandomEntityRuntime.remap(base,
                    cinderState.cinder$getRandomEntityVariantIndex());
            return random;
        }
        return base;
    }
}
