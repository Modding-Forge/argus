package com.argus.ctm;

import com.argus.resource.NamespaceId;

import java.util.Map;
import java.util.Objects;

/**
 * Immutable hot-path prefilter for CTM render lookups.
 *
 * <p>The index is built together with a {@link CtmRuleSet}. It answers cheap
 * "can this quad possibly match" questions before renderer integrations build
 * neighbour views or run the full selector.
 *
 * <p>Threading: immutable and safe to share across section-build threads.
 *
 * <p>Performance: HOT PATH. Allocation policy: no allocation during reads;
 * lookups are map probes plus bit tests.
 */
public final class CtmRenderIndex {

    private static final CtmRule[] NO_RULES = new CtmRule[0];
    private static final int FACE_COUNT = 6;

    private final Map<NamespaceId, FaceCandidates[]> bySprite;
    private final Map<String, FaceCandidates[]> byBlock;
    private final boolean hasOverlayCandidates;

    CtmRenderIndex(Map<NamespaceId, FaceCandidates[]> bySprite,
                   Map<String, FaceCandidates[]> byBlock,
                   boolean hasOverlayCandidates) {
        this.bySprite = copy(bySprite);
        this.byBlock = copy(byBlock);
        this.hasOverlayCandidates = hasOverlayCandidates;
    }

    /**
     * Returns {@code true} when either the sprite or block key has any rule
     * candidate for the requested face.
     */
    public boolean hasCandidate(NamespaceId sprite, String blockId, int face) {
        return hasSpriteCandidate(sprite, face) || hasBlockCandidate(blockId, face);
    }

    /**
     * Fills caller-owned candidate storage for the requested sprite/block/face.
     */
    public boolean candidatesInto(NamespaceId sprite,
                                  String blockId,
                                  int face,
                                  CtmCandidateScratch out) {
        Objects.requireNonNull(out, "out");
        out.clear();
        FaceCandidates spriteCandidates = spriteFaceCandidates(sprite, face);
        FaceCandidates blockCandidates = blockFaceCandidates(blockId, face);
        CtmRule[] spriteRules = spriteCandidates.rules;
        CtmRule[] blockRules = blockCandidates.rules;
        if (spriteRules.length == 0 && blockRules.length == 0) {
            return false;
        }
        out.set(spriteRules, blockRules, blockCandidates.neighborIndex,
                spriteCandidates.workFlags | blockCandidates.workFlags);
        return true;
    }

    /**
     * Returns {@code true} when the sprite has any rule candidate for a face.
     */
    public boolean hasSpriteCandidate(NamespaceId sprite, int face) {
        return spriteCandidates(sprite, face).length != 0;
    }

    /**
     * Returns {@code true} when the block has any rule candidate for a face.
     */
    public boolean hasBlockCandidate(String blockId, int face) {
        return blockCandidates(blockId, face).length != 0;
    }

    /**
     * Returns face-filtered sprite candidates in final rule priority order.
     */
    public CtmRule[] spriteCandidates(NamespaceId sprite, int face) {
        if (sprite == null || face < Faces.DOWN || face > Faces.EAST) {
            return NO_RULES;
        }
        FaceCandidates[] byFace = bySprite.get(sprite);
        return byFace == null ? NO_RULES : byFace[face].rules;
    }

    /**
     * Returns face-filtered block candidates in final rule priority order.
     */
    public CtmRule[] blockCandidates(String blockId, int face) {
        if (blockId == null || face < Faces.DOWN || face > Faces.EAST) {
            return NO_RULES;
        }
        FaceCandidates[] byFace = byBlock.get(blockId);
        return byFace == null ? NO_RULES : byFace[face].rules;
    }

    /**
     * Returns {@code true} when this snapshot contains any overlay CTM rule.
     */
    public boolean hasOverlayCandidates() {
        return hasOverlayCandidates;
    }

    static CandidateFlags flagsFor(CtmRule rule) {
        Objects.requireNonNull(rule, "rule");
        return new CandidateFlags(rule.facesMask(),
                rule.runtimeProfile().isOverlay());
    }

    static CtmRule[] noRules() {
        return NO_RULES;
    }

