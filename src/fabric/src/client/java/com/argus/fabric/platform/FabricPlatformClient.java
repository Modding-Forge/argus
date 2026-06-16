package com.argus.fabric.platform;

import com.argus.Constants;
import com.argus.ctm.CtmRegistry;
import com.argus.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Client-side {@link Platform} implementation. Discovered via
 * {@code META-INF/services} on the client classpath; the
 * server-side {@link FabricPlatform} (in the {@code main} source
 * set) is shadowed on client processes by this class.
 *
 * <p>The class is the single allowed importer of
 * {@code net.fabricmc.*} in the client source set beyond what
 * Fabric-API provides.
 */
public final class FabricPlatformClient implements Platform {

    private final CtmRegistry ctmRegistry;

    public FabricPlatformClient() {
        this.ctmRegistry = new CtmRegistry(Constants.MOD_ID);
    }

    @Override
    public String id() {
        return "fabric";
    }

    @Override
    public String modId() {
        return Constants.MOD_ID;
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public CtmRegistry ctmRegistry() {
        return this.ctmRegistry;
    }
}
