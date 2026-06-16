package com.cinder.ctm;

import java.util.Locale;

/**
 * Loader-agnostic CTM render-layer hint parsed from OptiFine's
 * {@code layer=} property.
 *
 * <p>The shared CTM model keeps this as semantic data only. Fabric/Sodium
 * later maps it to the concrete terrain pass that exists in the active
 * renderer.
 *
 * <p>Mutability: immutable enum.
 *
 * <p>Performance: used on section-build hot paths through simple enum
 * comparisons; no allocation after parsing.
 */
public enum CtmRenderLayerHint {
    AUTO,
    SOLID,
    CUTOUT,
    CUTOUT_MIPPED,
    TRANSLUCENT;

    /**
     * Parses an OptiFine {@code layer=} value.
     *
     * <p>Unknown values fall back to {@link #AUTO}; resource packs should not
     * lose their whole rule because one renderer hint is unfamiliar.
     */
    public static CtmRenderLayerHint fromKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return AUTO;
        }
        return switch (key.trim().toLowerCase(Locale.ROOT)) {
            case "solid" -> SOLID;
            case "cutout" -> CUTOUT;
            case "cutout_mipped", "cutout-mipped" -> CUTOUT_MIPPED;
            case "translucent" -> TRANSLUCENT;
            default -> AUTO;
        };
    }
}