    static FaceCandidates[] byFace(java.util.List<CtmRule> rules) {
        @SuppressWarnings("unchecked")
        java.util.ArrayList<CtmRule>[] mutable =
                new java.util.ArrayList[FACE_COUNT];
        for (CtmRule rule : rules) {
            int mask = rule.facesMask() == 0 ? CandidateFlags.ALL_FACES
                    : rule.facesMask() & CandidateFlags.ALL_FACES;
            for (int face = Faces.DOWN; face <= Faces.EAST; face++) {
                if ((mask & (1 << face)) == 0) {
                    continue;
                }
                java.util.ArrayList<CtmRule> list = mutable[face];
                if (list == null) {
                    list = new java.util.ArrayList<>();
                    mutable[face] = list;
                }
                list.add(rule);
            }
        }
        FaceCandidates[] out = new FaceCandidates[FACE_COUNT];
        for (int face = Faces.DOWN; face <= Faces.EAST; face++) {
            java.util.ArrayList<CtmRule> list = mutable[face];
            CtmRule[] faceRules = list == null
                    ? NO_RULES
                    : list.toArray(NO_RULES);
            out[face] = FaceCandidates.of(faceRules);
        }
        return out;
    }

    private FaceCandidates spriteFaceCandidates(NamespaceId sprite, int face) {
        if (sprite == null || face < Faces.DOWN || face > Faces.EAST) {
            return FaceCandidates.EMPTY;
        }
        FaceCandidates[] byFace = bySprite.get(sprite);
        return byFace == null ? FaceCandidates.EMPTY : byFace[face];
    }

    private FaceCandidates blockFaceCandidates(String blockId, int face) {
        if (blockId == null || face < Faces.DOWN || face > Faces.EAST) {
            return FaceCandidates.EMPTY;
        }
        FaceCandidates[] byFace = byBlock.get(blockId);
        return byFace == null ? FaceCandidates.EMPTY : byFace[face];
    }

    private static <K> Map<K, FaceCandidates[]> copy(
            Map<K, FaceCandidates[]> input) {
        java.util.HashMap<K, FaceCandidates[]> out = new java.util.HashMap<>();
        for (Map.Entry<K, FaceCandidates[]> entry : input.entrySet()) {
            FaceCandidates[] faces = entry.getValue();
            FaceCandidates[] copied = new FaceCandidates[FACE_COUNT];
            for (int face = Faces.DOWN; face <= Faces.EAST; face++) {
                copied[face] = faces[face].copy();
            }
            out.put(entry.getKey(), copied);
        }
        return Map.copyOf(out);
    }

    private static int workFlags(CtmRule[] rules) {
        int flags = CtmCandidateScratch.NO_WORK;
        for (CtmRule rule : rules) {
            if (rule.runtimeProfile().isOverlay()) {
                flags |= CtmCandidateScratch.OVERLAY_ONLY;
            } else {
                flags |= CtmCandidateScratch.REPLACEMENT_ONLY;
            }
        }
        return flags;
    }

    /**
     * Immutable candidates for one key/face pair.
     */
    static final class FaceCandidates {
        private static final FaceCandidates EMPTY = new FaceCandidates(
                NO_RULES, null, CtmCandidateScratch.NO_WORK);

        private final CtmRule[] rules;
        private final CtmNeighborRuleIndex neighborIndex;
        private final int workFlags;

        private FaceCandidates(CtmRule[] rules,
                               CtmNeighborRuleIndex neighborIndex,
                               int workFlags) {
            this.rules = rules;
            this.neighborIndex = neighborIndex;
            this.workFlags = workFlags;
        }

        private static FaceCandidates of(CtmRule[] rules) {
            if (rules.length == 0) {
                return EMPTY;
            }
            CtmRule[] copy = java.util.Arrays.copyOf(rules, rules.length);
            return new FaceCandidates(copy, CtmNeighborRuleIndex.build(copy),
                    workFlags(copy));
        }

        private FaceCandidates copy() {
            if (rules.length == 0) {
                return EMPTY;
            }
            CtmRule[] copy = java.util.Arrays.copyOf(rules, rules.length);
            return new FaceCandidates(copy, CtmNeighborRuleIndex.build(copy),
                    workFlags);
        }
    }

    /**
     * Compact face/overlay metadata for one indexed key.
     */
    static final class CandidateFlags {
        private static final int ALL_FACES = 0x3F;

        private final int faceMask;
        private final boolean hasOverlay;

        CandidateFlags(int ruleFacesMask, boolean hasOverlay) {
            this.faceMask = ruleFacesMask == 0
                    ? ALL_FACES
                    : ruleFacesMask & ALL_FACES;
            this.hasOverlay = hasOverlay;
        }

        CandidateFlags merge(CandidateFlags other) {
            return new CandidateFlags(this.faceMask | other.faceMask,
                    this.hasOverlay || other.hasOverlay);
        }

        boolean matchesFace(int face) {
            return face >= Faces.DOWN && face <= Faces.EAST
                    && (faceMask & (1 << face)) != 0;
        }

        boolean hasOverlay() {
            return hasOverlay;
        }
    }
}
