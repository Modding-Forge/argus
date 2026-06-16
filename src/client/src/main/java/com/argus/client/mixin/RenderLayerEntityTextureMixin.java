package com.argus.client.mixin;

import com.argus.client.randomentity.ArgusRandomEntityState;
import com.argus.client.randomentity.RandomEntityRuntime;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Target: {@link RenderLayer#renderColoredCutoutModel}.
 *
 * <p>Purpose: emits OptiFine-style emissive companion textures for generic
 * living entity cutout layers that flow through Mojang's shared layer helper.
 *
 * <p>Preserved behavior: vanilla layer visibility, tint, overlay, ordering and
 * model choice are untouched. Argus only submits a fullbright companion pass
 * when the current resource-pack snapshot contains one.
 *
 * <p>Compatibility: optional injection; unsupported or custom layer renderers
 * fall through unchanged.
 *
 * <p>Risk: medium-low; this is a shared layer helper, but the added work is
 * config/snapshot gated and no vanilla arguments are mutated.
 */
@Mixin(RenderLayer.class)
public abstract class RenderLayerEntityTextureMixin {
    private static final int ARGUS_FULL_BRIGHT_LIGHT = 0x00F000F0;

    @Inject(method = "renderColoredCutoutModel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/"
                            + "OrderedSubmitNodeCollector;submitModel"
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
    private static <S extends LivingEntityRenderState>
    void argus$submitLayerEmissive(Model<? super S> model,
                                    Identifier texture,
                                    PoseStack poseStack,
                                    SubmitNodeCollector nodes,
                                    int lightCoords,
                                    S state,
                                    int color,
                                    int order,
                                    CallbackInfo ci) {
        Identifier current = texture;
        if (state instanceof ArgusRandomEntityState argusState) {
            current = RandomEntityRuntime.remap(texture,
                    argusState.argus$getRandomEntityVariantIndex());
        }
        Identifier emissive = RandomEntityRuntime.emissiveTexture(current);
        if (emissive == null) {
            return;
        }
        nodes.order(order).submitModel(model, state, poseStack,
                RenderTypes.entityTranslucentEmissive(emissive),
                ARGUS_FULL_BRIGHT_LIGHT, OverlayTexture.NO_OVERLAY,
                -1, null, state.outlineColor, null);
    }
}
