package io.github.jake404notfound.simple_block_physics.mixin;

import io.github.jake404notfound.simple_block_physics.utils.BlockPlaceHandler;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "place", at = @At("RETURN"))
    public void onPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue().consumesAction() && context.getLevel() != null && context.getClickedPos() != null) {
            BlockPlaceHandler.checkSupportsFromPlace(context.getLevel(), context.getClickedPos());
        }
    }
}
