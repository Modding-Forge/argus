package com.argus.client.platform;

import java.nio.file.Path;

/**
 * Loader bridge for Argus client code that is shared between Fabric and
 * NeoForge.
 *
 * <p>This interface intentionally exposes only stable process facts needed by
 * common Minecraft/Sodium client classes: config location, installed-mod
 * checks and the displayed Argus version. Loader modules provide the
 * implementation through {@link java.util.ServiceLoader}; render hooks never
 * import Fabric or NeoForge directly.
 *
 * <p>Threading: implementations must be safe for reads from client/render
 * threads after loader startup.
 *
 * <p>Performance: O(1) expected for all methods.
 */
public interface ClientRuntimePlatform {

    /**
     * Returns the loader-standard config directory.
     */
    Path configDir();

    /**
     * Returns whether a mod id is present in the running client.
     */
    boolean isModLoaded(String modId);

    /**
     * Returns the user-facing Argus version for config screens.
     */
    String modVersion();
}
