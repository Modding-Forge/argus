package com.argus.client.mixin;

import com.argus.fabric.randomentity.ArgusRandomEntityState;
import com.argus.fabric.randomentity.RandomEntityRuntime;
import com.argus.randomentity.RandomEntityContext;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Target: {@link LivingEntityRenderer#getRenderType}.
 *
 * <p>Purpose: remap the base living-entity texture selected by vanilla to an
 * OptiFine-style Random Entity variant.
 *
 * <p>Preserved behavior: vanilla still chooses visibility, translucency,
 * outline, model render type, lighting, and state submission. Argus changes
 * only the texture identifier passed into the same vanilla render-type path.
 *
 * <p>Compatibility: {@code require = 0} lets the mod fail safe if Mojang
 * reshapes the render type method.
 *
 * <p>Risk: medium; the hook targets a renderer method shared by all living
 * entity renderers.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererRandomEntityMixin {
    @ModifyExpressionValue(method = "getRenderType",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/"
                            + "LivingEntityRenderer;getTextureLocation"
                            + "(Lnet/minecraft/client/renderer/entity/state/"
                            + "LivingEntityRenderState;)"
                            + "Lnet/minecraft/resources/Identifier;"),
            require = 0)
    private Identifier argus$randomEntityTexture(Identifier base,
            LivingEntityRenderState state) {
        if (state instanceof ArgusRandomEntityState argusState) {
            RandomEntityContext context =
                    argusState.argus$getRandomEntityContext();
            int variant = RandomEntityRuntime.resolveIndex(base, context);
            argusState.argus$setRandomEntityVariantIndex(variant);
            return RandomEntityRuntime.remap(base, variant);
        }
        return base;
    }
}
