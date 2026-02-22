package io.github.jake404notfound.simple_block_physics;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.stream.Collectors;

public class Config {
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

    public static void load(ConfigData data) {
        indestructibleBlocks = data.indestructibleBlocks.stream()
                .map(blockName -> BuiltInRegistries.BLOCK.getValue(Identifier.parse(blockName)))
                .collect(Collectors.toSet());

        invertIndestructibleBlocks = data.invertIndestructibleBlocks;
        blockBreakVolume = data.blockBreakVolume;
        supportLengthMax = data.supportLengthMax;
        supportLengthMin = data.supportLengthMin;
        dmgDist = data.dmgDist;
        dmgMax = data.dmgMax;
        maxBreakPerTick = data.maxBreakPerTick;
        supportSearchIter = data.supportSearchIter;
        weightSearchIter = data.weightSearchIter;
        fallingBlockBreakFactor = data.fallingBlockBreakFactor;
        fallingBlockItemDropChance = data.fallingBlockItemDropChance;
        fallingBlockShiftFactor = data.fallingBlockShiftFactor;
        removeBlocksInsteadOfFall = data.removeBlocksInsteadOfFall;
        buoyancyEnabled = data.buoyancyEnabled;

        allowedDimensions = data.allowedDimensions.stream()
                .map(dimName -> ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimName)))
                .collect(Collectors.toSet());

        overwrittenBlocks = data.overwriteBlocks.stream()
                .map(blockName -> BuiltInRegistries.BLOCK.getValue(Identifier.parse(blockName)))
                .collect(Collectors.toSet());

        overwrittenBlockValues = new ArrayList<>(data.overwriteBlockValues);
        overwrittenBlockMap = new HashMap<>();
        for (Block block : overwrittenBlocks) {
            if (overwrittenBlockValues.isEmpty()) {
                overwrittenBlockMap.put(block, -1);
            } else {
                overwrittenBlockMap.put(block, overwrittenBlockValues.removeFirst());
            }
        }

        overwrittenBlockTags = data.overwriteBlockTags.stream()
                .map(tagName -> TagKey.create(Registries.BLOCK, Identifier.parse(tagName)))
                .collect(Collectors.toList());

        overwrittenBlockTagValues = new ArrayList<>(data.overwriteBlockTagValues);
        overwrittenTagMap = new HashMap<>();
        for (TagKey<Block> blockTagKey : overwrittenBlockTags) {
            if (overwrittenBlockTagValues.isEmpty()) {
                overwrittenTagMap.put(blockTagKey, -1);
            } else {
                overwrittenTagMap.put(blockTagKey, overwrittenBlockTagValues.removeFirst());
            }
        }
    }
}
