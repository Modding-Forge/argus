package com.argus.ctm;

/**
 * Caller-owned CTM candidate storage for renderer hot paths.
 *
 * <p>The render index fills this object with face-filtered sprite and block
 * candidates. Resolver callers can then evaluate those arrays without probing
 * the index a second time.
 *
 * <p>Threading: not thread-safe. Keep one instance per renderer worker or
 * pipeline object.
 *
 * <p>Performance: HOT PATH. Allocation policy: allocated once by the renderer
 * and reused for each quad.
 */
public final class CtmCandidateScratch {

    public static final int NO_WORK = 0;
    public static final int REPLACEMENT_ONLY = 1;
    public static final int OVERLAY_ONLY = 2;
    public static final int BOTH = REPLACEMENT_ONLY | OVERLAY_ONLY;

    private CtmRule[] spriteRules = CtmRenderIndex.noRules();
    private CtmRule[] blockRules = CtmRenderIndex.noRules();
    private CtmNeighborRuleIndex blockNeighborIndex;
    private CtmRule[] filteredBlockRules = CtmRenderIndex.noRules();
    private long[] neighborRuleMask = new long[0];
    private int blockRuleCount;
    private int workFlags;

    public void clear() {
        spriteRules = CtmRenderIndex.noRules();
        blockRules = CtmRenderIndex.noRules();
        blockNeighborIndex = null;
        filteredBlockRules = CtmRenderIndex.noRules();
        blockRuleCount = 0;
        workFlags = NO_WORK;
    }

    void set(CtmRule[] spriteRules,
             CtmRule[] blockRules,
             CtmNeighborRuleIndex blockNeighborIndex,
             int workFlags) {
        this.spriteRules = spriteRules;
        this.blockRules = blockRules;
        this.blockNeighborIndex = blockNeighborIndex;
        this.filteredBlockRules = CtmRenderIndex.noRules();
        this.blockRuleCount = blockRules.length;
        this.workFlags = workFlags;
    }

    public CtmRule[] spriteRules() {
        return spriteRules;
    }

    public CtmRule[] blockRules() {
        return blockRules;
    }

    CtmRule[] blockRules(NeighborView view, int face) {
        if (blockNeighborIndex == null || view == null) {
            blockRuleCount = blockRules.length;
            return blockRules;
        }
        return blockNeighborIndex.filter(view, face, this);
    }

    int blockRuleCount() {
        return blockRuleCount;
    }

    public int workFlags() {
        return workFlags;
    }

    public boolean hasWork() {
        return workFlags != NO_WORK;
    }

    public boolean hasReplacements() {
        return (workFlags & REPLACEMENT_ONLY) != 0;
    }

    public boolean hasOverlays() {
        return (workFlags & OVERLAY_ONLY) != 0;
    }

    long[] neighborRuleMask(int wordCount) {
        if (neighborRuleMask.length < wordCount) {
            neighborRuleMask = new long[wordCount];
        } else {
            java.util.Arrays.fill(neighborRuleMask, 0, wordCount, 0L);
        }
        return neighborRuleMask;
    }

    CtmRule[] materializeNeighborRules(CtmRule[] rules, long[] mask) {
        if (filteredBlockRules.length < rules.length) {
            filteredBlockRules = new CtmRule[rules.length];
        }
        int out = 0;
        for (int wordIndex = 0; wordIndex < mask.length; wordIndex++) {
            long word = mask[wordIndex];
            while (word != 0L) {
                int bit = Long.numberOfTrailingZeros(word);
                int ruleIndex = (wordIndex << 6) + bit;
                if (ruleIndex < rules.length) {
                    filteredBlockRules[out++] = rules[ruleIndex];
                }
                word &= word - 1L;
            }
        }
        blockRuleCount = out;
        return out == rules.length ? rules : filteredBlockRules;
    }
}
