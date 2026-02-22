package io.github.jake404notfound.simple_block_physics.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class SupportChecker {
    static FallingBlockEntity dummyFallingBlockEntity;

    public static boolean IsSupport(BlockState blockState, BlockPos blockPos, Level level) {
        if (dummyFallingBlockEntity == null) {
            dummyFallingBlockEntity = new FallingBlockEntity(EntityType.FALLING_BLOCK, level);
        }
        return !blockState.getCollisionShape(level, blockPos, CollisionContext.of(dummyFallingBlockEntity)).isEmpty();
    }
}
