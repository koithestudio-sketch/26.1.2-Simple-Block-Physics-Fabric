/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.fml.event.config.ModConfigEvent
 *  net.neoforged.neoforge.common.ModConfigSpec
 *  net.neoforged.neoforge.common.ModConfigSpec$BooleanValue
 *  net.neoforged.neoforge.common.ModConfigSpec$Builder
 *  net.neoforged.neoforge.common.ModConfigSpec$ConfigValue
 *  net.neoforged.neoforge.common.ModConfigSpec$DoubleValue
 *  net.neoforged.neoforge.common.ModConfigSpec$IntValue
 */
package io.github.ferrinember.simpleblockphysics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid="simpleblockphysics")
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.ConfigValue<List<? extends String>> INDUS_BLOCK_STRINGS = BUILDER.comment("A list of 'indestructible blocks' that will always count as support and never fall themselves.").defineListAllowEmpty("indestructibleBlocks", List.of("minecraft:bedrock", "minecraft:command_block", "minecraft:barrier", "minecraft:structure_block", "minecraft:structure_void", "minecraft:reinforced_deepslate", "minecraft:end_portal_frame"), Config::validateBlockName);
    private static final ModConfigSpec.BooleanValue INVERT_INDUS_BLOCKS = BUILDER.comment("Invert the list of 'indestructible blocks' such that only those blocks in the indestructible list will be considered for collapse. Keep in mind, blocks that cannot fall are also always valid supports.").define("invertIndestructibleBlocks", false);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_DIMENSIONS = BUILDER.comment("A list of dimensions allowed to have physics behavior. The Nether, and especially the End, are not recommended as both can easily experience catastrophic structural failure.").defineListAllowEmpty("allowedDimensions", List.of("minecraft:overworld"), e -> e instanceof String && ((String)e).contains(":"));
    private static final ModConfigSpec.DoubleValue BLOCK_BREAK_VOLUME = BUILDER.comment("Block Break Volume (caused by mod).").defineInRange("blockBreakVolume", 1.0, 0.0, Double.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUPPORT_LENGTH_MAX = BUILDER.comment("Support Strength Max. This value will be used by anything with a default hardness equal to or greater than 7 (iron blocks, obsidian, etc...).").defineInRange("supportLengthMax", 10, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUPPORT_LENGTH_MIN = BUILDER.comment("Support Strength Min. This value will be used by anything with a default hardness equal to 0 (honey blocks, slime blocks, etc...).").defineInRange("supportLengthMin", 1, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue DMG_DIST = BUILDER.comment("Base block entity fall damage inflicted per block fallen.").defineInRange("dmgDist", 1, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue DMG_MAX = BUILDER.comment("Max block entity fall damage inflicted. Max is taken from support strength (i.e. falling obsidian hurts more than dirt), but is overwritten if greater than this value.").defineInRange("dmgMax", 10, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue MAX_BREAK_PER_TICK = BUILDER.comment("Max number of blocks the mod can break per tick. Lower for better performance, raise for faster collapses, set to 0 to turn the mod off.").defineInRange("maxBreakPerTick", 500, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUPPORT_SEARCH_ITER = BUILDER.comment("Max number of iterative supports to scan.").defineInRange("supportSearchIter", 4, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue WEIGHT_SEARCH_ITER = BUILDER.comment("Max number of iterative supports to scan for weights.").defineInRange("weightSearchIter", 80, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue FALLING_BLOCK_BREAK_FACTOR = BUILDER.comment("A factor influencing the chance of a falling block to break on ground contact, rather than be placed or shift over, which is also influenced by hardness. Increase this value to make contact breaks more frequent (1 will always break), and decrease to make them less frequent (0 will never break).").defineInRange("fallingBlockBreakFactor", 0.5, 0.0, 1.0);
    private static final ModConfigSpec.DoubleValue FALLING_BLOCK_ITEM_DROP_CHANCE = BUILDER.comment("Percent chance that a destroyed falling block will drop its item. 100 will always drop, 0 will never drop.").defineInRange("fallingBlockItemDropChance", 0.5, 0.0, 1.0);
    private static final ModConfigSpec.DoubleValue FALLING_BLOCK_SHIFT_FACTOR = BUILDER.comment("Assuming the falling block doesn't break and has a valid spot to shift to, the percent chance that it does. 0 will turn shifting off, 1 means blocks will always shift given the chance.").defineInRange("fallingBlockShiftFactor", 0.8, 0.0, 1.0);
    private static final ModConfigSpec.BooleanValue REMOVE_BLOCKS_INSTEAD_OF_FALL = BUILDER.comment("Causes collapsing blocks to be destroyed directly instead of generating a falling block entity. Dramatically improves performance. Uses fallingBlockItemDropChance to determine how often they should drop their item.").define("removeBlocksInsteadOfFall", false);
    private static final ModConfigSpec.BooleanValue BUOYANCY_ENABLED = BUILDER.comment("Treats liquid source blocks as valid supports. Not the most realistic thing, but prevents ship structures from exploding.").define("buoyancyEnabled", true);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> OVERWRITE_TAG_STRINGS = BUILDER.comment("A list of vanilla blocktags to assign custom support strength values (rather than the hardness-based default). Listed blocktags without a matching support value at its index (below) will use default values.").defineListAllowEmpty("overwriteBlockTags", List.of("minecraft:leaves"), Config::validateTagName);
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> OVERWRITE_TAG_INTS = BUILDER.comment("A list of support strength values to override native (hardness based) blocktag support strength, matched by index (order) to above list.").defineListAllowEmpty("overwriteBlockTagValues", List.of(Integer.valueOf(4)), e -> e instanceof Integer);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> OVERWRITE_BLOCK_STRINGS = BUILDER.comment("As the blocktag list above, but with individual blocks. Specified blocks will overwrite both default and blocktag specified support values.").defineListAllowEmpty("overwriteBlocks", List.of("minecraft:moss_block"), Config::validateBlockName);
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> OVERWRITE_BLOCK_INTS = BUILDER.comment("A list of support strength values to override native (hardness based) individual block support strength, matched by index to above list.").defineListAllowEmpty("overwriteBlockValues", List.of(Integer.valueOf(3)), e -> e instanceof Integer);
    static final ModConfigSpec SPEC = BUILDER.build();
    public static Set<Block> indestructibleBlocks;
    public static Boolean invertIndestructibleBlocks;
    public static Set<ResourceKey<Level>> allowedDimensions;
    public static Double blockBreakVolume;
    public static Integer supportLengthMax;
    public static Integer supportLengthMin;
    public static Integer dmgDist;
    public static Integer dmgMax;
    public static Integer maxBreakPerTick;
    public static Integer supportSearchIter;
    public static Integer weightSearchIter;
    public static Double fallingBlockBreakFactor;
    public static Double fallingBlockItemDropChance;
    public static Double fallingBlockShiftFactor;
    public static Boolean removeBlocksInsteadOfFall;
    public static Boolean buoyancyEnabled;
    public static List<TagKey<Block>> overwrittenBlockTags;
    public static List<Integer> overwrittenBlockTagValues;
    public static HashMap<Block, Integer> overwrittenBlockMap;
    public static Set<Block> overwrittenBlocks;
    public static List<Integer> overwrittenBlockValues;
    public static HashMap<TagKey<Block>, Integer> overwrittenTagMap;

    private static boolean validateBlockName(Object obj) {
        String blockName;
        return obj instanceof String && BuiltInRegistries.BLOCK.containsKey(ResourceLocation.tryParse((String)(blockName = (String)obj)));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static boolean validateTagName(Object obj) {
        if (!(obj instanceof String)) return false;
        String tagName = (String)obj;
        if (!BuiltInRegistries.BLOCK.getTagNames().anyMatch(key -> key.location().equals((Object)ResourceLocation.tryParse((String)tagName)))) return false;
        return true;
    }

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        indestructibleBlocks = ((List)INDUS_BLOCK_STRINGS.get()).stream().map(blockName -> (Block)BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse((String)blockName))).collect(Collectors.toSet());
        invertIndestructibleBlocks = (Boolean)INVERT_INDUS_BLOCKS.get();
        blockBreakVolume = (Double)BLOCK_BREAK_VOLUME.get();
        supportLengthMax = (Integer)SUPPORT_LENGTH_MAX.get();
        supportLengthMin = (Integer)SUPPORT_LENGTH_MIN.get();
        dmgDist = (Integer)DMG_DIST.get();
        dmgMax = (Integer)DMG_MAX.get();
        maxBreakPerTick = (Integer)MAX_BREAK_PER_TICK.get();
        supportSearchIter = (Integer)SUPPORT_SEARCH_ITER.get();
        weightSearchIter = (Integer)WEIGHT_SEARCH_ITER.get();
        fallingBlockBreakFactor = (Double)FALLING_BLOCK_BREAK_FACTOR.get();
        fallingBlockItemDropChance = (Double)FALLING_BLOCK_ITEM_DROP_CHANCE.get();
        fallingBlockShiftFactor = (Double)FALLING_BLOCK_SHIFT_FACTOR.get();
        removeBlocksInsteadOfFall = (Boolean)REMOVE_BLOCKS_INSTEAD_OF_FALL.get();
        buoyancyEnabled = (Boolean)BUOYANCY_ENABLED.get();
        allowedDimensions = ((List)ALLOWED_DIMENSIONS.get()).stream().map(dimName -> ResourceKey.create((ResourceKey)Registries.DIMENSION, (ResourceLocation)ResourceLocation.tryParse((String)dimName))).collect(Collectors.toSet());
        overwrittenBlocks = ((List)OVERWRITE_BLOCK_STRINGS.get()).stream().map(blockName -> (Block)BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse((String)blockName))).collect(Collectors.toSet());
        overwrittenBlockValues = new ArrayList<Integer>((Collection)OVERWRITE_BLOCK_INTS.get());
        overwrittenBlockMap = new HashMap();
        overwrittenBlocks.forEach(block -> {
            if (overwrittenBlockValues.isEmpty()) {
                overwrittenBlockMap.put((Block)block, -1);
            } else {
                overwrittenBlockMap.put((Block)block, overwrittenBlockValues.remove(0));
            }
        });
        overwrittenBlockTags = ((List)OVERWRITE_TAG_STRINGS.get()).stream().map(tagName -> TagKey.create((ResourceKey)Registries.BLOCK, (ResourceLocation)ResourceLocation.tryParse((String)tagName))).collect(Collectors.toList());
        overwrittenBlockTagValues = new ArrayList<Integer>((Collection)OVERWRITE_TAG_INTS.get());
        overwrittenTagMap = new HashMap();
        overwrittenBlockTags.forEach(blockTagKey -> {
            if (overwrittenBlockTagValues.isEmpty()) {
                overwrittenTagMap.put((TagKey<Block>)blockTagKey, -1);
            } else {
                overwrittenTagMap.put((TagKey<Block>)blockTagKey, overwrittenBlockTagValues.remove(0));
            }
        });
    }
}
