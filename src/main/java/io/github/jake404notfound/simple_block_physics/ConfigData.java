package io.github.jake404notfound.simple_block_physics;

import java.util.List;

public class ConfigData {
    public List<String> indestructibleBlocks = List.of("minecraft:bedrock", "minecraft:command_block",
            "minecraft:barrier", "minecraft:structure_block", "minecraft:structure_void",
            "minecraft:reinforced_deepslate", "minecraft:end_portal_frame");
    public boolean invertIndestructibleBlocks = false;
    public List<String> allowedDimensions = List.of("minecraft:overworld");
    public double blockBreakVolume = 1.0;
    public int supportLengthMax = 10;
    public int supportLengthMin = 1;
    public int dmgDist = 1;
    public int dmgMax = 10;
    public int maxBreakPerTick = 500;
    public int supportSearchIter = 4;
    public int weightSearchIter = 80;
    public double fallingBlockBreakFactor = 0.5;
    public double fallingBlockItemDropChance = 0.5;
    public double fallingBlockShiftFactor = 0.8;
    public boolean removeBlocksInsteadOfFall = false;
    public boolean buoyancyEnabled = true;
    public List<String> overwriteBlockTags = List.of("minecraft:leaves");
    public List<Integer> overwriteBlockTagValues = List.of(4);
    public List<String> overwriteBlocks = List.of("minecraft:moss_block");
    public List<Integer> overwriteBlockValues = List.of(3);
}
