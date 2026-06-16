package com.argus.client.animation;

import com.argus.client.platform.ClientEnvironment;
import com.argus.config.ArgusConfig;
import com.argus.config.ArgusConfigHolder;
import com.argus.resource.NamespaceId;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
            LoggerFactory.getLogger("argus/custom-animations");
    private static final AtomicReference<CustomAnimationClientSnapshot>
            SNAPSHOT = new AtomicReference<>(
            CustomAnimationClientSnapshot.empty());
    private static final Set<NamespaceId> USED_TERRAIN_SPRITES =
            ConcurrentHashMap.newKeySet();
    private static final ConcurrentMap<Identifier, NamespaceId> SPRITE_IDS =
            new ConcurrentHashMap<>();

    private static volatile State state = State.empty();
    private static boolean warnedAnimatica;

    private CustomAnimationRuntime() {
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

    /**
     * Returns whether Sodium terrain meshing should resolve and report source
     * sprites for custom-animation visibility.
     */
    public static boolean shouldMarkTerrainSprites() {
        return !SNAPSHOT.get().isEmpty()
                && ArgusConfigHolder.get().customAnimationsActive()
                && !ClientEnvironment.isModLoaded("animatica");
    }

    public static void markTerrainSprite(TextureAtlasSprite sprite) {
        CustomAnimationClientSnapshot snapshot = SNAPSHOT.get();
        if (sprite == null || snapshot.isEmpty()) {
            return;
        }
        Identifier id = sprite.contents().name();
        NamespaceId spriteId = SPRITE_IDS.computeIfAbsent(id,
                key -> new NamespaceId(key.getNamespace(), key.getPath()));
        if (snapshot.animatesSprite(spriteId)) {
            USED_TERRAIN_SPRITES.add(spriteId);
        }
    }

    public static boolean animatesSprite(TextureAtlasSprite sprite) {
        if (sprite == null || !ArgusConfigHolder.get().customAnimationsActive()
                || ClientEnvironment.isModLoaded("animatica")) {
            return false;
        }
        Identifier id = sprite.contents().name();
        return SNAPSHOT.get().animatesSprite(new NamespaceId(
                id.getNamespace(), id.getPath()));
    }

    public static void tick(Minecraft client) {
        CustomAnimationClientSnapshot snapshot = SNAPSHOT.get();
        if (snapshot.isEmpty() || client == null) {
            return;
        }
        ArgusConfig config = ArgusConfigHolder.get();
        if (!config.customAnimationsActive()) {
            return;
        }
        if (ClientEnvironment.isModLoaded("animatica")) {
            if (!warnedAnimatica) {
                warnedAnimatica = true;
                LOGGER.warn("[Argus] Animatica detected; Argus Custom "
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
            LOGGER.debug("[Argus] custom animation upload failed for {}: {}",
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
