package com.argus.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Phase 5 tests for the {@link ArgusConfigHolder}.
 *
 * <p>The holder is the process-wide singleton that loaders
 * populate and the rest of the codebase reads. The tests
 * verify the atomic-replace contract and the default fallback.
 */
class ArgusConfigHolderTest {

    @AfterEach
    void resetHolder() {
        // Each test must leave the holder at its default so
        // other tests in the same JVM do not observe stale
        // state.
        ArgusConfigHolder.reset();
    }

    @Test
    void get_returnsDefaultBeforeAnyReplace() {
        ArgusConfig cfg = ArgusConfigHolder.get();
        assertNotNull(cfg);
        assertEquals(ArgusConfigDefaults.ENABLED, cfg.enabled());
    }

    @Test
    void replace_isVisibleToSubsequentGet() {
        ArgusConfig custom = new ArgusConfig(
                false, true, true, false, true, BetterGrassMode.FANCY);
        ArgusConfigHolder.replace(custom);
        assertEquals(custom, ArgusConfigHolder.get());
    }

    @Test
    void replace_isAtomic() {
        // The holder uses an AtomicReference; a single thread
        // sees either the old or the new value, never a
        // half-updated state. We cannot directly test
        // atomicity from a single thread; we test the
        // contract that the returned reference is the one
        // we just put.
        ArgusConfig first = new ArgusConfig(true, false, false, true,
                false, BetterGrassMode.FAST);
        ArgusConfig second = new ArgusConfig(false, true, true, false,
                true, BetterGrassMode.OFF);
        ArgusConfigHolder.replace(first);
        ArgusConfigHolder.replace(second);
        assertEquals(second, ArgusConfigHolder.get());
    }

    @Test
    void reset_returnsToDefault() {
        ArgusConfigHolder.replace(
                new ArgusConfig(false, false, false, false, false,
                        BetterGrassMode.OFF));
        ArgusConfigHolder.reset();
        assertEquals(ArgusConfigDefaults.defaults(), ArgusConfigHolder.get());
    }
}
