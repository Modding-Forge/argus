package com.argus.ctm;

import com.argus.resource.NamespaceId;

import java.util.HashMap;
import java.util.Map;

/**
 * Reload-built prefilter for large homogeneous overlay candidate arrays.
 *
 * <p>Purpose: many real overlay packs attach large stacks of
 * {@code method=overlay} rules to one substrate block face. Most of those
 * rules cannot match if none of their connect materials appear in the eight
 * overlay-neighbour positions. This index maps possible neighbour block or
 * sprite identities to rule bits, letting the resolver skip rules before the
 * expensive overlay selector runs.
 *
 * <p>Threading: immutable after construction. Runtime filtering writes only
 * into caller-owned {@link CtmCandidateScratch}.
 *
 * <p>Performance: HOT PATH. Reads allocate nothing. The index is intentionally
 * only built for large, homogeneous overlay arrays; small or mixed candidate
 * arrays keep the simpler direct loop.
 */
final class CtmNeighborRuleIndex {

    private static final int MIN_RULES = 32;
    private static final int[][][] OVERLAY_SIDE_OFFSETS = {
            {
                    { -1, 0, 0 }, { 1, 0, 0 },
                    { 0, 0, -1 }, { 0, 0, 1 }
            },
            {
                    { -1, 0, 0 }, { 1, 0, 0 },
                    { 0, 0, 1 }, { 0, 0, -1 }
            },
            {
                    { 1, 0, 0 }, { -1, 0, 0 },
                    { 0, -1, 0 }, { 0, 1, 0 }
            },
            {
                    { -1, 0, 0 }, { 1, 0, 0 },
                    { 0, -1, 0 }, { 0, 1, 0 }
            },
            {
                    { 0, 0, -1 }, { 0, 0, 1 },
                    { 0, -1, 0 }, { 0, 1, 0 }
            },
            {
                    { 0, 0, 1 }, { 0, 0, -1 },
                    { 0, -1, 0 }, { 0, 1, 0 }
            }
    };
    private static final int[][][] OVERLAY_EDGE_OFFSETS = {
            {
                    { 1, 0, -1 }, { -1, 0, -1 },
                    { 1, 0, 1 }, { -1, 0, 1 }
            },
            {
                    { 1, 0, 1 }, { -1, 0, 1 },
                    { 1, 0, -1 }, { -1, 0, -1 }
            },
            {
                    { -1, -1, 0 }, { 1, -1, 0 },
                    { -1, 1, 0 }, { 1, 1, 0 }
            },
            {
                    { 1, -1, 0 }, { -1, -1, 0 },
                    { 1, 1, 0 }, { -1, 1, 0 }
            },
            {
                    { 0, -1, 1 }, { 0, -1, -1 },
                    { 0, 1, 1 }, { 0, 1, -1 }
            },
            {
                    { 0, -1, -1 }, { 0, -1, 1 },
                    { 0, 1, -1 }, { 0, 1, 1 }
            }
    };

    private final CtmRule[] rules;
    private final Map<String, long[]> byBlockId;
    private final Map<NamespaceId, long[]> bySprite;
    private final long[] alwaysMask;
    private final int wordCount;

    private CtmNeighborRuleIndex(CtmRule[] rules,
                                 Map<String, long[]> byBlockId,
                                 Map<NamespaceId, long[]> bySprite,
                                 long[] alwaysMask,
                                 int wordCount) {
        this.rules = rules;
        this.byBlockId = Map.copyOf(byBlockId);
        this.bySprite = Map.copyOf(bySprite);
        this.alwaysMask = java.util.Arrays.copyOf(alwaysMask,
                alwaysMask.length);
        this.wordCount = wordCount;
    }

    static CtmNeighborRuleIndex build(CtmRule[] rules) {
        if (rules.length < MIN_RULES) {
            return null;
        }
        int wordCount = (rules.length + 63) >>> 6;
        HashMap<String, long[]> byBlockId = new HashMap<>();
        HashMap<NamespaceId, long[]> bySprite = new HashMap<>();
        long[] alwaysMask = new long[wordCount];
        for (int i = 0; i < rules.length; i++) {
            CtmRule rule = rules[i];
            if (!canIndex(rule)) {
                return null;
            }
            boolean indexed = false;
            for (String blockId : rule.connectBlockIds()) {
                add(byBlockId, blockId, i, wordCount);
                indexed = true;
            }
            for (String blockId : rule.connectTileBlockFallbackIds()) {
                add(byBlockId, blockId, i, wordCount);
                indexed = true;
            }
            for (NamespaceId sprite : rule.connectTiles()) {
                add(bySprite, sprite, i, wordCount);
                indexed = true;
            }
            if (!indexed) {
                set(alwaysMask, i);
            }
        }
        if (byBlockId.isEmpty() && bySprite.isEmpty()) {
            return null;
        }
        return new CtmNeighborRuleIndex(rules, byBlockId, bySprite,
                alwaysMask, wordCount);
    }

    CtmRule[] filter(NeighborView view, int face, CtmCandidateScratch scratch) {
        long[] mask = scratch.neighborRuleMask(wordCount);
        System.arraycopy(alwaysMask, 0, mask, 0, wordCount);
        mergeFaceOffsets(view, face, OVERLAY_SIDE_OFFSETS[face], mask);
        mergeFaceOffsets(view, face, OVERLAY_EDGE_OFFSETS[face], mask);
        return scratch.materializeNeighborRules(rules, mask);
    }

    private void mergeFaceOffsets(NeighborView view,
                                  int face,
                                  int[][] offsets,
                                  long[] mask) {
        for (int i = 0; i < offsets.length; i++) {
            int[] offset = offsets[i];
            String blockId = view.blockId(offset[0], offset[1], offset[2]);
            if (blockId != null) {
                merge(mask, byBlockId.get(normalizedBlockId(blockId)));
            }
            NamespaceId sprite = view.sprite(offset[0], offset[1],
                    offset[2], face);
            if (sprite != null) {
                merge(mask, bySprite.get(sprite));
            }
        }
    }

    private static boolean canIndex(CtmRule rule) {
        CtmRuleRuntimeProfile profile = rule.runtimeProfile();
        if (!profile.isOverlay()
                || (rule.method() != CtmMethod.OVERLAY
                && rule.method() != CtmMethod.OVERLAY_CTM)) {
            return false;
        }
        if (!rule.heights().isAll() || !rule.biomes().isEmpty()) {
            return false;
        }
        return rule.connect() == ConnectMode.BLOCK;
    }

    private static <K> void add(Map<K, long[]> map,
                                K key,
                                int ruleIndex,
                                int wordCount) {
        long[] mask = map.get(key);
        if (mask == null) {
            mask = new long[wordCount];
            map.put(key, mask);
        }
        set(mask, ruleIndex);
    }

    private static void set(long[] mask, int ruleIndex) {
        mask[ruleIndex >>> 6] |= 1L << (ruleIndex & 63);
    }

    private static void merge(long[] target, long[] source) {
        if (source == null) {
            return;
        }
        for (int i = 0; i < source.length; i++) {
            target[i] |= source[i];
        }
    }

    private static String normalizedBlockId(String blockId) {
        return blockId.indexOf(':') >= 0
                ? blockId
                : NamespaceId.DEFAULT_NAMESPACE + ":" + blockId;
    }
}
