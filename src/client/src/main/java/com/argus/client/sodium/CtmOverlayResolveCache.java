package com.argus.client.sodium;

import com.argus.client.render.CtmMinecraftNeighborView;
import com.argus.ctm.ConnectMode;
import com.argus.ctm.CtmCandidateScratch;
import com.argus.ctm.CtmConnectivityProfile;
import com.argus.ctm.CtmMethod;
import com.argus.ctm.CtmRule;
import com.argus.ctm.Faces;
import com.argus.resource.NamespaceId;
import com.argus.resource.RangeListInt;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Per-processor CTM resolve cache for homogeneous connected-overlay stacks.
 *
 * <p>Purpose: real packs can attach very large overlay-only rule arrays to
 * common substrate blocks such as stone, deepslate and grass. Adjacent blocks
 * often repeat the same local neighbour signature, so re-running the full
 * overlay resolver for every block face wastes the same predicate work. This
 * cache stores the final shared resolver output for the exact rule-array and
 * neighbour signature.
 *
 * <p>Correctness: only narrowly cacheable candidate sets are admitted:
 * block-fallback only, overlay-only, connected overlay methods, no biome or
 * height filters, and no random/repeat/fixed methods. The cache key compares
 * candidate-array identity, face, centre block/sprite, neighbour block ids,
 * neighbour face sprites, solidity and exposed-neighbour masks exactly.
 *
 * <p>Threading: owned by one {@link CtmSodiumQuadProcessor}. It is not
 * thread-safe, matching the processor's reusable resolver scratch objects.
 *
 * <p>Performance: HOT PATH. Allocation policy: no allocation on lookup; one
 * entry allocation only when a cacheable signature is first stored.
 */
final class CtmOverlayResolveCache {

    private static final int NEIGHBOR_COUNT = 8;
    private static final int ENTRY_COUNT = 1024;
    private static final int ENTRY_MASK = ENTRY_COUNT - 1;
    private static final int MIN_RULES = 16;

    private static final int[][][] OFFSETS = {
            {
                    { -1, 0, 0 }, { 1, 0, 0 },
                    { 0, 0, -1 }, { 0, 0, 1 },
                    { 1, 0, -1 }, { -1, 0, -1 },
                    { 1, 0, 1 }, { -1, 0, 1 }
            },
            {
                    { -1, 0, 0 }, { 1, 0, 0 },
                    { 0, 0, 1 }, { 0, 0, -1 },
                    { 1, 0, 1 }, { -1, 0, 1 },
                    { 1, 0, -1 }, { -1, 0, -1 }
            },
            {
                    { 1, 0, 0 }, { -1, 0, 0 },
                    { 0, -1, 0 }, { 0, 1, 0 },
                    { -1, -1, 0 }, { 1, -1, 0 },
                    { -1, 1, 0 }, { 1, 1, 0 }
            },
            {
                    { -1, 0, 0 }, { 1, 0, 0 },
                    { 0, -1, 0 }, { 0, 1, 0 },
                    { 1, -1, 0 }, { -1, -1, 0 },
                    { 1, 1, 0 }, { -1, 1, 0 }
            },
            {
                    { 0, 0, -1 }, { 0, 0, 1 },
                    { 0, -1, 0 }, { 0, 1, 0 },
                    { 0, -1, 1 }, { 0, -1, -1 },
                    { 0, 1, 1 }, { 0, 1, -1 }
            },
            {
                    { 0, 0, 1 }, { 0, 0, -1 },
                    { 0, -1, 0 }, { 0, 1, 0 },
                    { 0, -1, -1 }, { 0, -1, 1 },
                    { 0, 1, -1 }, { 0, 1, 1 }
            }
    };

    private final Entry[] entries = new Entry[ENTRY_COUNT];
    private final String[] blocks = new String[NEIGHBOR_COUNT];
    private final NamespaceId[] sprites = new NamespaceId[NEIGHBOR_COUNT];

    private CtmRule @Nullable [] currentRules;
    private @Nullable String currentBlockId;
    private @Nullable NamespaceId currentBaseSprite;
    private int currentFace;
    private int currentFullMask;
    private int currentExposedMask;
    private long currentHash;
    private boolean currentReady;

