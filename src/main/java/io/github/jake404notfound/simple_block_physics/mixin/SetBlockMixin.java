package io.github.jake404notfound.simple_block_physics.mixin;

import io.github.jake404notfound.simple_block_physics.Config;
import io.github.jake404notfound.simple_block_physics.utils.PosLevelKey;
import io.github.jake404notfound.simple_block_physics.utils.SupportChecker;
import io.github.jake404notfound.simple_block_physics.utils.TickHandler;
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

@Mixin(Level.class)
public abstract class SetBlockMixin {
    @Shadow
    @Final
    public boolean isClientSide;

    @Shadow
    public abstract BlockState getBlockState(BlockPos pos);

    @Shadow
    public abstract ResourceKey<Level> dimension();

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"))
    public void addToCheckMap(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft,
            CallbackInfoReturnable<Boolean> cir) {
        if (!this.isClientSide && (pFlags & 2) != 0) {
            BlockState oldState = this.getBlockState(pPos);
            if (SupportChecker.IsSupport(oldState, pPos, (Level) (Object) this)
                    && !SupportChecker.IsSupport(pState, pPos, (Level) (Object) this)
                    && Config.allowedDimensions.contains(this.dimension())) {
                for (BlockPos adjPos : BlockPos.betweenClosed(pPos.offset(-1, -1, -1), pPos.offset(1, 1, 1))) {
                    boolean indusBlock = Config.indestructibleBlocks.contains(this.getBlockState(adjPos).getBlock());
                    if (Config.invertIndestructibleBlocks) {
                        indusBlock = !indusBlock;
                    }
                    if (adjPos.equals(pPos)
                            || !SupportChecker.IsSupport(this.getBlockState(adjPos), adjPos, (Level) (Object) this)
                            || TickHandler.checkMap.containsKey(new PosLevelKey(adjPos, (Level) (Object) this))
                            || this.getBlockState(adjPos).is(Blocks.PISTON_HEAD)
                            || this.getBlockState(adjPos).is(Blocks.MOVING_PISTON) || indusBlock)
                        continue;
                    TickHandler.checkMap.put(new PosLevelKey(adjPos.immutable(), (Level) (Object) this), 1);
                }
            }
        }
    }
}
