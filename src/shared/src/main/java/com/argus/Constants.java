package com.argus;

/**
 * Compile-time constants shared between the {@code common} and loader
 * modules. Kept in {@code common} so that any feature code can refer to
 * them without depending on a loader module.
 */
public final class Constants {

    /**
     * The mod id of the running mod. This value MUST match the
     * {@code fabric.mod.json.id} (and, later, the NeoForge mods.toml
     * {@code modId}). It is duplicated here as a compile-time constant so
     * that no resource lookups are needed at runtime.
     */
    public static final String MOD_ID = "argus";

    /**
     * The conceptual mod name, used in logs and user-facing strings. The
     * shipped jar's {@code name} field may differ in some loaders, but
     * this is the canonical name.
     */
    public static final String MOD_NAME = "Argus";

    private Constants() {
    }
}