    /**
     * Prepares the reusable lookup signature for a cacheable candidate set.
     */
    boolean prepare(CtmCandidateScratch candidates,
                    String blockId,
                    NamespaceId baseSprite,
                    CtmMinecraftNeighborView view,
                    int face) {
        currentReady = false;
        if (!isCacheable(candidates) || face < Faces.DOWN
                || face > Faces.EAST) {
            return false;
        }
        currentRules = candidates.blockRules();
        currentBlockId = blockId;
        currentBaseSprite = baseSprite;
        currentFace = face;
        currentFullMask = 0;
        currentExposedMask = 0;
        long hash = 0xCBF29CE484222325L;
        hash = mix(hash, System.identityHashCode(currentRules));
        hash = mix(hash, face);
        hash = mix(hash, blockId == null ? 0 : blockId.hashCode());
        hash = mix(hash, baseSprite == null ? 0 : baseSprite.hashCode());

        int[] normal = Faces.delta(face);
        int[][] offsets = OFFSETS[face];
        for (int i = 0; i < NEIGHBOR_COUNT; i++) {
            int[] offset = offsets[i];
            boolean full = view.isFullBlock(offset[0], offset[1], offset[2]);
            boolean exposed = !view.isFullBlock(
                    offset[0] + normal[0],
                    offset[1] + normal[1],
                    offset[2] + normal[2]);
            String neighbourBlock = view.blockId(offset[0], offset[1],
                    offset[2]);
            NamespaceId neighbourSprite = view.sprite(offset[0], offset[1],
                    offset[2], face);
            blocks[i] = neighbourBlock;
            sprites[i] = neighbourSprite;
            if (full) {
                currentFullMask |= 1 << i;
            }
            if (exposed) {
                currentExposedMask |= 1 << i;
            }
            hash = mix(hash, neighbourBlock == null
                    ? 0
                    : neighbourBlock.hashCode());
            hash = mix(hash, neighbourSprite == null
                    ? 0
                    : neighbourSprite.hashCode());
        }
        hash = mix(hash, currentFullMask);
        hash = mix(hash, currentExposedMask);
        currentHash = hash;
        currentReady = true;
        return true;
    }

    /**
     * Returns a cached result for the current prepared signature.
     */
    @Nullable ArgusCtmFaceSpriteResult lookup() {
        if (!currentReady) {
            return null;
        }
        Entry entry = entries[index(currentHash)];
        return entry != null && entry.matches(this) ? entry.result : null;
    }

    /**
     * Stores the resolved output for the current prepared signature.
     */
    void store(ArgusCtmFaceSpriteResult result) {
        if (!currentReady || currentRules == null) {
            return;
        }
        int index = index(currentHash);
        Entry entry = entries[index];
        if (entry == null) {
            entry = new Entry();
            entries[index] = entry;
        }
        entry.store(
                currentHash,
                currentRules,
                currentBlockId,
                currentBaseSprite,
                currentFace,
                currentFullMask,
                currentExposedMask,
                blocks,
                sprites,
                result);
    }

    private static boolean isCacheable(CtmCandidateScratch candidates) {
        if (candidates.hasReplacements()
                || candidates.spriteRules().length != 0) {
            return false;
        }
        CtmRule[] rules = candidates.blockRules();
        if (rules.length < MIN_RULES) {
            return false;
        }
        for (CtmRule rule : rules) {
            if (!isCacheable(rule)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isCacheable(CtmRule rule) {
        CtmMethod method = rule.method();
        return rule.runtimeProfile().isOverlay()
                && rule.runtimeProfile().connectivity()
                == CtmConnectivityProfile.FULL_8
                && (method == CtmMethod.OVERLAY
                || method == CtmMethod.OVERLAY_CTM)
                && rule.connect() == ConnectMode.BLOCK
                && rule.biomes().isEmpty()
                && rule.heights() == RangeListInt.ALL;
    }

    private static int index(long hash) {
        return ((int) (hash ^ (hash >>> 32))) & ENTRY_MASK;
    }

    private static long mix(long hash, int value) {
        long next = hash ^ value;
        return next * 0x100000001B3L;
    }

    private static final class Entry {
        private long hash;
        private CtmRule[] rules;
        private @Nullable String blockId;
        private @Nullable NamespaceId baseSprite;
        private int face;
        private int fullMask;
        private int exposedMask;
        private final String[] blocks = new String[NEIGHBOR_COUNT];
        private final NamespaceId[] sprites = new NamespaceId[NEIGHBOR_COUNT];
        private ArgusCtmFaceSpriteResult result = ArgusCtmFaceSpriteResult.NO_WORK;

        private void store(long hash,
                           CtmRule[] rules,
                           @Nullable String blockId,
                           @Nullable NamespaceId baseSprite,
                           int face,
                           int fullMask,
                           int exposedMask,
                           String[] blocks,
                           NamespaceId[] sprites,
                           ArgusCtmFaceSpriteResult result) {
            this.hash = hash;
            this.rules = rules;
            this.blockId = blockId;
            this.baseSprite = baseSprite;
            this.face = face;
            this.fullMask = fullMask;
            this.exposedMask = exposedMask;
            System.arraycopy(blocks, 0, this.blocks, 0, NEIGHBOR_COUNT);
            System.arraycopy(sprites, 0, this.sprites, 0, NEIGHBOR_COUNT);
            this.result = result;
        }

        private boolean matches(CtmOverlayResolveCache current) {
            if (hash != current.currentHash
                    || rules != current.currentRules
                    || face != current.currentFace
                    || fullMask != current.currentFullMask
                    || exposedMask != current.currentExposedMask
                    || !Objects.equals(blockId, current.currentBlockId)
                    || !Objects.equals(baseSprite, current.currentBaseSprite)) {
                return false;
            }
            for (int i = 0; i < NEIGHBOR_COUNT; i++) {
                if (!Objects.equals(blocks[i], current.blocks[i])
                        || !Objects.equals(sprites[i], current.sprites[i])) {
                    return false;
                }
            }
            return true;
        }
    }
}
