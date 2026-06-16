package com.argus.fabric.animation;

import com.argus.Constants;
import com.argus.animation.CustomAnimationParseResult;
import com.argus.animation.CustomAnimationProperties;
import com.argus.animation.CustomAnimationRuleSet;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Fabric resource reload bridge for OptiFine custom texture animations.
 *
 * <p>Threading: file discovery, parsing, and image decoding run on the reload
 * prepare executor. Publication and old snapshot disposal happen after the
 * reload barrier.
 */
public final class CustomAnimationReloadListener implements
        PreparableReloadListener,
        IdentifiableResourceReloadListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Constants.MOD_ID + "/animations-reload");
    private static final String OPTIFINE_ANIM = "optifine/anim";

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,
                    "custom_animations_reload");

    public static void register() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new CustomAnimationReloadListener());
        CustomAnimationRuntime.register();
        LOGGER.info("[{}] Custom Animations reload listener registered",
                Constants.MOD_NAME);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public CompletableFuture<Void> reload(
            SharedState currentReload,
            Executor taskExecutor,
            PreparationBarrier preparationBarrier,
            Executor reloadExecutor) {
        ResourceManager resourceManager = currentReload.resourceManager();
        return CompletableFuture
                .supplyAsync(() -> load(resourceManager), taskExecutor)
                .thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(CustomAnimationReloadListener::publish,
                        reloadExecutor);
    }

    private static CustomAnimationClientSnapshot load(
            ResourceManager resourceManager) {
        ArrayList<CustomAnimationProperties.RuleSource> sources =
                new ArrayList<>();
        for (Identifier loc : resourceManager
                .listResources(OPTIFINE_ANIM,
                        id -> id.getPath().endsWith(".properties"))
                .keySet()) {
            Optional<Resource> resource = resourceManager.getResource(loc);
            if (resource.isEmpty()) {
                continue;
            }
            try (var in = resource.get().open();
                 var reader = new InputStreamReader(
                         in, StandardCharsets.UTF_8)) {
                sources.add(new CustomAnimationProperties.RuleSource(
                        readAll(reader), loc.toString()));
            } catch (Exception e) {
                LOGGER.warn("[{}] failed to read Custom Animation file {}: {}",
                        Constants.MOD_NAME, loc, e.getMessage());
            }
        }
        CustomAnimationParseResult parsed =
                CustomAnimationProperties.parseAll(sources);
        for (CustomAnimationParseResult.Error error : parsed.errors()) {
            LOGGER.warn("[{}] skipping malformed Custom Animation file {}: {}",
                    Constants.MOD_NAME, error.sourceFile(),
                    error.message());
        }
        CustomAnimationRuleSet ruleSet =
                CustomAnimationRuleSet.of(parsed.rules());
        CustomAnimationClientSnapshot snapshot =
                CustomAnimationClientSnapshot.from(
                        ruleSet, resourceManager, LOGGER);
        LOGGER.info("[{}] Custom Animations reload: {} parsed rules from {} "
                        + "files, {} runtime rules",
                Constants.MOD_NAME, ruleSet.all().size(), sources.size(),
                snapshot.size());
        return snapshot;
    }

    private static void publish(CustomAnimationClientSnapshot snapshot) {
        CustomAnimationRuntime.replace(snapshot);
        requestTerrainRebuild();
        LOGGER.info("[{}] Custom Animations snapshot installed: active={}",
                Constants.MOD_NAME, !snapshot.isEmpty());
    }

    private static void requestTerrainRebuild() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        minecraft.levelRenderer.invalidateCompiledGeometry(
                minecraft.level,
                minecraft.options,
                minecraft.gameRenderer.mainCamera(),
                minecraft.getBlockColors());
    }

    private static String readAll(java.io.Reader reader)
            throws java.io.IOException {
        StringBuilder out = new StringBuilder();
        char[] buf = new char[1024];
        int n;
        while ((n = reader.read(buf)) > 0) {
            out.append(buf, 0, n);
        }
        return out.toString();
    }
}
