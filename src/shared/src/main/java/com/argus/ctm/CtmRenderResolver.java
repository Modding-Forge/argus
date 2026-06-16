package com.argus.ctm;

import com.argus.resource.NamespaceId;

import java.util.Objects;

/**
 * High-level CTM selection resolver for renderer integrations.
 *
 * <p>The resolver takes the renderer's current block id, base sprite, face,
 * position, and neighbour view, then returns a backend-neutral
 * {@link CtmRenderPlan}. It deliberately stops before any rendering
 * operation such as sprite swapping, UV mutation, or GPU buffer writes.
 *
 * <h2>Threading</h2>
 *
 * <p>The resolver is stateless and thread-safe. It reads immutable
 * {@link CtmRuleSet} snapshots through {@link CtmRegistry}.
 *
 * <h2>Performance</h2>
 *
 * <p>Performance: HOT PATH. Allocation policy: no allocation when the
 * prefilter has no candidates; one {@link CtmSelector} and one
 * {@link CtmRenderPlan} when CTM work is selected. Callers may keep a resolver
 * instance per section build to avoid repeatedly allocating it.
 */
public final class CtmRenderResolver {

    private final CtmRegistry registry;
    private final CtmSelector selector =
            new CtmSelector(CtmRuleSet.empty());

    public CtmRenderResolver(CtmRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    /**
     * Returns {@code true} when the active rule snapshot has any candidate for
     * the sprite or block on the requested face.
     */
    public boolean hasCandidates(String blockId, NamespaceId baseSprite,
                                 int face) {
        if (blockId == null || baseSprite == null) {
            return false;
        }
        return registry.ruleSet().renderIndex()
                .hasCandidate(baseSprite, blockId, face);
    }

    /**
     * Resolves applicable CTM render work for the given face.
     *
     * <p>Non-overlay methods keep OptiFine-style first-match behaviour.
     * Overlay methods are compositing rules, so matching overlay tiles before
     * the selected replacement are preserved in rule order.
     */
    public CtmRenderPlan resolvePlan(String blockId,
                                     NamespaceId baseSprite,
                                     NeighborView view,
                                     int x, int y, int z,
                                     int face) {
        CtmRenderScratch scratch = new CtmRenderScratch();
        return resolveInto(blockId, baseSprite, view, x, y, z, face, scratch)
                ? scratch.toPlan()
                : null;
    }

    /**
     * Resolves applicable CTM render work into caller-owned storage.
     *
     * <p>Performance: HOT PATH. Allocation policy: no plan/list allocation in
     * this method; selections selected by the shared selector remain immutable.
     */
    public boolean resolveInto(String blockId,
                               NamespaceId baseSprite,
                               NeighborView view,
                               int x, int y, int z,
                               int face,
                               CtmRenderScratch out) {
        Objects.requireNonNull(out, "out");
        out.clear();
        if (blockId == null || baseSprite == null || view == null) {
            return false;
        }
        CtmRuleSet ruleSet = registry.ruleSet();
        CtmRenderIndex index = ruleSet.renderIndex();
        CtmRule[] spriteRules = index.spriteCandidates(baseSprite, face);
        CtmRule[] blockRules = index.blockCandidates(blockId, face);
        if (spriteRules.length == 0 && blockRules.length == 0) {
            return false;
        }

        CtmRenderSelection selection = collectFromRules(
                selector,
                spriteRules,
                view, x, y, z, face, baseSprite, out);
        if (selection != null) {
            out.setReplacement(selection);
            return true;
        }
        selection = collectFromRules(
                selector,
                blockRules,
                view, x, y, z, face, baseSprite, out);
        if (selection != null) {
            out.setReplacement(selection);
            return true;
        }
        return out.hasOverlays();
    }

    /**
     * Compatibility wrapper for existing callers that expect one selection.
     */
    public CtmRenderSelection resolve(String blockId,
                                      NamespaceId baseSprite,
                                      NeighborView view,
                                      int x, int y, int z,
                                      int face) {
        CtmRenderPlan plan = resolvePlan(blockId, baseSprite, view, x, y, z,
                face);
        if (plan == null) {
            return null;
        }
        if (plan.hasReplacement()) {
            return plan.replacement();
        }
        return CtmRenderSelection.overlayStack(face, baseSprite,
                plan.overlays());
    }

    private static CtmRenderSelection collectFromRules(
            CtmSelector selector,
            CtmRule[] rules,
            NeighborView view,
            int x, int y, int z,
            int face,
            NamespaceId baseSprite,
            CtmRenderScratch out) {
        for (CtmRule rule : rules) {
            if (rule.method().isOverlay()) {
                if (!selector.maySelectConnectedOverlay(rule, view, face)) {
                    continue;
                }
                selector.selectOverlayInto(rule, view, x, y, z, face, out);
                continue;
            }
            CtmRenderSelection selection = selector.selectRender(
                    rule, view, x, y, z, face, baseSprite);
            if (selection == null || selection.isPrimarySkip()) {
                continue;
            }
            return selection;
        }
        return null;
    }
}
