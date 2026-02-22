package io.github.jake404notfound.simple_block_physics.utils;

import io.github.jake404notfound.simple_block_physics.Config;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;

@SuppressWarnings("resource")
public class TickHandler {
    public static LinkedHashMap<PosLevelKey, Integer> checkMap = new LinkedHashMap<>();
    public static LinkedHashMap<PosLevelKey, Integer> farCheckMap = new LinkedHashMap<>();
    public static LinkedHashMap<PosLevelKey, Integer> weightMap = new LinkedHashMap<>();
    public static Integer breakCount = 0;
    public static Integer weightCheckCount = 0;

    public static void onServerTick() {
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
                checkBreak(checkBlock, checkLevel, checkGen);
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

                if (checkLevel.isLoaded(checkBlock)) {
                    checkBreak(checkBlock, checkLevel, checkGen);
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
            checkWeight(checkBlock, checkLevel, checkGen);
            weightCheckCount = weightCheckCount + 1;
        }
    }

    public static void onLevelTick(Level level) {
        if (!level.isClientSide() && level.getServer() != null && level.getDayTime() % 20L == 1L) {
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                BlockPos blockPos = player.blockPosition();
                boolean indusBlock = Config.indestructibleBlocks.contains(level.getBlockState(blockPos).getBlock());
                if (Config.invertIndestructibleBlocks) {
                    indusBlock = !indusBlock;
                }
                if (!level.dimension().equals(player.level().dimension())
                        || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR
                        || !SupportChecker.IsSupport(level.getBlockState(blockPos), blockPos, level) || indusBlock
                        || checkMap.containsKey(new PosLevelKey(blockPos, level))
                        || !Config.allowedDimensions.contains(level.dimension())) {
                    continue;
                }
                if (level.getRandom().nextDouble() < 0.05) {
                    weightMap.put(new PosLevelKey(blockPos, level), 1);
                    continue;
                }
                checkMap.put(new PosLevelKey(blockPos, level), 1);
            }
        }
    }

    public static void checkBreak(BlockPos blockPos, Level level, Integer generation) {
        HashSet<BlockPos> supportSet = new HashSet<>();
        HashSet<BlockPos> newlyAddedSet = new HashSet<>();
        HashSet<BlockPos> freshSet = new HashSet<>();
        newlyAddedSet.add(blockPos);

        for (int i = 1; i <= getSupportStrength(level.getBlockState(blockPos), blockPos, level); ++i) {
            for (BlockPos blockPos2 : newlyAddedSet) {
                for (BlockPos adjCurrentPos : BlockPos.betweenClosed(blockPos2.offset(-1, -1, -1),
                        blockPos2.offset(1, 1, 1))) {
                    boolean isSupport = SupportChecker.IsSupport(level.getBlockState(adjCurrentPos), adjCurrentPos,
                            level)
                            || (Config.buoyancyEnabled && level.getBlockState(adjCurrentPos).getFluidState().isSource())
                            || (level.getBlockState(blockPos).is(Blocks.OBSIDIAN)
                                    && level.getBlockState(adjCurrentPos).getFluidState().isSourceOfType(Fluids.LAVA))
                            || (level.getBlockState(blockPos).is(BlockTags.ICE)
                                    && level.getBlockState(adjCurrentPos).getFluidState().isSourceOfType(Fluids.WATER));

                    if (!isSupport || supportSet.contains(adjCurrentPos) || adjCurrentPos.distManhattan(blockPos2) != 1
                            || adjCurrentPos.equals(blockPos))
                        continue;

                    if (adjCurrentPos.getY() < blockPos.getY()) {
                        boolean indusBlock = Config.indestructibleBlocks
                                .contains(level.getBlockState(adjCurrentPos).getBlock());
                        if (Config.invertIndestructibleBlocks) {
                            indusBlock = !indusBlock;
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
        breakBlock(blockPos, level);
    }

    public static void checkWeight(BlockPos blockPos, Level level, Integer weight) {
        HashSet<BlockPos> supportSet = new HashSet<>();
        HashSet<BlockPos> newlyAddedSet = new HashSet<>();
        HashSet<BlockPos> freshSet = new HashSet<>();
        newlyAddedSet.add(blockPos);

        for (int i = 1; i <= getSupportStrength(level.getBlockState(blockPos), blockPos, level); ++i) {
            for (BlockPos blockPos2 : newlyAddedSet) {
                for (BlockPos adjCurrentPos : BlockPos.betweenClosed(blockPos2.offset(-1, -1, -1),
                        blockPos2.offset(1, 1, 1))) {
                    if (!(SupportChecker.IsSupport(level.getBlockState(adjCurrentPos), adjCurrentPos, level)
                            || (Config.buoyancyEnabled && level.getBlockState(adjCurrentPos).getFluidState().isSource())
                            || (level.getBlockState(blockPos).is(Blocks.OBSIDIAN)
                                    && level.getBlockState(adjCurrentPos).getFluidState().isSourceOfType(Fluids.LAVA)))
                            && (!level.getBlockState(blockPos).is(BlockTags.ICE)
                                    || !level.getBlockState(adjCurrentPos).getFluidState().isSourceOfType(Fluids.WATER))
                            || supportSet.contains(adjCurrentPos) || adjCurrentPos.distManhattan(blockPos2) != 1
                            || adjCurrentPos.equals(blockPos))
                        continue;

                    int n = 1;
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        if (!SupportChecker.IsSupport(level.getBlockState(adjCurrentPos.relative(direction)),
                                adjCurrentPos.relative(direction), level))
                            continue;
                        ++n;
                    }

                    boolean indusBlock = Config.indestructibleBlocks
                            .contains(level.getBlockState(adjCurrentPos).getBlock());
                    if (Config.invertIndestructibleBlocks) {
                        indusBlock = !indusBlock;
                    }

                    if (!indusBlock && n * 3
                            + 3 * getSupportStrength(level.getBlockState(adjCurrentPos), adjCurrentPos, level) <= weight
                                    + i)
                        continue;

                    if (adjCurrentPos.getY() < blockPos.getY()) {
                        if (!indusBlock && weight + i <= Config.weightSearchIter && !atGround(adjCurrentPos, level)) {
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
        breakBlock(blockPos, level);
    }

    public static void breakBlock(BlockPos blockPos, Level level) {
        breakCount = breakCount + 1;
        if (Config.removeBlocksInsteadOfFall) {
            level.destroyBlock(blockPos, Config.fallingBlockItemDropChance > level.getRandom().nextDouble());
        } else {
            CompoundTag tag = null;
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be != null) {
                tag = be.saveWithFullMetadata(level.registryAccess());
                if (be instanceof Container container) {
                    container.clearContent();
                }
                level.removeBlockEntity(blockPos);
            }
            FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall(level, blockPos,
                    level.getBlockState(blockPos));
            if (tag != null) {
                fallingBlockEntity.blockData = tag;
            }
            fallingBlockEntity.setHurtsEntities((float) Config.dmgDist,
                    Math.min(getSupportStrength(fallingBlockEntity.getBlockState(), blockPos, level), Config.dmgMax));
            fallingBlockEntity.dropItem = true;
        }
    }

    public static boolean atGround(BlockPos blockPos, Level level) {
        for (BlockPos adjCurrentPos : BlockPos.betweenClosed(blockPos.offset(-4, 0, -4), blockPos.offset(4, 0, 4))) {
            if (SupportChecker.IsSupport(level.getBlockState(adjCurrentPos), adjCurrentPos, level))
                continue;
            return false;
        }
        return true;
    }

    public static int getSupportStrength(BlockState blockState, BlockPos blockPos, Level level) {
        AtomicInteger strength = new AtomicInteger(-1);
        if (!blockState.getShape(level, blockPos).equals(Shapes.block())) {
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

        if (strength.get() == -1) {
            float hardness = blockState.getBlock().defaultDestroyTime();
            if (hardness > 7.0f || hardness < 0.0f) {
                hardness = 7.0f;
            }
            return Math.round((float) (Math.log10(hardness + 1.0f)
                    * ((double) (Config.supportLengthMax - Config.supportLengthMin) / 0.903089987)
                    + (double) Config.supportLengthMin));
        }
        return strength.get();
    }
}
