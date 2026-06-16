package com.argus.randomentity;

import com.argus.resource.ComponentMatchers;
import com.argus.resource.NamespaceId;
import com.argus.resource.RangeListInt;
import com.argus.resource.WeightedSelector;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * Immutable Random Entity rule.
 *
 * <p>Rules are evaluated in file order; the first matching rule chooses one of
 * its texture indices using a deterministic selector.
 */
public final class RandomEntityRule {
    private final int[] textureIndices;
    private final WeightedSelector selector;
    private final Set<NamespaceId> biomes;
    private final RangeListInt heights;
    private final ComponentMatchers.Compiled name;
    private final Set<String> professions;
    private final RangeListInt professionLevels;
    private final Set<String> colors;
    private final Boolean baby;
    private final RangeListInt health;
    private final boolean healthPercent;
    private final RangeListInt moonPhase;
    private final RangeListInt dayTime;
    private final Set<String> weather;
    private final RangeListInt sizes;
    private final Set<NamespaceId> blocks;
    private final Map<String, ComponentMatchers.Compiled> nbtMatchers;
    private final int seedOffset;
    private final boolean seedSourceVehicle;

    public RandomEntityRule(int[] textureIndices,
                            int[] weights,
                            Set<NamespaceId> biomes,
                            RangeListInt heights,
                            ComponentMatchers.Compiled name,
                            Set<String> professions,
                            RangeListInt professionLevels,
                            Set<String> colors,
                            Boolean baby,
                            RangeListInt health,
                            boolean healthPercent,
                            RangeListInt moonPhase,
                            RangeListInt dayTime,
                            Set<String> weather,
                            RangeListInt sizes,
                            Set<NamespaceId> blocks,
                            Map<String, ComponentMatchers.Compiled> nbtMatchers,
                            int seedOffset,
                            boolean seedSourceVehicle) {
        if (textureIndices == null || textureIndices.length == 0) {
            throw new IllegalArgumentException("textures must be non-empty");
        }
        this.textureIndices = textureIndices.clone();
        int[] actualWeights = normalizeWeights(weights, textureIndices.length);
        this.selector = new WeightedSelector(actualWeights);
        this.biomes = biomes == null ? Set.of() : Set.copyOf(biomes);
        this.heights = heights;
        this.name = name;
        this.professions = professions == null ? Set.of() : Set.copyOf(professions);
        this.professionLevels = professionLevels;
        this.colors = colors == null ? Set.of() : Set.copyOf(colors);
        this.baby = baby;
        this.health = health;
        this.healthPercent = healthPercent;
        this.moonPhase = moonPhase;
        this.dayTime = dayTime;
        this.weather = weather == null ? Set.of() : Set.copyOf(weather);
        this.sizes = sizes;
        this.blocks = blocks == null ? Set.of() : Set.copyOf(blocks);
        this.nbtMatchers = nbtMatchers == null ? Map.of()
                : Map.copyOf(nbtMatchers);
        this.seedOffset = seedOffset;
        this.seedSourceVehicle = seedSourceVehicle;
    }

    public int selectIndex(RandomEntityContext context) {
        long seed = seedSourceVehicle ? context.vehicleSeed()
                : context.entitySeed();
        seed ^= Integer.toUnsignedLong(seedOffset * 0x9E3779B9);
        return textureIndices[selector.sample(seed)];
    }

    public boolean matches(RandomEntityContext context) {
        if (!biomes.isEmpty() && (context.biome() == null
                || !biomes.contains(context.biome()))) {
            return false;
        }
        if (heights != null && !heights.contains(context.height())) {
            return false;
        }
        if (name != null && (context.name() == null
                || !name.matches(context.name()))) {
            return false;
        }
        if (!professions.isEmpty() && (context.profession() == null
                || !matchesProfession(context))) {
            return false;
        }
        if (!colors.isEmpty() && (context.color() == null
                || !colors.contains(context.color()))) {
            return false;
        }
        if (baby != null && baby.booleanValue() != context.baby()) {
            return false;
        }
        if (health != null) {
            int value = healthPercent && context.maxHealth() > 0
                    ? Math.round(context.health() * 100.0f / context.maxHealth())
                    : context.health();
            if (!health.contains(value)) {
                return false;
            }
        }
        if (moonPhase != null && !moonPhase.contains(context.moonPhase())) {
            return false;
        }
        if (dayTime != null && !dayTime.contains(context.dayTime())) {
            return false;
        }
        if (!weather.isEmpty() && (context.weather() == null
                || !weather.contains(context.weather()))) {
            return false;
        }
        if (sizes != null && !sizes.contains(context.size())) {
            return false;
        }
        if (!blocks.isEmpty() && (context.block() == null
                || !blocks.contains(context.block()))) {
            return false;
        }
        if (!nbtMatchers.isEmpty()) {
            for (Map.Entry<String, ComponentMatchers.Compiled> entry
                    : nbtMatchers.entrySet()) {
                String value = context.nbt().get(entry.getKey());
                if (!entry.getValue().matches(value, value != null)) {
                    return false;
                }
            }
        }
        return true;
    }

    public int[] textureIndices() {
        return textureIndices.clone();
    }

    private boolean matchesProfession(RandomEntityContext context) {
        String profession = context.profession();
        if (profession == null) {
            return false;
        }
        for (String accepted : professions) {
            int levelSep = accepted.indexOf(':');
            if (levelSep > 0 && levelSep + 1 < accepted.length()) {
                String acceptedProfession = accepted.substring(0, levelSep);
                if (!acceptedProfession.equals(profession)) {
                    continue;
                }
                try {
                    if (Integer.parseInt(accepted.substring(levelSep + 1))
                            == context.professionLevel()) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                    if (accepted.equals(profession)) {
                        return professionLevels == null
                                || professionLevels.contains(
                                context.professionLevel());
                    }
                }
            } else if (accepted.equals(profession)) {
                return professionLevels == null
                        || professionLevels.contains(context.professionLevel());
            }
        }
        return false;
    }

    private static int[] normalizeWeights(int[] weights, int count) {
        int[] out = new int[count];
        Arrays.fill(out, 1);
        if (weights != null) {
            System.arraycopy(weights, 0, out, 0, Math.min(count, weights.length));
        }
        return out;
    }
}
