package com.argus.neoforge.platform;

import com.argus.Constants;
import com.argus.ctm.CtmRegistry;
import com.argus.platform.Platform;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * NeoForge implementation of Argus's loader-neutral platform service.
 */
public final class NeoForgePlatform implements Platform {

    private final CtmRegistry ctmRegistry = new CtmRegistry(Constants.MOD_ID);

    @Override
    public String id() {
        return "neoforge";
    }

    @Override
    public String modId() {
        return Constants.MOD_ID;
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.getDist().isClient();
    }

    @Override
    public CtmRegistry ctmRegistry() {
        return this.ctmRegistry;
    }
}
