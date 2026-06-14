package com.cinder.fabric.animation;

import com.cinder.config.CinderConfig;
import com.cinder.config.CinderConfigHolder;
import com.cinder.resource.NamespaceId;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Client-thread runtime for custom texture animations.
 *
 * <p>Performance: HOT PATH once per client tick. Allocation policy: no
 * per-rule allocation during normal ticking. Sodium section meshing marks
 * target usage through a concurrent set for later visibility optimisation; the
 * MVP still ticks active block-atlas rules so correctness does not depend on
 * chunk rebuild timing.
 */
public final class CustomAnimationRuntime {

    private static final Logger LOGGER =
            LoggerFactory.getLogger("cinder/custom-animations");
    private static final AtomicReference<CustomAnimationClientSnapshot>
            SNAPSHOT = new AtomicReference<>(
            CustomAnimationClientSnapshot.empty());
    private static final Set<NamespaceId> USED_TERRAIN_SPRITES =
            ConcurrentHashMap.newKeySet();

    private static volatile State state = State.empty();
    private static boolean registered;
    private static boolean warnedAnimatica;

    private CustomAnimationRuntime() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ClientTickEvents.END_CLIENT_TICK.register(
                CustomAnimationRuntime::tick);
    }

    public static void replace(CustomAnimationClientSnapshot snapshot) {
        CustomAnimationClientSnapshot next = snapshot == null
                ? CustomAnimationClientSnapshot.empty()
                : snapshot;
        CustomAnimationClientSnapshot old = SNAPSHOT.getAndSet(next);
        state = State.forSnapshot(next);
        USED_TERRAIN_SPRITES.clear();
        old.close();
    }

    public static CustomAnimationClientSnapshot snapshot() {
        return SNAPSHOT.get();
    }

    public static void markTerrainSprite(TextureAtlasSprite sprite) {
        if (sprite == null) {
            return;
        }
        Identifier id = sprite.contents().name();
        USED_TERRAIN_SPRITES.add(new NamespaceId(
                id.getNamespace(), id.getPath()));
    }

    public static boolean animatesSprite(TextureAtlasSprite sprite) {
        if (sprite == null || !CinderConfigHolder.get().customAnimationsActive()
                || FabricLoader.getInstance().isModLoaded("animatica")) {
            return false;
        }
        Identifier id = sprite.contents().name();
        return SNAPSHOT.get().animatesSprite(new NamespaceId(
                id.getNamespace(), id.getPath()));
    }

    private static void tick(Minecraft client) {
        CustomAnimationClientSnapshot snapshot = SNAPSHOT.get();
        if (snapshot.isEmpty() || client == null) {
            return;
        }
        CinderConfig config = CinderConfigHolder.get();
        if (!config.customAnimationsActive()) {
            return;
        }
        if (FabricLoader.getInstance().isModLoaded("animatica")) {
            if (!warnedAnimatica) {
                warnedAnimatica = true;
                LOGGER.warn("[Cinder] Animatica detected; Cinder Custom "
                        + "Animations runtime is disabled");
            }
            return;
        }
        TextureAtlas atlas = blockAtlas(client);
        if (atlas == null) {
            return;
        }
        State localState = state;
        int mipDistance = config.customAnimationMipmapDistance();
        CustomAnimationClientSnapshot.RuntimeRule[] rules = snapshot.rules();
        for (int i = 0; i < rules.length; i++) {
            CustomAnimationClientSnapshot.RuntimeRule rule = rules[i];
            if (!rule.targetPresent(atlas)) {
                continue;
            }
            if (!localState.tick(i, rule)) {
                continue;
            }
            upload(atlas, rule, localState.frame(i), mipDistance);
        }
    }

    private static TextureAtlas blockAtlas(Minecraft client) {
        if (client.getAtlasManager() == null) {
            return null;
        }
        try {
            return client.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return null;
        }
    }

    private static void upload(TextureAtlas atlas,
                               CustomAnimationClientSnapshot.RuntimeRule rule,
                               int frameIndex,
                               int mipDistance) {
        TextureAtlasSprite sprite = atlas.getSprite(rule.targetIdentifier());
        if (sprite == null
                || !sprite.contents().name().equals(rule.targetIdentifier())) {
            return;
        }
        if (rule.x() + rule.width() > sprite.contents().width()
                || rule.y() + rule.height() > sprite.contents().height()) {
            return;
        }
        try {
            var encoder = RenderSystem.getDevice().createCommandEncoder();
            int maxLevel = Math.min(Math.max(0, mipDistance),
                    Math.min(rule.frameMipImages()[frameIndex].length - 1,
                            atlas.getTexture().getMipLevels() - 1));
            for (int level = 0; level <= maxLevel; level++) {
                int atlasWidth = atlas.getTexture().getWidth(level);
                int atlasHeight = atlas.getTexture().getHeight(level);
                int destX = Math.round(sprite.getU0() * atlasWidth)
                        + (rule.x() >> level);
                int destY = Math.round(sprite.getV0() * atlasHeight)
                        + (rule.y() >> level);
                encoder.writeToTexture(atlas.getTexture(),
                        rule.frameMipImages()[frameIndex][level],
                        level, 0, destX, destY);
            }
        } catch (RuntimeException e) {
            LOGGER.debug("[Cinder] custom animation upload failed for {}: {}",
                    rule.sourceFile(), e.getMessage());
        }
    }

    private static final class State {
        private final int[] frame;
        private final int[] ticks;
        private final boolean[] dirty;

        private State(int[] frame, int[] ticks, boolean[] dirty) {
            this.frame = frame;
            this.ticks = ticks;
            this.dirty = dirty;
        }

        static State empty() {
            return new State(new int[0], new int[0], new boolean[0]);
        }

        static State forSnapshot(CustomAnimationClientSnapshot snapshot) {
            int size = snapshot == null ? 0 : snapshot.size();
            boolean[] dirty = new boolean[size];
            java.util.Arrays.fill(dirty, true);
            return new State(new int[size], new int[size], dirty);
        }

        boolean tick(int index,
                     CustomAnimationClientSnapshot.RuntimeRule rule) {
            if (index >= frame.length) {
                return false;
            }
            if (dirty[index]) {
                dirty[index] = false;
                return true;
            }
            ticks[index]++;
            int duration = rule.durations()[frame[index]];
            if (ticks[index] < duration) {
                return false;
            }
            ticks[index] = 0;
            frame[index] = (frame[index] + 1) % rule.tiles().length;
            return true;
        }

        int frame(int index) {
            return frame[index];
        }
    }
}
