package com.argus.randomentity;

import com.argus.resource.NamespaceId;

import java.util.Map;

/**
 * Loader-neutral facts used to evaluate Random Entity rules.
 *
 * <p>Purpose: keeps OptiFine-style random entity rule matching out of Fabric
 * and Minecraft classes. Missing facts are represented by {@code null} or
 * {@code -1}; matchers must treat them as non-matching unless the condition is
 * absent.
 *
 * <p>Threading: immutable and safe to share. Performance: tiny value object
 * allocated once per evaluated render state by loader adapters.
 */
public record RandomEntityContext(
        long entitySeed,
        long vehicleSeed,
        NamespaceId biome,
        int height,
        String name,
        String profession,
        int professionLevel,
        String color,
        boolean baby,
        int health,
        int maxHealth,
        int moonPhase,
        int dayTime,
        String weather,
        int size,
        NamespaceId block,
        Map<String, String> nbt,
        Map<String, String> blockProperties) {

    public RandomEntityContext {
        nbt = nbt == null ? Map.of() : Map.copyOf(nbt);
        blockProperties = blockProperties == null ? Map.of()
                : Map.copyOf(blockProperties);
    }

    /** Empty context used by unit tests and fallback renderer paths. */
    public static RandomEntityContext empty(long seed) {
        return new RandomEntityContext(seed, seed, null, -1, null, null, -1,
                null, false, -1, -1, -1, -1, null, -1, null,
                Map.of(), Map.of());
    }
}
