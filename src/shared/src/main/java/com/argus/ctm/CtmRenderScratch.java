package com.argus.ctm;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable CTM render output buffer for renderer hot paths.
 *
 * <p>Purpose: exposes the same replacement/overlay plan semantics as
 * {@link CtmRenderPlan}, but lets renderer integrations reuse storage instead
 * of allocating a plan and overlay list for every matching face.
 *
 * <p>Threading: not thread-safe. Use one instance per renderer/worker object.
 *
 * <p>Performance: HOT PATH. Allocation policy: no allocation for the common
 * case after construction; the overlay array grows only for unusually deep
 * overlay stacks.
 */
public final class CtmRenderScratch {

    private CtmRenderSelection replacement;
    private CtmOverlayTile[] overlays = new CtmOverlayTile[8];
    private int overlayCount;

    /**
     * Clears previously selected work while retaining backing storage.
     */
    public void clear() {
        replacement = null;
        for (int i = 0; i < overlayCount; i++) {
            overlays[i] = null;
        }
        overlayCount = 0;
    }

    public void setReplacement(CtmRenderSelection selection) {
        this.replacement = selection;
    }

    public void addOverlays(List<CtmOverlayTile> tiles) {
        ensureCapacity(overlayCount + tiles.size());
        for (int i = 0; i < tiles.size(); i++) {
            overlays[overlayCount++] = tiles.get(i);
        }
    }

    public void addOverlay(CtmRule rule, int tileIndex) {
        ensureCapacity(overlayCount + 1);
        overlays[overlayCount++] = new CtmOverlayTile(rule, tileIndex);
    }

    public CtmRenderSelection replacement() {
        return replacement;
    }

    public boolean hasReplacement() {
        return replacement != null;
    }

    public boolean hasOverlays() {
        return overlayCount > 0;
    }

    public boolean hasWork() {
        return replacement != null || overlayCount > 0;
    }

    public int overlayCount() {
        return overlayCount;
    }

    public CtmOverlayTile overlay(int index) {
        if (index < 0 || index >= overlayCount) {
            throw new IndexOutOfBoundsException(index);
        }
        return overlays[index];
    }

    /**
     * Builds the immutable public plan representation.
     */
    public CtmRenderPlan toPlan() {
        if (replacement == null && overlayCount == 0) {
            return null;
        }
        ArrayList<CtmOverlayTile> list = new ArrayList<>(overlayCount);
        for (int i = 0; i < overlayCount; i++) {
            list.add(overlays[i]);
        }
        return new CtmRenderPlan(replacement, list);
    }

    private void ensureCapacity(int target) {
        if (target <= overlays.length) {
            return;
        }
        CtmOverlayTile[] next = new CtmOverlayTile[Math.max(target,
                overlays.length * 2)];
        System.arraycopy(overlays, 0, next, 0, overlayCount);
        overlays = next;
    }
}
