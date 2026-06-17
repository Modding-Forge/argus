package com.argus.ctm;

import java.util.concurrent.atomic.LongAdder;

/**
 * Benchmark-only counters for connected-overlay rejection reasons.
 *
 * <p>Purpose: CTM-v3 optimization work needs evidence about why the remaining
 * large overlay candidate sets still resolve to no work. This class records
 * those reasons during explicit dev benchmark runs without pulling Minecraft
 * or loader APIs into {@code src/shared}.
 *
 * <p>Threading: Sodium section-build workers may increment counters
 * concurrently. Snapshots are eventually consistent and intended for benchmark
 * reports only.
 *
 * <p>Performance: HOT PATH. Allocation policy: no allocation while recording.
 * Recording is gated by {@code argus.benchmark.ctmOverlayDiagnostics};
 * normal gameplay and ordinary performance benchmarks pay only a no-op branch
 * after JIT inlining.
 */
public final class CtmOverlayDiagnostics {

    private static final boolean ENABLED =
            Boolean.getBoolean("argus.benchmark.ctmOverlayDiagnostics");
    private static final LongAdder[] COUNTERS = createCounters();

    private CtmOverlayDiagnostics() {
    }

    /**
     * Returns whether benchmark diagnostics are enabled for this JVM.
     */
    public static boolean enabled() {
        return ENABLED;
    }

    /**
     * Clears all counters for a new benchmark measurement window.
     */
    public static void reset() {
        if (!ENABLED) {
            return;
        }
        for (LongAdder counter : COUNTERS) {
            counter.reset();
        }
    }

    /**
     * Records one overlay diagnostic event.
     *
     * @param reason reason to increment
     */
    public static void record(Reason reason) {
        if (!ENABLED || reason == null) {
            return;
        }
        COUNTERS[reason.ordinal()].increment();
    }

    /**
     * Returns the current counters in enum order.
     */
    public static Snapshot[] snapshot() {
        if (!ENABLED) {
            return new Snapshot[0];
        }
        Reason[] reasons = Reason.values();
        Snapshot[] out = new Snapshot[reasons.length];
        for (int i = 0; i < reasons.length; i++) {
            out[i] = new Snapshot(reasons[i].reportName(), COUNTERS[i].sum());
        }
        return out;
    }

    private static LongAdder[] createCounters() {
        Reason[] reasons = Reason.values();
        LongAdder[] out = new LongAdder[reasons.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = new LongAdder();
        }
        return out;
    }

    /**
     * Connected-overlay prefilter and connection outcomes.
     */
    public enum Reason {
        PREFILTER_CALL("prefilter.call"),
        PREFILTER_PASS("prefilter.pass"),
        PREFILTER_FAIL("prefilter.fail"),
        CONNECT_CALL("connect.call"),
        CONNECT_PASS("connect.pass"),
        CONNECT_NOT_FULL_BLOCK("connect.reject.notFullBlock"),
        CONNECT_TILE_MISMATCH("connect.reject.tileMismatch"),
        CONNECT_BLOCK_MISMATCH("connect.reject.blockMismatch"),
        CONNECT_DEFAULT_BLOCK_MISMATCH("connect.reject.defaultBlockMismatch"),
        CONNECT_FRONT_OCCLUDED("connect.reject.frontOccluded"),
        CONNECT_SAME_AS_BASE("connect.reject.sameAsBase");

        private final String reportName;

        Reason(String reportName) {
            this.reportName = reportName;
        }

        private String reportName() {
            return reportName;
        }
    }

    /**
     * Immutable overlay diagnostic row for benchmark reports.
     */
    public record Snapshot(String reason, long count) {
    }
}
