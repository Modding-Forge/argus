package com.argus.client.mixin;

import com.argus.fabric.randomentity.ArgusRandomEntityState;
import com.argus.randomentity.RandomEntityContext;
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
