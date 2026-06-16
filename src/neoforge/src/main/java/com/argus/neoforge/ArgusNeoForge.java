package com.argus.neoforge;

import com.argus.Constants;
import com.argus.neoforge.client.ArgusNeoForgeClient;
import com.argus.platform.Platforms;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeoForge mod entrypoint.
 *
 * <p>Argus is a client visual mod. The common NeoForge entrypoint performs
 * only loader-level initialization that is safe on either logical side; all
 * client hooks live in the client-only companion class.
 */
@Mod(Constants.MOD_ID)
public final class ArgusNeoForge {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Constants.MOD_ID + "/neoforge");

    public ArgusNeoForge(IEventBus modEventBus) {
        if (FMLEnvironment.getDist().isClient()) {
            new ArgusNeoForgeClient(modEventBus);
        }
        LOGGER.info("[{}] initialized (loader={})",
                Constants.MOD_NAME, Platforms.get().id());
    }
}
