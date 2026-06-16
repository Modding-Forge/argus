package com.cinder.fabric.randomentity;

import com.cinder.Constants;
import com.cinder.emissive.EmissiveProperties;
import com.cinder.randomentity.RandomEntityRuleParser;
import com.cinder.randomentity.RandomEntityRuleSet;
import com.cinder.randomentity.RandomEntityVariant;
import com.cinder.resource.NamespaceId;
import com.cinder.resource.PropertiesFile;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Fabric resource reload bridge for Random Entity texture rules.
 *
 * <p>Purpose: scans active packs for OptiFine-compatible random entity
 * resources, delegates parsing to shared code, and publishes an immutable
 * client snapshot atomically.
 *
 * <p>Threading: resource I/O and parsing run on the prepare executor;
 * publication runs after the reload barrier.
 */
public final class RandomEntityReloadListener
        implements PreparableReloadListener, IdentifiableResourceReloadListener {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(Constants.MOD_ID + "/random-entities");
    private static final String OPTIFINE_RANDOM = "optifine/random";
    private static final String OPTIFINE_MOB = "optifine/mob";
    private static final Identifier[] EMISSIVE_PROPERTIES = {
            Identifier.fromNamespaceAndPath("minecraft",
                    "optifine/emissive.properties"),
            Identifier.fromNamespaceAndPath("minecraft",
                    "textures/emissive.properties")
    };

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,
                    "random_entities_reload");

    public static void register() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new RandomEntityReloadListener());
        LOGGER.info("[{}] Random Entities reload listener registered",
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
        ResourceManager manager = currentReload.resourceManager();
        return CompletableFuture
                .supplyAsync(() -> load(manager), taskExecutor)
                .thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(RandomEntityReloadListener::publish,
                        reloadExecutor);
    }

    private static LoadedRandomEntities load(ResourceManager manager) {
        Map<NamespaceId, Group> groups = new HashMap<>();
        collect(manager, OPTIFINE_RANDOM, groups);
        collect(manager, OPTIFINE_MOB, groups);
        Set<Identifier> textures = collectTextures(manager);
        String emissiveSuffix = emissiveSuffix(manager);

        RandomEntityRuleSet.Builder builder = new RandomEntityRuleSet.Builder();
        int propertyFiles = 0;
        int errors = 0;
        for (Group group : groups.values()) {
            group.ensureBaseVariant();
            group.variants.sort(Comparator.comparingInt(
                    RandomEntityVariant::index));
            PropertiesFile properties = null;
            if (group.properties != null) {
                Optional<Resource> resource =
                        manager.getResource(group.properties);
                if (resource.isPresent()) {
                    try (var in = resource.get().open();
                         var reader = new InputStreamReader(in,
                                 StandardCharsets.UTF_8)) {
                        properties = PropertiesFile.parse(reader);
                        propertyFiles++;
                    } catch (Exception e) {
                        errors++;
                        LOGGER.warn("[{}] failed to parse Random Entities "
                                        + "file {}: {}",
                                Constants.MOD_NAME, group.properties,
                                e.getMessage());
                    }
                }
            }
            try {
                builder.add(properties == null
                        ? new RandomEntityRuleSet.Entry(group.baseTexture,
                        group.variants, java.util.List.of())
                        : RandomEntityRuleParser.parseEntry(group.baseTexture,
                        group.variants, properties));
            } catch (Exception e) {
                errors++;
                LOGGER.warn("[{}] skipping malformed Random Entities group "
                                + "{}: {}",
                        Constants.MOD_NAME, group.baseTexture,
                        e.getMessage());
            }
        }
        RandomEntityRuleSet rules = builder.build();
        LOGGER.info("[{}] Random Entities reload: {} texture groups, {} "
                        + "property files, {} known textures, {} errors",
                Constants.MOD_NAME, rules.entries().size(), propertyFiles,
                textures.size(), errors);
        return new LoadedRandomEntities(rules, textures, emissiveSuffix);
    }

    private static void collect(ResourceManager manager,
                                String root,
                                Map<NamespaceId, Group> groups) {
        for (Identifier loc : manager.listResources(root,
                id -> id.getPath().endsWith(".png")
                        || id.getPath().endsWith(".properties")).keySet()) {
            NamespaceId randomId = new NamespaceId(loc.getNamespace(),
                    loc.getPath());
            NamespaceId base =
                    RandomEntityRuleParser.baseTextureForRandomPath(randomId);
            Group group = groups.computeIfAbsent(base, Group::new);
            if (loc.getPath().endsWith(".properties")) {
                group.properties = loc;
            } else {
                group.variants.add(new RandomEntityVariant(
                        RandomEntityRuleParser.variantIndex(randomId),
                        randomId));
            }
        }
    }

    private static Set<Identifier> collectTextures(ResourceManager manager) {
        Set<Identifier> out = new HashSet<>();
        out.addAll(manager.listResources("textures",
                id -> id.getPath().endsWith(".png")).keySet());
        out.addAll(manager.listResources(OPTIFINE_RANDOM,
                id -> id.getPath().endsWith(".png")).keySet());
        out.addAll(manager.listResources(OPTIFINE_MOB,
                id -> id.getPath().endsWith(".png")).keySet());
        return out;
    }

    private static String emissiveSuffix(ResourceManager manager) {
        for (Identifier id : EMISSIVE_PROPERTIES) {
            Optional<Resource> resource = manager.getResource(id);
            if (resource.isEmpty()) {
                continue;
            }
            try (var in = resource.get().open();
                 var reader = new InputStreamReader(in,
                         StandardCharsets.UTF_8)) {
                return EmissiveProperties.parse(reader).suffix();
            } catch (Exception e) {
                LOGGER.warn("[{}] failed to parse entity emissive settings "
                                + "{}: {}",
                        Constants.MOD_NAME, id, e.getMessage());
            }
        }
        return "_e";
    }

    private static void publish(LoadedRandomEntities loaded) {
        RandomEntityRuntime.replace(new RandomEntityClientSnapshot(
                loaded.rules, RandomEntityRuntime.nextVersion(),
                loaded.textures, loaded.emissiveSuffix));
        if (FabricLoader.getInstance().isModLoaded("entity_texture_features")) {
            LOGGER.warn("[{}] ETF detected; Entity Texture snapshot was "
                            + "loaded but runtime selection is disabled",
                    Constants.MOD_NAME);
        }
        LOGGER.info("[{}] Entity Texture snapshot installed: {} random "
                        + "groups, {} known textures, active={}",
                Constants.MOD_NAME, loaded.rules.entries().size(),
                loaded.textures.size(),
                !loaded.rules.isEmpty() || !loaded.textures.isEmpty());
    }

    private record LoadedRandomEntities(RandomEntityRuleSet rules,
                                        Set<Identifier> textures,
                                        String emissiveSuffix) {
    }

    private static final class Group {
        private final NamespaceId baseTexture;
        private final ArrayList<RandomEntityVariant> variants =
                new ArrayList<>();
        private Identifier properties;

        private Group(NamespaceId baseTexture) {
            this.baseTexture = baseTexture;
        }

        private void ensureBaseVariant() {
            for (RandomEntityVariant variant : variants) {
                if (variant.index() == 1) {
                    return;
                }
            }
            variants.add(new RandomEntityVariant(1, baseTexture));
        }
    }
}
