/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.neoforge.event.level.BlockEvent$EntityPlaceEvent
 */
package io.github.ferrinember.simpleblockphysics.utils;

import io.github.ferrinember.simpleblockphysics.Config;
import io.github.ferrinember.simpleblockphysics.utils.PosLevelKey;
import io.github.ferrinember.simpleblockphysics.utils.SupportChecker;
import io.github.ferrinember.simpleblockphysics.utils.TickHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class BlockPlaceHandler {
    @SubscribeEvent
    public void checkSupportsFromPlace(BlockEvent.EntityPlaceEvent event) {
        Level world = (Level)event.getLevel();
        boolean indusBlock = Config.indestructibleBlocks.contains(world.getBlockState(event.getPos()).getBlock());
        if (Config.invertIndestructibleBlocks.booleanValue()) {
            boolean bl = indusBlock = !indusBlock;
        }
        if (!world.isClientSide() && SupportChecker.IsSupport(world.getBlockState(event.getPos()), event.getPos(), (Level)event.getLevel()) && !TickHandler.weightMap.containsKey(new PosLevelKey(event.getPos(), world)) && !indusBlock && Config.allowedDimensions.contains(world.dimension())) {
            BlockPos blockPos = event.getPos();
            TickHandler.weightMap.put(new PosLevelKey(blockPos.immutable(), world), 1);
        }
    }
}
