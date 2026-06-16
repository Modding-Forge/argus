package com.argus.client.mixin;

import com.argus.fabric.randomentity.ArgusRandomEntityState;
import com.argus.randomentity.RandomEntityContext;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Target: {@link EntityRenderState}.
 *
 * <p>Purpose: attach the immutable Random Entity evaluation context to the
 * per-entity render state so later texture hooks do not need global mutable
 * current-entity state.
 *
 * <p>Preserved behavior: no vanilla fields or methods are changed.
 *
 * <p>Compatibility: adds only one private field and an interface.
 *
 * <p>Risk: low.
 */
@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateRandomEntityMixin
        implements ArgusRandomEntityState {
    @Unique
    private RandomEntityContext argus$randomEntityContext;

    @Unique
    private int argus$randomEntityVariantIndex = -1;

    @Override
    public void argus$setRandomEntityContext(RandomEntityContext context) {
        this.argus$randomEntityContext = context;
    }

    @Override
    public RandomEntityContext argus$getRandomEntityContext() {
        return argus$randomEntityContext;
    }

    @Override
    public void argus$setRandomEntityVariantIndex(int index) {
        this.argus$randomEntityVariantIndex = index;
    }

    @Override
    public int argus$getRandomEntityVariantIndex() {
        return argus$randomEntityVariantIndex;
    }
}
