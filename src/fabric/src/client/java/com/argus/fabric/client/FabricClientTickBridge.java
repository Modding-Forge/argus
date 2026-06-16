package com.argus.fabric.client;

import com.argus.client.animation.CustomAnimationRuntime;
import com.argus.client.benchmark.ArgusBenchmarkDriver;
import com.argus.client.quad.BlockAtlasTracker;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Fabric adapter for common Argus client tick tasks.
 *
 * <p>Purpose: wires loader-neutral runtime helpers into Fabric's client tick
 * event without leaking Fabric API into {@code src/client}.
 *
 * <p>Threading: Fabric calls both helpers on the client thread.
 *
 * <p>Performance: each callback is O(1) and allocation-free in the common
 * case.
 */
public final class FabricClientTickBridge {

    private FabricClientTickBridge() {
    }

    /**
     * Registers all Argus client tick callbacks.
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(CustomAnimationRuntime::tick);
        ClientTickEvents.END_CLIENT_TICK.register(BlockAtlasTracker::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ArgusBenchmarkDriver::tick);
    }
}
