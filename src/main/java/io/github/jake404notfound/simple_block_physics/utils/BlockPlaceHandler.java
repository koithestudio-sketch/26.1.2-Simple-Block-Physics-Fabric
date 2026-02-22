package io.github.jake404notfound.simple_block_physics.utils;

import io.github.jake404notfound.simple_block_physics.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BlockPlaceHandler {
    public static void checkSupportsFromPlace(Level world, BlockPos pos) {
        boolean indusBlock = Config.indestructibleBlocks.contains(world.getBlockState(pos).getBlock());
        if (Config.invertIndestructibleBlocks) {
            indusBlock = !indusBlock;
        }
        if (!world.isClientSide() && SupportChecker.IsSupport(world.getBlockState(pos), pos, world)
                && !TickHandler.weightMap.containsKey(new PosLevelKey(pos, world)) && !indusBlock
                && Config.allowedDimensions.contains(world.dimension())) {
            TickHandler.weightMap.put(new PosLevelKey(pos.immutable(), world), 1);
        }
    }
}
