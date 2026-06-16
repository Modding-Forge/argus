package com.argus.fabric.randomentity;

import com.argus.randomentity.RandomEntityContext;

/**
 * Accessor attached to Minecraft entity render states by a tiny mixin.
 *
 * <p>Threading: the render state is per extracted entity render submission, so
 * storing the immutable context on it avoids global mutable entity state.
 */
public interface ArgusRandomEntityState {
    void argus$setRandomEntityContext(RandomEntityContext context);

    RandomEntityContext argus$getRandomEntityContext();

    void argus$setRandomEntityVariantIndex(int index);

    int argus$getRandomEntityVariantIndex();
}
