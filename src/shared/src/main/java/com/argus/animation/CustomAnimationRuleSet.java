package com.argus.animation;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of all parsed custom animation rules.
 *
 * <p>Rules are kept in a stable source-path order so reloads produce
 * deterministic runtime snapshots.
 */
public final class CustomAnimationRuleSet {

    private static final CustomAnimationRuleSet EMPTY =
            new CustomAnimationRuleSet(List.of());

    private final List<CustomAnimationRule> rules;

    private CustomAnimationRuleSet(List<CustomAnimationRule> rules) {
        this.rules = rules;
    }

    public static CustomAnimationRuleSet empty() {
        return EMPTY;
    }

    public static CustomAnimationRuleSet of(List<CustomAnimationRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return EMPTY;
        }
        List<CustomAnimationRule> sorted = rules.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(CustomAnimationRule::sourceFile))
                .toList();
        return sorted.isEmpty() ? EMPTY : new CustomAnimationRuleSet(sorted);
    }

    public List<CustomAnimationRule> all() {
        return rules;
    }

    public boolean isEmpty() {
        return rules.isEmpty();
    }
}
