package com.cinder.client.mixin;

import com.cinder.fabric.randomentity.CinderRandomEntityState;
import com.cinder.fabric.randomentity.RandomEntityRuntime;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.entity.state.PaintingRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Target: {@link PaintingRenderer#submit}.
 *
 * <p>Purpose: remaps the front painting sprite to OptiFine-style Random
 * Painting variants while keeping vanilla painting geometry, lighting, back
 * texture and submission order intact.
 *
 * <p>Compatibility: the hook is non-required and only observes the immutable
 * render state context already attached by Cinder's entity extraction mixin.
 *
 * <p>Risk: low. It changes only the first painting atlas lookup, which is the
 * front artwork sprite; the back sprite remains vanilla.
 */
@Mixin(PaintingRenderer.class)
public abstract class PaintingRendererRandomEntityMixin {
    @Shadow
    @Final
    private TextureAtlas paintingsAtlas;

    @ModifyExpressionValue(method = "submit",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/"
                            + "TextureAtlas;getSprite"
                            + "(Lnet/minecraft/resources/Identifier;)"
                            + "Lnet/minecraft/client/renderer/texture/"
                            + "TextureAtlasSprite;",
                    ordinal = 0),
            require = 0)
    private TextureAtlasSprite cinder$randomPaintingSprite(
            TextureAtlasSprite original,
            PaintingRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        if (!(state instanceof CinderRandomEntityState cinderState)
                || state.variant == null) {
            return original;
        }
        Identifier spriteId = state.variant.assetId();
        TextureAtlasSprite remapped = RandomEntityRuntime.remapPaintingSprite(
                this.paintingsAtlas,
                spriteId,
                cinderState.cinder$getRandomEntityContext());
        return remapped == null ? original : remapped;
    }
}
