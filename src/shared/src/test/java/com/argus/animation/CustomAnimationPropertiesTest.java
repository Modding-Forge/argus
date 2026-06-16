package com.argus.animation;

import com.argus.resource.NamespaceId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomAnimationPropertiesTest {

    @Test
    void minimalRuleResolvesRelativeSourceAndTextureTarget() {
        CustomAnimationRule rule = CustomAnimationProperties.parseString("""
                from=./stone_anim.png
                to=textures/block/stone.png
                """, "minecraft:optifine/anim/stone.properties");

        assertEquals(new NamespaceId("minecraft",
                "optifine/anim/stone_anim.png"), rule.fromTexture());
        assertEquals(new NamespaceId("minecraft",
                "textures/block/stone.png"), rule.toTexture());
        assertEquals(0, rule.x());
        assertEquals(0, rule.y());
        assertEquals(0, rule.width());
        assertEquals(0, rule.height());
        assertEquals(1, rule.defaultDuration());
        assertTrue(rule.frames().isEmpty());
    }

    @Test
    void namespacedPathsArePreserved() {
        CustomAnimationRule rule = CustomAnimationProperties.parseString("""
                from=example:optifine/anim/glow.png
                to=example:textures/block/glow.png
                """, "minecraft:optifine/anim/glow.properties");

        assertEquals(new NamespaceId("example",
                "optifine/anim/glow.png"), rule.fromTexture());
        assertEquals(new NamespaceId("example",
                "textures/block/glow.png"), rule.toTexture());
    }

    @Test
    void dimensionsDurationAndExplicitFramesAreParsed() {
        CustomAnimationRule rule = CustomAnimationProperties.parseString("""
                from=./clock.png
                to=textures/block/stone.png
                x=2
                y=4
                w=16
                h=16
                duration=3
                tile.0=4
                tile.1=1
                duration.1=9
                """, "minecraft:optifine/anim/clock.properties");

        assertEquals(2, rule.x());
        assertEquals(4, rule.y());
        assertEquals(16, rule.width());
        assertEquals(16, rule.height());
        assertEquals(3, rule.defaultDuration());
        assertEquals(List.of(
                new CustomAnimationFrame(4, 3),
                new CustomAnimationFrame(1, 9)), rule.frames());
    }

    @Test
    void invalidValuesProduceIsolatedErrors() {
        CustomAnimationParseResult result = CustomAnimationProperties.parseAll(
                List.of(
                        new CustomAnimationProperties.RuleSource("""
                                from=./a.png
                                to=textures/block/stone.png
                                """, "minecraft:optifine/anim/good.properties"),
                        new CustomAnimationProperties.RuleSource("""
                                from=./b.png
                                to=textures/block/dirt.png
                                w=-1
                                """, "minecraft:optifine/anim/bad.properties")));

        assertEquals(1, result.rules().size());
        assertEquals(1, result.errors().size());
        assertEquals("minecraft:optifine/anim/bad.properties",
                result.errors().getFirst().sourceFile());
        assertTrue(result.errors().getFirst().message().contains("w"));
    }

    @Test
    void ruleSetIsImmutableAndSourceSorted() {
        CustomAnimationRule b = CustomAnimationProperties.parseString("""
                from=./b.png
                to=textures/block/b.png
                """, "minecraft:optifine/anim/b.properties");
        CustomAnimationRule a = CustomAnimationProperties.parseString("""
                from=./a.png
                to=textures/block/a.png
                """, "minecraft:optifine/anim/a.properties");

        CustomAnimationRuleSet set = CustomAnimationRuleSet.of(List.of(b, a));

        assertEquals(List.of(a, b), set.all());
        assertThrows(UnsupportedOperationException.class,
                () -> set.all().add(a));
    }
}
