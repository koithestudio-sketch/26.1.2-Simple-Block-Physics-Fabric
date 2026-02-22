/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Vec3i
 *  net.minecraft.server.level.FullChunkStatus
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.world.entity.item.FallingBlockEntity
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.MossBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.neoforge.event.tick.LevelTickEvent$Pre
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Pre
 */
package io.github.ferrinember.simpleblockphysics.utils;

import io.github.ferrinember.simpleblockphysics.Config;
import io.github.ferrinember.simpleblockphysics.utils.PosLevelKey;
import io.github.ferrinember.simpleblockphysics.utils.SupportChecker;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MossBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class TickHandler {
    public static LinkedHashMap<PosLevelKey, Integer> checkMap = new LinkedHashMap();
    public static LinkedHashMap<PosLevelKey, Integer> farCheckMap = new LinkedHashMap();
    public static LinkedHashMap<PosLevelKey, Integer> weightMap = new LinkedHashMap();
    public static Integer breakCount = 0;
    public static Integer weightCheckCount = 0;

    @SubscribeEvent
    public void onTick(ServerTickEvent.Pre event) {
        Integer checkGen;
        Level checkLevel;
        BlockPos checkBlock;
        Map.Entry<PosLevelKey, Integer> checkVal;
        breakCount = 0;
        boolean delay = false;
        if (checkMap.isEmpty() && weightMap.isEmpty() && !farCheckMap.isEmpty()) {
            checkVal = farCheckMap.entrySet().iterator().next();
            farCheckMap.remove(checkVal.getKey());
            checkBlock = checkVal.getKey().getPos();
            checkLevel = checkVal.getKey().getLevel();
            checkGen = checkVal.getValue();
            if (checkLevel.isLoaded(checkBlock)) {
                this.checkBreak(checkBlock, checkLevel, checkGen);
            } else {
                farCheckMap.put(checkVal.getKey(), checkVal.getValue());
            }
        } else {
            while (!delay && !checkMap.isEmpty()) {
                if (breakCount >= Config.maxBreakPerTick) {
                    delay = true;
                }
                checkVal = checkMap.entrySet().iterator().next();
                checkMap.remove(checkVal.getKey());
                checkBlock = checkVal.getKey().getPos();
                checkLevel = checkVal.getKey().getLevel();
                checkGen = checkVal.getValue();
                if (checkLevel.isLoaded(checkBlock) && checkLevel.getChunkAt(checkBlock).getFullStatus().equals((Object)FullChunkStatus.ENTITY_TICKING)) {
                    this.checkBreak(checkBlock, checkLevel, checkGen);
                    continue;
                }
                farCheckMap.put(checkVal.getKey(), checkVal.getValue());
            }
        }
        weightCheckCount = 0;
        delay = false;
        while (!delay && !weightMap.isEmpty()) {
            if (weightCheckCount >= 20) {
                delay = true;
            }
            checkVal = weightMap.entrySet().iterator().next();
            weightMap.remove(checkVal.getKey());
            checkBlock = checkVal.getKey().getPos();
            checkLevel = checkVal.getKey().getLevel();
            checkGen = checkVal.getValue();
            this.checkWeight(checkBlock, checkLevel, checkGen);
            Integer n = weightCheckCount;
            weightCheckCount = weightCheckCount + 1;
        }
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Pre event) {
        if (!event.getLevel().isClientSide() && event.getLevel().getServer() != null && event.getLevel().getDayTime() % 20L == 1L) {
            for (ServerPlayer player : event.getLevel().getServer().getPlayerList().getPlayers()) {
                BlockPos blockPos = player.getOnPos();
                boolean indusBlock = Config.indestructibleBlocks.contains(event.getLevel().getBlockState(blockPos).getBlock());
                if (Config.invertIndestructibleBlocks.booleanValue()) {
                    boolean bl = indusBlock = !indusBlock;
                }
                if (!event.getLevel().dimension().equals(player.level().dimension()) || player.gameMode.getGameModeForPlayer().equals((Object)GameType.SPECTATOR) || !SupportChecker.IsSupport(event.getLevel().getBlockState(blockPos), blockPos, event.getLevel()) || indusBlock || checkMap.containsKey(blockPos) || !Config.allowedDimensions.contains(event.getLevel().dimension())) continue;
                if (event.getLevel().getRandom().nextDouble() < 0.05) {
                    weightMap.put(new PosLevelKey(blockPos, event.getLevel()), 1);
                    continue;
                }
                checkMap.put(new PosLevelKey(blockPos, event.getLevel()), 1);
            }
        }
    }

    public void checkBreak(BlockPos blockPos, Level level, Integer generation) {
        HashSet<BlockPos> supportSet = new HashSet<BlockPos>();
        HashSet<Object> newlyAddedSet = new HashSet<Object>();
        HashSet<BlockPos> freshSet = new HashSet<BlockPos>();
        newlyAddedSet.add(blockPos);
        for (int i = 1; i <= TickHandler.getSupportStrength(level.getBlockState(blockPos), blockPos, level); ++i) {
            for (BlockPos blockPos2 : newlyAddedSet) {
                for (BlockPos adjCurrentPos : BlockPos.betweenClosed((BlockPos)blockPos2.offset(-1, -1, -1), (BlockPos)blockPos2.offset(1, 1, 1))) {
                    boolean isSupport = SupportChecker.IsSupport(level.getBlockState(adjCurrentPos), adjCurrentPos, level) || Config.buoyancyEnabled != false && level.getBlockState(adjCurrentPos).getFluidState().isSource() || level.getBlockState(blockPos).is(Blocks.OBSIDIAN) && level.getBlockState(adjCurrentPos).getFluidState().isSourceOfType((Fluid)Fluids.LAVA) || level.getBlockState(blockPos).is(BlockTags.ICE) && level.getBlockState(adjCurrentPos).getFluidState().isSourceOfType((Fluid)Fluids.WATER);
                    if (!isSupport || supportSet.contains(adjCurrentPos) || adjCurrentPos.distManhattan((Vec3i)blockPos2) != 1 || adjCurrentPos.equals((Object)blockPos)) continue;
                    if (adjCurrentPos.getY() < blockPos.getY()) {
                        boolean indusBlock = Config.indestructibleBlocks.contains(level.getBlockState(adjCurrentPos).getBlock());
                        if (Config.invertIndestructibleBlocks.booleanValue()) {
                            boolean bl = indusBlock = !indusBlock;
                        }
                        if (!indusBlock && generation <= Config.supportSearchIter) {
                            checkMap.put(new PosLevelKey(adjCurrentPos.immutable(), level), generation + 1);
                        }
                        return;
                    }
                    supportSet.add(adjCurrentPos.immutable());
                    freshSet.add(adjCurrentPos.immutable());
                }
            }
            newlyAddedSet.clear();
            newlyAddedSet.addAll(freshSet);
            freshSet.clear();
        }
        TickHandler.breakBlock(blockPos, level);
    }

    public void checkWeight(BlockPos blockPos, Level level, Integer weight) {
        HashSet<BlockPos> supportSet = new HashSet<BlockPos>();
        HashSet<Object> newlyAddedSet = new HashSet<Object>();
        HashSet<BlockPos> freshSet = new HashSet<BlockPos>();
        newlyAddedSet.add(blockPos);
        for (int i = 1; i <= TickHandler.getSupportStrength(level.getBlockState(blockPos), blockPos, level); ++i) {
            for (BlockPos blockPos2 : newlyAddedSet) {
                for (BlockPos adjCurrentPos : BlockPos.betweenClosed((BlockPos)blockPos2.offset(-1, -1, -1), (BlockPos)blockPos2.offset(1, 1, 1))) {
                    if (!(SupportChecker.IsSupport(level.getBlockState(adjCurrentPos), adjCurrentPos, level) || Config.buoyancyEnabled != false && level.getBlockState(adjCurrentPos).getFluidState().isSource() || level.getBlockState(blockPos).is(Blocks.OBSIDIAN) && level.getBlockState(adjCurrentPos).getFluidState().isSourceOfType((Fluid)Fluids.LAVA)) && (!level.getBlockState(blockPos).is(BlockTags.ICE) || !level.getBlockState(adjCurrentPos).getFluidState().isSourceOfType((Fluid)Fluids.WATER)) || supportSet.contains(adjCurrentPos) || adjCurrentPos.distManhattan((Vec3i)blockPos2) != 1 || adjCurrentPos.equals((Object)blockPos)) continue;
                    int n = 1;
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        if (!SupportChecker.IsSupport(level.getBlockState(adjCurrentPos.relative(direction)), adjCurrentPos.relative(direction), level)) continue;
                        ++n;
                    }
                    boolean indusBlock = Config.indestructibleBlocks.contains(level.getBlockState(adjCurrentPos).getBlock());
                    if (Config.invertIndestructibleBlocks.booleanValue()) {
                        boolean bl = indusBlock = !indusBlock;
                    }
                    if (!indusBlock && n * 3 + 3 * TickHandler.getSupportStrength(level.getBlockState(adjCurrentPos), adjCurrentPos, level) <= weight + i) continue;
                    if (adjCurrentPos.getY() < blockPos.getY()) {
                        if (!indusBlock && weight + i <= Config.weightSearchIter && !TickHandler.atGround(adjCurrentPos, level)) {
                            weightMap.put(new PosLevelKey(adjCurrentPos.immutable(), level), weight + i);
                        }
                        return;
                    }
                    supportSet.add(adjCurrentPos.immutable());
                    freshSet.add(adjCurrentPos.immutable());
                }
            }
            newlyAddedSet.clear();
            newlyAddedSet.addAll(freshSet);
            freshSet.clear();
        }
        TickHandler.breakBlock(blockPos, level);
    }

    public static void breakBlock(BlockPos blockPos, Level level) {
        Integer n = breakCount;
        breakCount = breakCount + 1;
        if (Config.removeBlocksInsteadOfFall.booleanValue()) {
            level.destroyBlock(blockPos, Config.fallingBlockItemDropChance > level.getRandom().nextDouble());
        } else {
            FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall((Level)level, (BlockPos)blockPos, (BlockState)level.getBlockState(blockPos));
            fallingBlockEntity.setHurtsEntities((float)Config.dmgDist.intValue(), Math.min(TickHandler.getSupportStrength(fallingBlockEntity.getBlockState(), blockPos, level), Config.dmgMax));
            fallingBlockEntity.dropItem = false;
        }
    }

    public static boolean atGround(BlockPos blockPos, Level level) {
        for (BlockPos adjCurrentPos : BlockPos.betweenClosed((BlockPos)blockPos.offset(-4, 0, -4), (BlockPos)blockPos.offset(4, 0, 4))) {
            if (SupportChecker.IsSupport(level.getBlockState(adjCurrentPos), adjCurrentPos, level)) continue;
            return false;
        }
        return true;
    }

    public static int getSupportStrength(BlockState blockState, BlockPos blockPos, Level level) {
        AtomicInteger strength = new AtomicInteger(-1);
        if (!blockState.getShape((BlockGetter)level, blockPos).equals(Shapes.block())) {
            strength.set(3);
        }
        blockState.getTags().forEach(blockTagKey -> {
            if (Config.overwrittenTagMap.containsKey(blockTagKey)) {
                strength.set(Config.overwrittenTagMap.get(blockTagKey));
            }
        });
        if (Config.overwrittenBlockMap.containsKey(blockState.getBlock())) {
            strength.set(Config.overwrittenBlockMap.get(blockState.getBlock()));
        }
        if (blockState.getBlock() instanceof MossBlock) {
            boolean bl = true;
        }
        if (strength.get() == -1) {
            float hardness = blockState.getBlock().defaultDestroyTime();
            if (hardness > 7.0f || hardness < 0.0f) {
                hardness = 7.0f;
            }
            return Math.round((float)(Math.log10(hardness + 1.0f) * ((double)(Config.supportLengthMax - Config.supportLengthMin) / 0.903089987) + (double)Config.supportLengthMin.intValue()));
        }
        return strength.get();
    }
}
