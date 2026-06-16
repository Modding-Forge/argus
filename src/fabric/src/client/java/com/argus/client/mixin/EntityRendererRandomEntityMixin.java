package com.argus.client.mixin;

import com.argus.fabric.randomentity.ArgusRandomEntityState;
import com.argus.fabric.randomentity.RandomEntityRuntime;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Target: {@link EntityRenderer#extractRenderState(Entity, EntityRenderState, float)}.
 *
 * <p>Purpose: capture entity facts once during render-state extraction for
 * Random Entity texture matching.
 *
 * <p>Preserved behavior: vanilla state extraction runs first and is not
 * modified; Argus only stores its own immutable side payload.
 *
 * <p>Compatibility: {@code require = 0} keeps startup safe if the method moves.
 *
 * <p>Risk: low.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererRandomEntityMixin<T extends Entity,
        S extends EntityRenderState> {
    @Inject(method = "extractRenderState", at = @At("TAIL"), require = 0)
    private void argus$captureRandomEntityContext(T entity,
                                                   S state,
                                                   float partialTicks,
                                                   CallbackInfo ci) {
        if (state instanceof ArgusRandomEntityState argusState) {
            argusState.argus$setRandomEntityContext(
                    RandomEntityRuntime.capture(entity));
            argusState.argus$setRandomEntityVariantIndex(-1);
        }
    }
}
