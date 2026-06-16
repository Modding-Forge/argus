package com.argus.neoforge.client;

import com.argus.Constants;
import com.argus.client.animation.CustomAnimationRuntime;
import com.argus.client.benchmark.ArgusBenchmarkDriver;
import com.argus.client.config.ArgusClientConfigLoader;
import com.argus.client.platform.ClientEnvironment;
import com.argus.client.quad.BlockAtlasTracker;
import com.argus.client.reload.ArgusClientReloadListeners;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeoForge client-side bridge for common Argus client runtime code.
 *
 * <p>Purpose: connects loader-neutral client reload listeners, optional
 * built-in packs, and tick helpers to NeoForge events. Feature logic remains
 * in {@code src/client}; this class only adapts NeoForge event APIs.
 *
 * <p>Threading: NeoForge calls registration on the mod event bus during
 * startup and client tick callbacks on the client thread.
 *
 * <p>Compatibility: hooks are narrow and do not replace vanilla or Sodium
 * systems. Sodium remains a required dependency declared in metadata.
 */
public final class ArgusNeoForgeClient {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Constants.MOD_ID + "/neoforge-client");
    private static final Identifier BUILTIN_DEFAULT_PACK =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,
                    "resourcepacks/default_ctm_glass");

    /**
     * Registers NeoForge client event bridges.
     *
     * @param modEventBus NeoForge mod event bus
     */
    public ArgusNeoForgeClient(IEventBus modEventBus) {
        modEventBus.addListener(ArgusNeoForgeClient::registerReloadListeners);
        modEventBus.addListener(ArgusNeoForgeClient::registerBuiltinPacks);
        NeoForge.EVENT_BUS.addListener(ArgusNeoForgeClient::onClientTick);

        try {
            ArgusClientConfigLoader.loadAndInstall(
                    ClientEnvironment.get().configDir());
        } catch (RuntimeException e) {
            LOGGER.warn("[{}] config load failed; using defaults: {}",
                    Constants.MOD_NAME, e.getMessage());
        }
        LOGGER.info("[{}] initialized (client, loader=neoforge)",
                Constants.MOD_NAME);
    }

    private static void registerReloadListeners(
            AddClientReloadListenersEvent event) {
        for (ArgusClientReloadListeners.Entry entry
                : ArgusClientReloadListeners.createAll()) {
            event.addListener(entry.id(), entry.listener());
        }
        LOGGER.info("[{}] registered client resource reload listeners",
                Constants.MOD_NAME);
    }

    private static void registerBuiltinPacks(AddPackFindersEvent event) {
        event.addPackFinders(
                BUILTIN_DEFAULT_PACK,
                PackType.CLIENT_RESOURCES,
                Component.literal("Argus Default Glass CTM"),
                PackSource.BUILT_IN,
                false,
                Pack.Position.BOTTOM);
        LOGGER.info("[{}] registered built-in resource pack {}",
                Constants.MOD_NAME, BUILTIN_DEFAULT_PACK);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        CustomAnimationRuntime.tick(minecraft);
        BlockAtlasTracker.tick(minecraft);
        ArgusBenchmarkDriver.tick(minecraft);
    }
}
