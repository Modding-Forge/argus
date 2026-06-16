package com.argus.randomentity;

import com.argus.resource.NamespaceId;
import com.argus.resource.PropertiesFile;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RandomEntityRuleParserTest {
    @Test
    void discoversDigitSuffixVariants() {
        NamespaceId plain = NamespaceId.parse(
                "minecraft:optifine/random/entity/creeper/creeper2.png");
        NamespaceId dotted = NamespaceId.parse(
                "minecraft:optifine/random/entity/warden/"
                        + "warden_pulsating_spots_2.2.png");

        assertEquals(NamespaceId.parse(
                        "minecraft:textures/entity/creeper/creeper.png"),
                RandomEntityRuleParser.baseTextureForRandomPath(plain));
        assertEquals(2, RandomEntityRuleParser.variantIndex(plain));
        assertEquals(NamespaceId.parse(
                        "minecraft:textures/entity/warden/"
                                + "warden_pulsating_spots_2.png"),
                RandomEntityRuleParser.baseTextureForRandomPath(dotted));
        assertEquals(2, RandomEntityRuleParser.variantIndex(dotted));
    }

    @Test
    void discoversPaintingVariantsIncludingDigitNames() {
        NamespaceId plain = NamespaceId.parse(
                "minecraft:optifine/random/painting/kebab2.png");
        NamespaceId digitName = NamespaceId.parse(
                "minecraft:optifine/random/painting/aztec2.2.png");

        assertEquals(NamespaceId.parse(
                        "minecraft:textures/painting/kebab.png"),
                RandomEntityRuleParser.baseTextureForRandomPath(plain));
        assertEquals(2, RandomEntityRuleParser.variantIndex(plain));
        assertEquals(NamespaceId.parse(
                        "minecraft:textures/painting/aztec2.png"),
                RandomEntityRuleParser.baseTextureForRandomPath(digitName));
        assertEquals(2, RandomEntityRuleParser.variantIndex(digitName));
    }

    @Test
    void noPropertiesFallbackIsStablePerEntitySeed() {
        NamespaceId base = NamespaceId.parse(
                "minecraft:textures/entity/creeper/creeper.png");
        RandomEntityRuleSet.Entry entry = new RandomEntityRuleSet.Entry(base,
                List.of(new RandomEntityVariant(1, base),
                        new RandomEntityVariant(2, NamespaceId.parse(
                                "minecraft:optifine/random/entity/creeper/"
                                        + "creeper2.png"))),
                List.of());
        RandomEntityRuleSet rules = new RandomEntityRuleSet(
                Map.of(base, entry));

        RandomEntityContext context = RandomEntityContext.empty(7L);

        assertEquals(entry.resolve(context), rules.entry(base).resolve(context));
    }

    @Test
    void selectedVariantIndexCanDriveLayerTexture() {
        NamespaceId armor = NamespaceId.parse(
                "minecraft:textures/entity/creeper/creeper_armor.png");
        NamespaceId armor2 = NamespaceId.parse(
                "minecraft:optifine/random/entity/creeper/creeper_armor2.png");
        RandomEntityRuleSet.Entry entry = new RandomEntityRuleSet.Entry(armor,
                List.of(new RandomEntityVariant(1, armor),
                        new RandomEntityVariant(2, armor2)),
                List.of());

        assertEquals(armor2, entry.textureForIndex(2));
        assertEquals(armor, entry.textureForIndex(3));
    }

    @Test
    void parsesConditionsAndWeights() throws Exception {
        NamespaceId base = NamespaceId.parse(
                "minecraft:textures/entity/sheep/sheep.png");
        PropertiesFile properties = PropertiesFile.parse(new StringReader("""
                textures.1=2 3
                weights.1=0 1
                biomes.1=minecraft:plains
                heights.1=60-80
                name.1=ipattern:*jeb*
                colors.1=blue
                baby.1=false
                health.1=50-100%
                weather.1=clear
                """));
        RandomEntityRuleSet.Entry entry = RandomEntityRuleParser.parseEntry(
                base,
                List.of(new RandomEntityVariant(1, base),
                        new RandomEntityVariant(2, NamespaceId.parse(
                                "minecraft:optifine/random/entity/sheep/"
                                        + "sheep2.png")),
                        new RandomEntityVariant(3, NamespaceId.parse(
                                "minecraft:optifine/random/entity/sheep/"
                                        + "sheep3.png"))),
                properties);

        RandomEntityContext matching = new RandomEntityContext(42L, 42L,
                NamespaceId.parse("minecraft:plains"), 70, "jeb sheep",
                null, 0, "blue", false, 10, 20, 0, 6000,
                "clear", 100, null, Map.of(), Map.of());
        RandomEntityContext wrongBiome = new RandomEntityContext(42L, 42L,
                NamespaceId.parse("minecraft:desert"), 70, "jeb sheep",
                null, 0, "blue", false, 10, 20, 0, 6000,
                "clear", 100, null, Map.of(), Map.of());

        assertEquals(NamespaceId.parse(
                        "minecraft:optifine/random/entity/sheep/sheep3.png"),
                entry.resolve(matching));
        assertEquals(base, entry.resolve(wrongBiome));
    }

    @Test
    void parsesLegacyMinMaxHeightAndNbtConditions() throws Exception {
        NamespaceId base = NamespaceId.parse(
                "minecraft:textures/entity/cat/cat.png");
        NamespaceId variant = NamespaceId.parse(
                "minecraft:optifine/random/entity/cat/cat2.png");
        PropertiesFile properties = PropertiesFile.parse(new StringReader("""
                textures.1=2
                minHeight.1=63
                maxHeight.1=80
                nbt.1.CustomName=pattern:Smoke*
                """));
        RandomEntityRuleSet.Entry entry = RandomEntityRuleParser.parseEntry(
                base,
                List.of(new RandomEntityVariant(1, base),
                        new RandomEntityVariant(2, variant)),
                properties);

        RandomEntityContext matching = new RandomEntityContext(9L, 9L,
                NamespaceId.parse("minecraft:plains"), 70, "Smoke Cat",
                null, 0, null, false, 0, 0, 0, 6000, "clear",
                100, null, Map.of("CustomName", "Smoke Cat"), Map.of());
        RandomEntityContext wrongName = new RandomEntityContext(9L, 9L,
                NamespaceId.parse("minecraft:plains"), 70, "Other Cat",
                null, 0, null, false, 0, 0, 0, 6000, "clear",
                100, null, Map.of("CustomName", "Other Cat"), Map.of());

        assertEquals(variant, entry.resolve(matching));
        assertEquals(base, entry.resolve(wrongName));
    }

    @Test
    void parsesProfessionLevelConditions() throws Exception {
        NamespaceId base = NamespaceId.parse(
                "minecraft:textures/entity/villager/villager.png");
        NamespaceId variant = NamespaceId.parse(
                "minecraft:optifine/random/entity/villager/villager2.png");
        PropertiesFile properties = PropertiesFile.parse(new StringReader("""
                textures.1=2
                professions.1=farmer:2 librarian:3
                """));
        RandomEntityRuleSet.Entry entry = RandomEntityRuleParser.parseEntry(
                base,
                List.of(new RandomEntityVariant(1, base),
                        new RandomEntityVariant(2, variant)),
                properties);

        RandomEntityContext farmer = new RandomEntityContext(12L, 12L,
                NamespaceId.parse("minecraft:plains"), 64, null,
                "farmer", 2, null, false, 0, 0, 0, 6000,
                "clear", 100, null, Map.of(), Map.of());
        RandomEntityContext lowFarmer = new RandomEntityContext(12L, 12L,
                NamespaceId.parse("minecraft:plains"), 64, null,
                "farmer", 1, null, false, 0, 0, 0, 6000,
                "clear", 100, null, Map.of(), Map.of());

        assertEquals(variant, entry.resolve(farmer));
        assertEquals(base, entry.resolve(lowFarmer));
    }
}
