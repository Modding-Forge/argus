package com.argus.fabric.platform;

import com.argus.Constants;
import com.argus.client.platform.ClientRuntimePlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/**
 * Fabric implementation of the shared Argus client runtime bridge.
 *
 * <p>Purpose: keeps FabricLoader calls out of common Minecraft/Sodium client
 * classes while preserving exact Fabric config-path and mod-presence
 * semantics.
 */
public final class FabricClientRuntimePlatform implements ClientRuntimePlatform {

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public String modVersion() {
        return FabricLoader.getInstance()
                .getModContainer(Constants.MOD_ID)
                .map(container -> container.getMetadata()
                        .getVersion()
                        .getFriendlyString())
                .orElse("dev");
    }
}
