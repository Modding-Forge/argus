package com.cinder.client.mixin;

import com.cinder.fabric.randomentity.CinderRandomEntityState;
import com.cinder.randomentity.RandomEntityContext;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Target: {@link BlockEntityRenderState}.
 *
 * <p>Purpose: attaches the immutable OptiFine-style Random Entity context to
 * block-entity render states so chest/shulker texture hooks can evaluate
 * conditions without global mutable renderer state.
 *
 * <p>Preserved behavior: vanilla render state fields and extraction remain
 * unchanged.
 *
 * <p>Compatibility: adds one private field and an accessor interface.
 *
 * <p>Risk: low.
 */
@Mixin(BlockEntityRenderState.class)
public abstract class BlockEntityRenderStateRandomEntityMixin
        implements CinderRandomEntityState {
    @Unique
    private RandomEntityContext cinder$randomEntityContext;

    @Unique
    private int cinder$randomEntityVariantIndex = -1;

    @Override
    public void cinder$setRandomEntityContext(RandomEntityContext context) {
        this.cinder$randomEntityContext = context;
    }

    @Override
    public RandomEntityContext cinder$getRandomEntityContext() {
        return cinder$randomEntityContext;
    }

    @Override
    public void cinder$setRandomEntityVariantIndex(int index) {
        this.cinder$randomEntityVariantIndex = index;
    }

    @Override
    public int cinder$getRandomEntityVariantIndex() {
        return cinder$randomEntityVariantIndex;
    }
}
