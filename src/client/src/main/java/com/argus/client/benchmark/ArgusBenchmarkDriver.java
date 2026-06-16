package com.argus.client.benchmark;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Dev-only in-game benchmark driver for repeatable loader comparisons.
 *
 * <p>Purpose: when explicitly enabled with
 * {@code -Dargus.benchmark.autopilot=true}, opens a local world, closes menus,
 * rotates the camera, and holds movement/use/attack keys through a deterministic
 * route. By default it also closes the client after the last sample, so Gradle
 * benchmark runs return without manual cleanup. This keeps Fabric and NeoForge
 * benchmarks on the same client-side workload without requiring manual world
 * joins.
 *
 * <p>Threading: called from the client tick event on the client thread only.
 *
 * <p>Performance: disabled cost is one static-final boolean branch. This class
 * is intended for development runs, not release benchmarking.
 */
public final class ArgusBenchmarkDriver {

    private static final boolean ENABLED =
            Boolean.getBoolean("argus.benchmark.autopilot");
    private static final Logger LOGGER =
            LoggerFactory.getLogger("argus/benchmark-driver");
    private static final int MAX_TICKS = Integer.getInteger(
            "argus.benchmark.autopilotTicks", 900);
    private static final boolean CLOSE_ON_COMPLETE = Boolean.parseBoolean(
            System.getProperty("argus.benchmark.closeOnComplete", "true"));
    private static final int CLOSE_DELAY_TICKS = Integer.getInteger(
            "argus.benchmark.closeDelayTicks", 40);
    private static final int LOG_INTERVAL_TICKS = 100;

    private static boolean openAttempted;
    private static boolean started;
    private static boolean complete;
    private static boolean closing;
    private static int closeDelayTicks;
    private static int ticks;
    private static int fpsMin = Integer.MAX_VALUE;
    private static int fpsMax;
    private static long fpsSum;
    private static int fpsSamples;

    private ArgusBenchmarkDriver() {
    }

    /**
     * Advances the benchmark driver by one client tick.
     *
     * @param minecraft active client instance
     */
    public static void tick(Minecraft minecraft) {
        if (!ENABLED || minecraft == null) {
            return;
        }
        if (complete) {
            closeAfterCompletion(minecraft);
            return;
        }
        if (minecraft.level == null || minecraft.player == null) {
            openWorldIfNeeded(minecraft);
            return;
        }

        if (!started) {
            started = true;
            ticks = 0;
            LOGGER.info("[Argus] benchmark-driver started world={} pos={}",
                    worldLabel(minecraft), positionLabel(minecraft.player));
        }

        ticks++;
        if (minecraft.gui.screen() != null) {
            minecraft.gui.setScreen(null);
        }

        driveCamera(minecraft.player, ticks);
        driveMovement(minecraft, ticks);
        sampleFps(minecraft);

        if (ticks % LOG_INTERVAL_TICKS == 0) {
            LOGGER.info(
                    "[Argus] benchmark-driver tick={} fpsCurrent={} fpsMin={} fpsAvg={} fpsMax={} pos={}",
                    ticks,
                    minecraft.getFps(),
                    fpsMin == Integer.MAX_VALUE ? 0 : fpsMin,
                    fpsSamples == 0 ? 0 : fpsSum / fpsSamples,
                    fpsMax,
                    positionLabel(minecraft.player));
        }

        if (ticks >= MAX_TICKS) {
            releaseKeys(minecraft);
            complete = true;
            closeDelayTicks = CLOSE_DELAY_TICKS;
            LOGGER.info(
                    "[Argus] benchmark-driver complete ticks={} fpsMin={} fpsAvg={} fpsMax={} samples={} closeOnComplete={}",
                    ticks,
                    fpsMin == Integer.MAX_VALUE ? 0 : fpsMin,
                    fpsSamples == 0 ? 0 : fpsSum / fpsSamples,
                    fpsMax,
                    fpsSamples,
                    CLOSE_ON_COMPLETE);
        }
    }

    private static void closeAfterCompletion(Minecraft minecraft) {
        if (!CLOSE_ON_COMPLETE || closing) {
            return;
        }
        if (closeDelayTicks > 0) {
            closeDelayTicks--;
            return;
        }
        closing = true;
        releaseKeys(minecraft);
        LOGGER.info("[Argus] benchmark-driver requesting client stop");
        minecraft.stop();
    }

    private static void openWorldIfNeeded(Minecraft minecraft) {
        if (openAttempted) {
            return;
        }
        String world = configuredWorld(minecraft);
        if (world == null || world.isBlank()) {
            openAttempted = true;
            LOGGER.warn("[Argus] benchmark-driver found no local world in {}",
                    new File(minecraft.gameDirectory, "saves"));
            return;
        }
        openAttempted = true;
        LOGGER.info("[Argus] benchmark-driver opening world={}", world);
        minecraft.createWorldOpenFlows()
                .openWorld(world,
                        () -> minecraft.gui.setScreen(new TitleScreen()));
    }

    private static String configuredWorld(Minecraft minecraft) {
        String configured = System.getProperty("argus.benchmark.world", "")
                .trim();
        if (!configured.isEmpty()) {
            return configured;
        }

        File saves = new File(minecraft.gameDirectory, "saves");
        File[] worlds = saves.listFiles(File::isDirectory);
        if (worlds == null || worlds.length == 0) {
            return null;
        }
        for (File world : worlds) {
            Path levelDat = world.toPath().resolve("level.dat");
            if (Files.isRegularFile(levelDat)) {
                return world.getName();
            }
        }
        return null;
    }

    private static void driveCamera(LocalPlayer player, int tick) {
        float yaw = player.getYRot() + 2.0F;
        float pitch = -8.0F + (float) Math.sin(tick / 35.0D) * 18.0F;
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        player.setXRot(pitch);
    }

    private static void driveMovement(Minecraft minecraft, int tick) {
        boolean forward = tick < MAX_TICKS - 80;
        boolean right = tick % 240 < 120;
        boolean left = !right && tick < MAX_TICKS - 80;
        boolean jump = tick % 90 < 8;
        boolean use = tick > 100 && tick % 70 < 5;
        boolean attack = tick > 140 && tick % 110 < 5;

        minecraft.options.keyUp.setDown(forward);
        minecraft.options.keyRight.setDown(right);
        minecraft.options.keyLeft.setDown(left);
        minecraft.options.keyDown.setDown(false);
        minecraft.options.keyJump.setDown(jump);
        minecraft.options.keyUse.setDown(use);
        minecraft.options.keyAttack.setDown(attack);
    }

    private static void releaseKeys(Minecraft minecraft) {
        minecraft.options.keyUp.setDown(false);
        minecraft.options.keyRight.setDown(false);
        minecraft.options.keyLeft.setDown(false);
        minecraft.options.keyDown.setDown(false);
        minecraft.options.keyJump.setDown(false);
        minecraft.options.keyUse.setDown(false);
        minecraft.options.keyAttack.setDown(false);
    }

    private static void sampleFps(Minecraft minecraft) {
        int fps = minecraft.getFps();
        if (fps <= 0) {
            return;
        }
        fpsMin = Math.min(fpsMin, fps);
        fpsMax = Math.max(fpsMax, fps);
        fpsSum += fps;
        fpsSamples++;
    }

    private static String worldLabel(Minecraft minecraft) {
        return minecraft.level == null
                ? "none"
                : minecraft.level.dimension().toString();
    }

    private static String positionLabel(LocalPlayer player) {
        return String.format(Locale.ROOT, "%.1f,%.1f,%.1f",
                player.getX(),
                player.getY(),
                player.getZ());
    }
}
