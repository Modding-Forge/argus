package com.cinder.animation;

/**
 * One frame declaration from an OptiFine-style custom animation rule.
 *
 * <p>Instances are immutable and loader-agnostic. The tile index references a
 * {@code w x h} region inside the source image, counted left-to-right and
 * top-to-bottom.
 */
public record CustomAnimationFrame(int tileIndex, int duration) {

    public CustomAnimationFrame {
        if (tileIndex < 0) {
            throw new IllegalArgumentException("tile index must be >= 0");
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("duration must be > 0");
        }
    }
}
