/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package io.github.ferrinember.simpleblockphysics.mixin;

import io.github.ferrinember.simpleblockphysics.Config;
import io.github.ferrinember.simpleblockphysics.utils.PosLevelKey;
import io.github.ferrinember.simpleblockphysics.utils.SupportChecker;
import io.github.ferrinember.simpleblockphysics.utils.TickHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Level.class})
public abstract class SetBlockMixin {
    @Shadow
    @Final
    public boolean isClientSide;

    @Shadow
    public abstract BlockState getBlockState(BlockPos var1);

    @Shadow
    public abstract ResourceKey<Level> dimension();

    @Inject(method={"setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I")})
    public void addToCheckMap(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (!this.isClientSide && (pFlags & 2) != 0 && SupportChecker.IsSupport(this.getBlockState(pPos), pPos, (Level)this) && !SupportChecker.IsSupport(pState, pPos, (Level)this) && Config.allowedDimensions.contains(this.dimension())) {
            for (BlockPos adjPos : BlockPos.betweenClosed((BlockPos)pPos.offset(-1, -1, -1), (BlockPos)pPos.offset(1, 1, 1))) {
                boolean indusBlock = Config.indestructibleBlocks.contains(this.getBlockState(adjPos).getBlock());
                if (Config.invertIndestructibleBlocks.booleanValue()) {
                    boolean bl = indusBlock = !indusBlock;
                }
                if (adjPos == pPos || !SupportChecker.IsSupport(this.getBlockState(adjPos), adjPos, (Level)this) || TickHandler.checkMap.containsKey(new PosLevelKey(adjPos, (Level)this)) || this.getBlockState(adjPos).is(Blocks.PISTON_HEAD) || this.getBlockState(adjPos).is(Blocks.MOVING_PISTON) || indusBlock) continue;
                TickHandler.checkMap.put(new PosLevelKey(adjPos.immutable(), (Level)this), 1);
            }
        }
    }
}
