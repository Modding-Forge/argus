package com.argus.fabric.client;

import com.argus.Constants;
import com.argus.client.reload.ArgusClientReloadListeners;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Fabric adapter for common Argus client resource reload listeners.
 *
 * <p>Purpose: the common listener implementation lives in {@code src/client};
 * this class only supplies Fabric's stable listener id wrapper and registers
 * the listeners with Fabric API.
 *
 * <p>Threading: Fabric invokes the delegated listener using Mojang's reload
 * executors. This bridge adds no additional mutable state.
 *
 * <p>Performance: startup-only registration of a small fixed listener list.
 */
public final class FabricClientReloadBridge {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Constants.MOD_ID + "/fabric-reload");

    private FabricClientReloadBridge() {
    }

    /**
     * Registers all Argus client reload listeners with Fabric API.
     */
    public static void register() {
        for (ArgusClientReloadListeners.Entry entry
                : ArgusClientReloadListeners.createAll()) {
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                    .registerReloadListener(new FabricIdentifiedReloadListener(
                            entry.id(), entry.listener()));
        }
        LOGGER.info("[{}] registered client resource reload listeners",
                Constants.MOD_NAME);
    }

    private record FabricIdentifiedReloadListener(
            Identifier id,
            PreparableReloadListener delegate)
            implements IdentifiableResourceReloadListener {

        @Override
        @SuppressWarnings("deprecation")
        public Identifier getFabricId() {
            return id;
        }

        @Override
        public CompletableFuture<Void> reload(
                SharedState currentReload,
                Executor taskExecutor,
                PreparationBarrier preparationBarrier,
                Executor reloadExecutor) {
            return delegate.reload(currentReload, taskExecutor,
                    preparationBarrier, reloadExecutor);
        }
    }
}
