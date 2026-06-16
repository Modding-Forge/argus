package com.argus.animation;

import java.util.List;
import java.util.Objects;

/**
 * Result of parsing many custom animation property files.
 *
 * <p>Malformed files are reported individually so resource reload can keep
 * valid rules active.
 */
public record CustomAnimationParseResult(
        List<CustomAnimationRule> rules,
        List<Error> errors) {

    public CustomAnimationParseResult {
        rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
        errors = List.copyOf(Objects.requireNonNull(errors, "errors"));
    }

    public record Error(String sourceFile, String message) {
        public Error {
            Objects.requireNonNull(sourceFile, "sourceFile");
            Objects.requireNonNull(message, "message");
        }
    }
}
