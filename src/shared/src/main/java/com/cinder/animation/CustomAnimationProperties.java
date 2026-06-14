package com.cinder.animation;

import com.cinder.resource.NamespaceId;
import com.cinder.resource.PropertiesFile;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Clean-room parser for OptiFine-style {@code optifine/anim/*.properties}.
 *
 * <p>Threading: stateless reload-only parser.
 */
public final class CustomAnimationProperties {

    private static final String TILE_PREFIX = "tile.";
    private static final String DURATION_PREFIX = "duration.";

    private CustomAnimationProperties() {
    }

    public record RuleSource(String body, String sourceLabel) {
        public RuleSource {
            Objects.requireNonNull(body, "body");
            Objects.requireNonNull(sourceLabel, "sourceLabel");
        }
    }

    public static CustomAnimationRule parseString(String body,
                                                  String sourceLabel) {
        try {
            return parse(new StringReader(body), sourceLabel);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "failed to parse " + sourceLabel, e);
        }
    }

    public static CustomAnimationRule parse(Reader reader,
                                            String sourceLabel)
            throws IOException {
        return parse(PropertiesFile.parse(reader), sourceLabel);
    }

    public static CustomAnimationParseResult parseAll(
            List<RuleSource> sources) {
        ArrayList<CustomAnimationRule> rules = new ArrayList<>();
        ArrayList<CustomAnimationParseResult.Error> errors =
                new ArrayList<>();
        if (sources == null) {
            return new CustomAnimationParseResult(rules, errors);
        }
        for (RuleSource source : sources) {
            try {
                rules.add(parseString(source.body(), source.sourceLabel()));
            } catch (RuntimeException e) {
                errors.add(new CustomAnimationParseResult.Error(
                        source.sourceLabel(), e.getMessage()));
            }
        }
        return new CustomAnimationParseResult(rules, errors);
    }

    public static CustomAnimationRuleSet buildRuleSet(
            List<RuleSource> sources) {
        return CustomAnimationRuleSet.of(parseAll(sources).rules());
    }

    private static CustomAnimationRule parse(PropertiesFile props,
                                             String sourceLabel) {
        NamespaceId parent = parentDirectory(sourceLabel);
        NamespaceId from = requiredPath(props, "from", sourceLabel, parent);
        NamespaceId to = requiredPath(props, "to", sourceLabel, parent);
        int x = parseNonNegative(props.get("x"), 0, "x");
        int y = parseNonNegative(props.get("y"), 0, "y");
        int w = parseNonNegative(props.get("w"), 0, "w");
        int h = parseNonNegative(props.get("h"), 0, "h");
        int duration = parsePositive(props.get("duration"), 1, "duration");
        return new CustomAnimationRule(sourceLabel, from, to, x, y, w, h,
                duration, parseFrames(props, duration));
    }

    private static List<CustomAnimationFrame> parseFrames(
            PropertiesFile props,
            int defaultDuration) {
        ArrayList<Integer> indices = new ArrayList<>();
        for (String key : props.entries().keySet()) {
            if (key.startsWith(TILE_PREFIX)
                    && key.length() > TILE_PREFIX.length()) {
                indices.add(parseNonNegative(
                        key.substring(TILE_PREFIX.length()), 0, key));
            }
        }
        indices.sort(Comparator.naturalOrder());
        ArrayList<CustomAnimationFrame> frames = new ArrayList<>();
        for (int index : indices) {
            String tileKey = TILE_PREFIX + index;
            int tile = parseNonNegative(props.get(tileKey), 0, tileKey);
            String durationKey = DURATION_PREFIX + index;
            int duration = parsePositive(props.get(durationKey),
                    defaultDuration, durationKey);
            frames.add(new CustomAnimationFrame(tile, duration));
        }
        for (Map.Entry<String, String> entry : props.entries().entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(DURATION_PREFIX)
                    && key.length() > DURATION_PREFIX.length()
                    && props.get(TILE_PREFIX
                    + key.substring(DURATION_PREFIX.length())) == null) {
                parsePositive(entry.getValue(), defaultDuration, key);
            }
        }
        return frames;
    }

    private static NamespaceId requiredPath(PropertiesFile props,
                                            String key,
                                            String sourceLabel,
                                            NamespaceId parent) {
        String raw = props.get(key);
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(key + " is required in "
                    + sourceLabel);
        }
        return resolvePath(raw.trim(), parent);
    }

    private static NamespaceId resolvePath(String raw, NamespaceId parent) {
        String normalized = raw.replace('\\', '/');
        if (normalized.indexOf(':') > 0) {
            return NamespaceId.parse(normalized);
        }
        if (normalized.startsWith("./")) {
            return new NamespaceId(parent.namespace(),
                    parent.path() + "/" + normalized.substring(2));
        }
        if (!normalized.contains("/")) {
            return new NamespaceId(parent.namespace(),
                    parent.path() + "/" + normalized);
        }
        return new NamespaceId(parent.namespace(), normalized);
    }

    private static NamespaceId parentDirectory(String sourceLabel) {
        NamespaceId source = NamespaceId.parse(sourceLabel);
        String path = source.path();
        int slash = path.lastIndexOf('/');
        return new NamespaceId(source.namespace(),
                slash < 0 ? "" : path.substring(0, slash));
    }

    private static int parseNonNegative(String value,
                                        int fallback,
                                        String key) {
        int parsed = parseInt(value, fallback, key);
        if (parsed < 0) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                    "%s=%s must be >= 0", key, value));
        }
        return parsed;
    }

    private static int parsePositive(String value, int fallback, String key) {
        int parsed = parseInt(value, fallback, key);
        if (parsed <= 0) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                    "%s=%s must be > 0", key, value));
        }
        return parsed;
    }

    private static int parseInt(String value, int fallback, String key) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    Locale.ROOT, "%s=%s is not an integer", key, value), e);
        }
    }
}
