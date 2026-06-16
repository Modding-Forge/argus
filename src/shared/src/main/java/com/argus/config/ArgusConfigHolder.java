package com.argus.config;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Process-wide holder for the active {@link ArgusConfig}. The
 * holder is a single atomic reference that can be replaced
 * atomically on config reload; readers see either the old or
 * the new config, never a half-updated state.
 *
 * <p>The class is loader-agnostic. Loaders initialise the
 * holder on mod start (and on every config-reload event) by
 * calling {@link #replace(ArgusConfig)}; the rest of the
 * codebase reads via {@link #get()}.
 *
 * <p>Performance: {@link #get()} is a single volatile read; the
 * hot paths (selector, registry) can cache the returned
 * reference locally and avoid the call entirely.
 */
public final class ArgusConfigHolder {

    private static final AtomicReference<ArgusConfig> INSTANCE =
            new AtomicReference<>(ArgusConfigDefaults.defaults());

    private ArgusConfigHolder() {
    }

    /**
     * Returns the active configuration, or the default if no
     * loader has set one.
     */
    public static ArgusConfig get() {
        return INSTANCE.get();
    }

    /**
     * Atomically replaces the active configuration.
     */
    public static void replace(ArgusConfig newConfig) {
        INSTANCE.set(newConfig);
    }

    /**
     * Resets the configuration to the defaults. Test-only.
     */
    public static void reset() {
        INSTANCE.set(ArgusConfigDefaults.defaults());
    }
}
