package com.argus.client.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.LongAdder;

/**
 * Dev-only runtime profiler for Argus renderer hooks.
 *
 * <p>Threading: called from Sodium section-build threads and the render
 * thread. Counters are lock-free; report emission is synchronized and rare.
 *
 * <p>Performance: HOT PATH. Disabled cost is one static-final boolean branch.
 * Enable only in development with {@code -Dargus.benchmark=true}; normal
 * release/runtime launches do not log or collect samples.
 */
public final class ArgusBenchmark {

    public static final int SODIUM_PROCESS_QUAD = 0;
    public static final int SODIUM_BETTER_GRASS = 1;
    public static final int SODIUM_CTM = 2;
    public static final int SODIUM_EMISSIVE = 3;
    public static final int SODIUM_NATURAL = 4;
    public static final int SODIUM_OVERLAY_RETURN = 5;
    public static final int SODIUM_BETTER_SNOW = 6;
    public static final int CTM_PREFILTER = 7;
    public static final int CTM_NEIGHBOR_VIEW = 8;
    public static final int CTM_RESOLVE = 9;
    public static final int CTM_MATERIAL = 10;
    public static final int CTM_OVERLAY_PLAN = 11;
    public static final int CTM_UV_REMAP = 12;

    private static final boolean ENABLED =
            Boolean.getBoolean("argus.benchmark");
    private static final Logger LOGGER =
            LoggerFactory.getLogger("argus/benchmark");
    private static final String[] NAMES = {
            "sodium.process_quad",
            "sodium.better_grass",
            "sodium.ctm",
            "sodium.emissive",
            "sodium.natural",
            "sodium.overlay_return",
            "sodium.better_snow",
            "ctm.prefilter",
            "ctm.neighbor_view",
            "ctm.resolve",
            "ctm.material",
            "ctm.overlay_plan",
            "ctm.uv_remap"
    };
    private static final LongAdder[] COUNTS = counters();
    private static final LongAdder[] NANOS = counters();
    private static final long REPORT_INTERVAL_NANOS = 5_000_000_000L;

    private static volatile long lastReportNanos = System.nanoTime();

    private ArgusBenchmark() {
    }

    public static boolean enabled() {
        return ENABLED;
    }

    public static long start() {
        return ENABLED ? System.nanoTime() : 0L;
    }

    public static void record(int bucket, long startNanos) {
        if (!ENABLED || startNanos == 0L) {
            return;
        }
        long now = System.nanoTime();
        COUNTS[bucket].increment();
        NANOS[bucket].add(now - startNanos);
        maybeReport(now);
    }

    private static void maybeReport(long now) {
        if (now - lastReportNanos < REPORT_INTERVAL_NANOS) {
            return;
        }
        synchronized (ArgusBenchmark.class) {
            if (now - lastReportNanos < REPORT_INTERVAL_NANOS) {
                return;
            }
            lastReportNanos = now;
            StringBuilder line = new StringBuilder(512);
            line.append("[Argus] benchmark");
            for (int i = 0; i < NAMES.length; i++) {
                long count = COUNTS[i].sumThenReset();
                long nanos = NANOS[i].sumThenReset();
                if (count == 0L) {
                    continue;
                }
                double totalMs = nanos / 1_000_000.0D;
                double avgNs = (double) nanos / count;
                line.append(' ')
                        .append(NAMES[i])
                        .append("{count=")
                        .append(count)
                        .append(", totalMs=")
                        .append(String.format(java.util.Locale.ROOT,
                                "%.3f", totalMs))
                        .append(", avgNs=")
                        .append(String.format(java.util.Locale.ROOT,
                                "%.1f", avgNs))
                        .append('}');
            }
            LOGGER.info(line.toString());
        }
    }

    private static LongAdder[] counters() {
        LongAdder[] out = new LongAdder[NAMES.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = new LongAdder();
        }
        return out;
    }
}
