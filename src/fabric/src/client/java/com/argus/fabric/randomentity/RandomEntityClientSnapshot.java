package com.argus.fabric.randomentity;

import com.argus.randomentity.RandomEntityContext;
import com.argus.randomentity.RandomEntityRuleSet;
import com.argus.resource.NamespaceId;
import net.minecraft.resources.Identifier;

import java.util.Set;

/**
 * Fabric-side immutable Random Entity texture snapshot.
 *
 * <p>Purpose: converts Minecraft {@link Identifier} texture locations into the
 * loader-agnostic shared rule set and converts selected replacements back to
 * Minecraft identifiers.
 *
 * <p>Threading: immutable after construction; read concurrently by render
 * state submission.
 *
 * <p>Performance: HOT PATH. One map lookup in the shared rule set plus the
 * small selected rule list. No allocation when the snapshot is empty or no
 * entry exists for the base texture.
 */
public final class RandomEntityClientSnapshot {
    private static final RandomEntityClientSnapshot EMPTY =
            new RandomEntityClientSnapshot(RandomEntityRuleSet.empty(), 0,
                    Set.of(), "_e");

    private final RandomEntityRuleSet ruleSet;
    private final int version;
    private final Set<Identifier> knownTextures;
    private final String emissiveSuffix;

    public RandomEntityClientSnapshot(RandomEntityRuleSet ruleSet,
                                      int version) {
        this(ruleSet, version, Set.of(), "_e");
    }

    public RandomEntityClientSnapshot(RandomEntityRuleSet ruleSet,
                                      int version,
                                      Set<Identifier> knownTextures,
                                      String emissiveSuffix) {
        this.ruleSet = ruleSet == null ? RandomEntityRuleSet.empty() : ruleSet;
        this.version = version;
        this.knownTextures = knownTextures == null ? Set.of()
                : Set.copyOf(knownTextures);
        this.emissiveSuffix = emissiveSuffix == null || emissiveSuffix.isBlank()
                ? "_e" : emissiveSuffix;
    }

    public static RandomEntityClientSnapshot empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return ruleSet.isEmpty() && knownTextures.isEmpty();
    }

    public boolean randomRulesEmpty() {
        return ruleSet.isEmpty();
    }

    public int version() {
        return version;
    }

    public Identifier remap(Identifier baseTexture,
                            RandomEntityContext context) {
        int index = resolveIndex(baseTexture, context);
        return remap(baseTexture, index);
    }

    public int resolveIndex(Identifier baseTexture,
                            RandomEntityContext context) {
        if (baseTexture == null || context == null || ruleSet.isEmpty()) {
            return 1;
        }
        NamespaceId base = new NamespaceId(baseTexture.getNamespace(),
                baseTexture.getPath());
        RandomEntityRuleSet.Entry entry = ruleSet.entry(base);
        if (entry == null) {
            return 1;
        }
        return entry.resolveIndex(context);
    }

    public Identifier remap(Identifier baseTexture, int variantIndex) {
        if (baseTexture == null || variantIndex <= 1 || ruleSet.isEmpty()) {
            return baseTexture;
        }
        NamespaceId base = new NamespaceId(baseTexture.getNamespace(),
                baseTexture.getPath());
        RandomEntityRuleSet.Entry entry = ruleSet.entry(base);
        if (entry == null) {
            return baseTexture;
        }
        NamespaceId selected = entry.textureForIndex(variantIndex);
        if (selected.equals(base)) {
            return baseTexture;
        }
        return Identifier.fromNamespaceAndPath(selected.namespace(),
                selected.path());
    }

    public Identifier remapPaintingSprite(Identifier paintingSprite,
                                          RandomEntityContext context) {
        if (paintingSprite == null || context == null || ruleSet.isEmpty()) {
            return paintingSprite;
        }
        Identifier baseTexture = Identifier.fromNamespaceAndPath(
                paintingSprite.getNamespace(),
                "textures/painting/" + paintingSprite.getPath() + ".png");
        Identifier selectedTexture = remap(baseTexture, context);
        if (selectedTexture.equals(baseTexture)) {
            return paintingSprite;
        }
        String path = selectedTexture.getPath();
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - 4);
        }
        return Identifier.fromNamespaceAndPath(selectedTexture.getNamespace(),
                path);
    }

    public Identifier emissiveTexture(Identifier baseTexture) {
        if (baseTexture == null) {
            return null;
        }
        Identifier emissive = suffixed(baseTexture, emissiveSuffix);
        return knownTextures.contains(emissive) ? emissive : null;
    }

    private static Identifier suffixed(Identifier id, String suffix) {
        String path = id.getPath();
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - 4) + suffix + ".png";
        } else {
            path = path + suffix;
        }
        return Identifier.fromNamespaceAndPath(id.getNamespace(), path);
    }
}
