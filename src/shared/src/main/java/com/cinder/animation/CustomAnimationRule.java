package com.cinder.animation;

import com.cinder.resource.NamespaceId;

import java.util.List;
import java.util.Objects;

/**
 * Immutable parsed custom texture animation rule.
 *
 * <p>Purpose: describes OptiFine-style animation properties without carrying
 * Minecraft classes or loaded image data. Loader adapters resolve resources and
 * upload frames.
 *
 * <p>Threading: immutable and safe to share between reload and render code.
 */
public record CustomAnimationRule(
        String sourceFile,
        NamespaceId fromTexture,
        NamespaceId toTexture,
        int x,
        int y,
        int width,
        int height,
        int defaultDuration,
        List<CustomAnimationFrame> frames) {

    public CustomAnimationRule {
        Objects.requireNonNull(sourceFile, "sourceFile");
        Objects.requireNonNull(fromTexture, "fromTexture");
        Objects.requireNonNull(toTexture, "toTexture");
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("x/y must be >= 0");
        }
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("w/h must be >= 0");
        }
        if (defaultDuration <= 0) {
            throw new IllegalArgumentException("duration must be > 0");
        }
        frames = List.copyOf(Objects.requireNonNull(frames, "frames"));
    }
}
