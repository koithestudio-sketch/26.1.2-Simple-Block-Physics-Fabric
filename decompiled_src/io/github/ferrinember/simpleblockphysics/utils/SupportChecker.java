/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.item.FallingBlockEntity
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.shapes.CollisionContext
 */
package io.github.ferrinember.simpleblockphysics.utils;

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
        return !blockState.getCollisionShape((BlockGetter)level, blockPos, CollisionContext.of((Entity)dummyFallingBlockEntity)).isEmpty();
    }
}
