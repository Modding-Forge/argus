package com.argus.neoforge.client;

import com.argus.Constants;
import com.argus.client.platform.ClientRuntimePlatform;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

/**
 * NeoForge implementation of the shared Argus client runtime bridge.
 */
public final class NeoForgeClientRuntimePlatform
        implements ClientRuntimePlatform {

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public String modVersion() {
        return ModList.get()
                .getModContainerById(Constants.MOD_ID)
                .map(container -> container.getModInfo()
                        .getVersion()
                        .toString())
                .orElse("dev");
    }
}
