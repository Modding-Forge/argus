package com.argus.client.reload;

import com.argus.client.animation.CustomAnimationReloadListener;
import com.argus.client.bettergrass.BetterGrassReloadListener;
import com.argus.client.cem.CemReloadListener;
import com.argus.client.cit.CitReloadListener;
import com.argus.client.ctm.CtmReloadListener;
import com.argus.client.customcolors.CustomColorsReloadListener;
import com.argus.client.customgui.CustomGuiReloadListener;
import com.argus.client.customsky.CustomSkyReloadListener;
import com.argus.client.emissive.EmissiveReloadListener;
import com.argus.client.natural.NaturalTexturesReloadListener;
import com.argus.client.randomentity.RandomEntityReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.List;

/**
 * Loader-neutral factory for Argus client resource reload listeners.
 *
 * <p>Purpose: keeps Minecraft resource parsing and snapshot publication in the
 * shared client layer while Fabric and NeoForge only adapt the listeners to
 * their event APIs.
 *
 * <p>Threading: listener instances are created during loader startup on the
 * client bootstrap path. Each listener still follows Mojang's prepare/apply
 * reload threading contract.
 *
 * <p>Performance: startup-only allocation of a small fixed list.
 */
public final class ArgusClientReloadListeners {

    private ArgusClientReloadListeners() {
    }

    /**
     * Creates all client resource reload listeners in deterministic order.
     *
     * @return immutable listener entries for loader-specific registration
     */
    public static List<Entry> createAll() {
        return List.of(
                new Entry(CtmReloadListener.ID,
                        new CtmReloadListener()),
                new Entry(BetterGrassReloadListener.ID,
                        new BetterGrassReloadListener()),
                new Entry(EmissiveReloadListener.ID,
                        new EmissiveReloadListener()),
                new Entry(CitReloadListener.ID,
                        new CitReloadListener()),
                new Entry(CustomGuiReloadListener.ID,
                        new CustomGuiReloadListener()),
                new Entry(CustomAnimationReloadListener.ID,
                        new CustomAnimationReloadListener()),
                new Entry(CustomColorsReloadListener.ID,
                        new CustomColorsReloadListener()),
                new Entry(CustomSkyReloadListener.ID,
                        new CustomSkyReloadListener()),
                new Entry(NaturalTexturesReloadListener.ID,
                        new NaturalTexturesReloadListener()),
                new Entry(RandomEntityReloadListener.ID,
                        new RandomEntityReloadListener()),
                new Entry(CemReloadListener.ID,
                        new CemReloadListener()));
    }

    /**
     * One reload listener plus its stable registration id.
     *
     * @param id stable reload listener id
     * @param listener listener instance
     */
    public record Entry(Identifier id, PreparableReloadListener listener) {
    }
}
