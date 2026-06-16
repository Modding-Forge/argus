package com.argus.neoforge.compat;

import com.argus.compat.CompatProbe;
import net.neoforged.fml.ModList;

/**
 * NeoForge mod-presence probe for shared compatibility decisions.
 */
public final class NeoForgeCompatProbe implements CompatProbe {

    @Override
    public boolean isLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
