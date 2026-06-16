package com.cinder.fabric.randomentity;

import com.cinder.config.CinderConfigHolder;
import com.cinder.randomentity.RandomEntityContext;
import com.cinder.resource.NamespaceId;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Random Entity texture runtime bridge.
 *
 * <p>Purpose: stores the active immutable resource-pack snapshot and creates a
 * loader-agnostic context during entity render-state extraction.
 *
 * <p>Threading: snapshot publication is atomic. Context objects are immutable
 * and stored on per-render states, never in global mutable current-entity
 * fields.
 *
 * <p>Performance: HOT PATH in {@link #remap(Identifier, RandomEntityContext)}.
 * The method performs config/compat gates, an empty-snapshot check, and one
 * O(1) rule lookup.
 */
public final class RandomEntityRuntime {
    private static final Logger LOGGER =
            LoggerFactory.getLogger("cinder/random-entities");
    private static final AtomicReference<RandomEntityClientSnapshot> SNAPSHOT =
            new AtomicReference<>(RandomEntityClientSnapshot.empty());
    private static final AtomicInteger VERSION = new AtomicInteger();
    private static volatile boolean warnedEtf;
    private static volatile long lastBlockEntityDebugNanos;

    private RandomEntityRuntime() {
    }

    public static int nextVersion() {
        return VERSION.incrementAndGet();
    }

    public static void replace(RandomEntityClientSnapshot snapshot) {
        SNAPSHOT.set(snapshot == null ? RandomEntityClientSnapshot.empty()
                : snapshot);
    }

    public static RandomEntityContext capture(Entity entity) {
        if (entity == null) {
            return null;
        }
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        NamespaceId biome = biomeId(level, pos);
        BlockState blockState = blockState(entity, level, pos);
        NamespaceId block = blockId(blockState);
        String name = entity.hasCustomName() && entity.getCustomName() != null
                ? entity.getCustomName().getString() : null;
        boolean baby = entity instanceof LivingEntity living && living.isBaby();
        int health = 0;
        int maxHealth = 0;
        if (entity instanceof LivingEntity living) {
            health = Math.round(living.getHealth());
            maxHealth = Math.round(living.getMaxHealth());
        }
        String weather = weather(level);
        String color = color(entity);
        Profession profession = profession(entity);
        int size = size(entity);
        Map<String, String> nbt = nbtFacts(name, color, baby, health,
                maxHealth, profession.name, profession.level, blockState);
        long day = level == null ? 0L : level.getOverworldClockTime();
        int dayTime = (int) Math.floorMod(day, 24000L);
        int moonPhase = (int) Math.floorMod(day / 24000L, 8L);
        Entity vehicle = entity.getVehicle();
        return new RandomEntityContext(seed(entity),
                vehicle == null ? seed(entity) : seed(vehicle),
                biome,
                pos.getY(),
                name,
                profession.name,
                profession.level,
                color,
                baby,
                health,
                maxHealth,
                moonPhase,
                dayTime,
                weather,
                size,
                block,
                nbt,
                blockProperties(blockState));
    }

    public static RandomEntityContext capture(BlockEntity blockEntity) {
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return null;
        }
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        BlockState state = blockEntity.getBlockState();
        NamespaceId biome = biomeId(level, pos);
        NamespaceId block = blockId(state);
        String name = blockEntity instanceof Nameable nameable
                && nameable.hasCustomName()
                ? nameable.getName().getString() : null;
        String color = blockEntityColor(blockEntity);
        Map<String, String> nbt = nbtFacts(name, color, false, -1, -1,
                null, -1, state);
        long day = level.getOverworldClockTime();
        int dayTime = (int) Math.floorMod(day, 24000L);
        int moonPhase = (int) Math.floorMod(day / 24000L, 8L);
        long seed = blockSeed(pos);
        return new RandomEntityContext(seed, seed, biome, pos.getY(), name,
                null, -1, color, false, -1, -1, moonPhase, dayTime,
                weather(level), -1, block, nbt, blockProperties(state));
    }

    public static Identifier remap(Identifier texture,
                                   RandomEntityContext context) {
        if (!active()) {
            return texture;
        }
        RandomEntityClientSnapshot snapshot = SNAPSHOT.get();
        if (snapshot.isEmpty()) {
            return texture;
        }
        return snapshot.remap(texture, context);
    }

    public static int resolveIndex(Identifier texture,
                                   RandomEntityContext context) {
        if (!active()) {
            return 1;
        }
        RandomEntityClientSnapshot snapshot = SNAPSHOT.get();
        if (snapshot.isEmpty()) {
            return 1;
        }
        return snapshot.resolveIndex(texture, context);
    }

    public static Identifier remap(Identifier texture, int variantIndex) {
        if (!active()) {
            return texture;
        }
        RandomEntityClientSnapshot snapshot = SNAPSHOT.get();
        if (snapshot.isEmpty()) {
            return texture;
        }
        return snapshot.remap(texture, variantIndex);
    }

    public static SpriteId remapSprite(SpriteId sprite,
                                       RandomEntityContext context,
                                       boolean blockEntity) {
        if ((!blockEntity && !active())
                || (blockEntity && !activeBlockEntities())
                || sprite == null) {
            return sprite;
        }
        RandomEntityClientSnapshot snapshot = SNAPSHOT.get();
        if (snapshot.isEmpty()) {
            return sprite;
        }
        Identifier baseTexture = Identifier.fromNamespaceAndPath(
                sprite.texture().getNamespace(),
                "textures/" + sprite.texture().getPath() + ".png");
        Identifier remapped = snapshot.remap(baseTexture, context);
        if (remapped.equals(baseTexture)) {
            return sprite;
        }
        String path = atlasSpritePath(sprite, remapped);
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - 4);
        }
        SpriteId out = new SpriteId(sprite.atlasLocation(),
                Identifier.fromNamespaceAndPath(remapped.getNamespace(),
                        path));
        debugBlockEntitySprite(sprite, baseTexture, remapped, out);
        return out;
    }

    public static TextureAtlasSprite remapPaintingSprite(TextureAtlas atlas,
                                                         Identifier spriteId,
                                                         RandomEntityContext context) {
        if (!active() || atlas == null || spriteId == null) {
            return null;
        }
        RandomEntityClientSnapshot snapshot = SNAPSHOT.get();
        if (snapshot.isEmpty()) {
            return null;
        }
        Identifier remapped = snapshot.remapPaintingSprite(spriteId, context);
        if (remapped.equals(spriteId)) {
            return null;
        }
        return atlas.getSprite(remapped);
    }

    public static Identifier emissiveTexture(Identifier texture) {
        if (!activeEntityTextures()
                || !CinderConfigHolder.get().entityEmissiveTexturesActive()) {
            return null;
        }
        RandomEntityClientSnapshot snapshot = SNAPSHOT.get();
        if (snapshot.isEmpty()) {
            return null;
        }
        return snapshot.emissiveTexture(texture);
    }

    public static boolean active() {
        if (!activeEntityTextures()
                || !CinderConfigHolder.get().randomEntitiesActive()) {
            return false;
        }
        return true;
    }

    public static boolean activeEntityTextures() {
        if (!CinderConfigHolder.get().entityTextureFeaturesActive()) {
            return false;
        }
        if (FabricLoader.getInstance().isModLoaded("entity_texture_features")) {
            if (!warnedEtf) {
                warnedEtf = true;
                LOGGER.warn("[Cinder] ETF detected; Cinder Entity Texture "
                        + "Features are disabled");
            }
            return false;
        }
        return true;
    }

    public static boolean activeBlockEntities() {
        return activeEntityTextures()
                && CinderConfigHolder.get().randomBlockEntityTexturesActive();
    }

    private static long seed(Entity entity) {
        UUID uuid = entity.getUUID();
        return uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits()
                ^ Integer.toUnsignedLong(entity.getId());
    }

    private static NamespaceId biomeId(Level level, BlockPos pos) {
        if (level == null) {
            return null;
        }
        Optional<ResourceKey<Biome>> key = level.getBiome(pos).unwrapKey();
        return key.map(value -> new NamespaceId(
                value.identifier().getNamespace(),
                value.identifier().getPath())).orElse(null);
    }

    private static NamespaceId blockBelowId(Level level, BlockPos pos) {
        if (level == null) {
            return null;
        }
        Identifier id = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(pos.below()).getBlock());
        return new NamespaceId(id.getNamespace(), id.getPath());
    }

    private static NamespaceId blockId(BlockState state) {
        if (state == null) {
            return null;
        }
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return new NamespaceId(id.getNamespace(), id.getPath());
    }

    private static BlockState blockState(Entity entity, Level level,
                                         BlockPos pos) {
        if (entity instanceof ItemEntity item
                && item.getItem().getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState();
        }
        if (level == null) {
            return null;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            state = level.getBlockState(pos.below());
        }
        return state;
    }

    private static String weather(Level level) {
        if (level == null) {
            return "clear";
        }
        if (level.isThundering()) {
            return "thunder";
        }
        return level.isRaining() ? "rain" : "clear";
    }

    private static String color(Entity entity) {
        DyeColor color = null;
        if (entity instanceof Sheep sheep) {
            color = sheep.getColor();
        } else if (entity instanceof Wolf wolf) {
            color = wolf.getCollarColor();
        } else if (entity instanceof Cat cat) {
            color = cat.getCollarColor();
        } else if (entity instanceof Llama llama) {
            color = llamaColor(llama);
        }
        return color == null ? null : color.getName();
    }

    private static DyeColor llamaColor(Llama llama) {
        Block block = Block.byItem(llama.getItemBySlot(
                EquipmentSlot.BODY).getItem());
        return block instanceof WoolCarpetBlock carpet
                ? carpet.getColor() : null;
    }

    private static String blockEntityColor(BlockEntity blockEntity) {
        DyeColor color = null;
        if (blockEntity.getBlockState().getBlock() instanceof BedBlock bed) {
            color = bed.getColor();
        } else if (blockEntity instanceof ShulkerBoxBlockEntity shulker) {
            color = shulker.getColor();
        }
        return color == null ? null : color.getName();
    }

    private static Profession profession(Entity entity) {
        if (entity instanceof Villager villager) {
            var data = villager.getVillagerData();
            Identifier id = BuiltInRegistries.VILLAGER_PROFESSION.getKey(
                    data.profession().value());
            String name = id == null ? null : id.getPath();
            return new Profession(name, data.level());
        }
        return new Profession(null, -1);
    }

    private static int size(Entity entity) {
        if (entity instanceof Slime slime) {
            return slime.getSize() - 1;
        }
        if (entity instanceof Phantom phantom) {
            return phantom.getPhantomSize();
        }
        return Math.max(1, Math.round(entity.getBbWidth() * 100.0f));
    }

    private static Map<String, String> nbtFacts(String name,
                                                String color,
                                                boolean baby,
                                                int health,
                                                int maxHealth,
                                                String profession,
                                                int professionLevel,
                                                BlockState blockState) {
        Map<String, String> out = new HashMap<>();
        if (name != null) {
            out.put("CustomName", name);
            out.put("custom_name", name);
            out.put("Name", name);
        }
        if (color != null) {
            out.put("Color", color);
            out.put("CollarColor", color);
        }
        out.put("Baby", Boolean.toString(baby));
        if (health >= 0) {
            out.put("Health", Integer.toString(health));
        }
        if (maxHealth >= 0) {
            out.put("MaxHealth", Integer.toString(maxHealth));
        }
        if (profession != null) {
            out.put("Profession", profession);
            out.put("VillagerData.profession", profession);
        }
        if (professionLevel >= 0) {
            out.put("ProfessionLevel", Integer.toString(professionLevel));
            out.put("VillagerData.level", Integer.toString(professionLevel));
        }
        if (blockState != null) {
            out.put("Block", blockId(blockState).toString());
            for (Map.Entry<String, String> entry
                    : blockProperties(blockState).entrySet()) {
                out.put("BlockState." + entry.getKey(), entry.getValue());
            }
        }
        return out;
    }

    private static Map<String, String> blockProperties(BlockState state) {
        if (state == null || state.getProperties().isEmpty()) {
            return Map.of();
        }
        Map<String, String> out = new HashMap<>();
        for (Property<?> property : state.getProperties()) {
            out.put(property.getName(), propertyValue(state, property));
        }
        return out;
    }

    private static <T extends Comparable<T>> String propertyValue(
            BlockState state,
            Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private static long blockSeed(BlockPos pos) {
        long x = Integer.toUnsignedLong(pos.getX());
        long y = Integer.toUnsignedLong(pos.getY());
        long z = Integer.toUnsignedLong(pos.getZ());
        return (x * 3129871L) ^ (z * 116129781L) ^ y;
    }

    private static String atlasSpritePath(SpriteId original,
                                          Identifier selectedTexture) {
        String path = selectedTexture.getPath();
        if (path.startsWith("optifine/random/entity/")) {
            return "entity/" + path.substring(
                    "optifine/random/entity/".length());
        }
        if (path.startsWith("optifine/mob/")) {
            return "entity/" + path.substring("optifine/mob/".length());
        }
        return path;
    }

    private static void debugBlockEntitySprite(SpriteId original,
                                               Identifier baseTexture,
                                               Identifier selectedTexture,
                                               SpriteId remapped) {
        if (!CinderConfigHolder.get().entityTextureDebug()) {
            return;
        }
        long now = System.nanoTime();
        if (now - lastBlockEntityDebugNanos < 1_000_000_000L) {
            return;
        }
        lastBlockEntityDebugNanos = now;
        LOGGER.info("[Cinder] Random Tile Entity sprite remap: atlas={} "
                        + "sprite={} base={} selected={} remappedSprite={}",
                original.atlasLocation(), original.texture(), baseTexture,
                selectedTexture, remapped.texture());
    }

    private record Profession(String name, int level) {
    }
}
