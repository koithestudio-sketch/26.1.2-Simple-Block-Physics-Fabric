/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.BlockParticleOption
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.item.FallingBlockEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.storage.loot.LootParams$Builder
 *  net.minecraft.world.level.storage.loot.parameters.LootContextParams
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package io.github.ferrinember.simpleblockphysics.mixin;

import io.github.ferrinember.simpleblockphysics.Config;
import io.github.ferrinember.simpleblockphysics.utils.TickHandler;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={FallingBlockEntity.class})
public abstract class FallingEntityMixin
extends Entity {
    @Shadow
    private BlockState blockState;

    @Shadow
    public abstract BlockState getBlockState();

    public FallingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/item/FallingBlockEntity;onGround()Z")}, cancellable=true)
    public void addTickCheck(CallbackInfo ci) {
        if (this.onGround()) {
            this.level().playSound(null, this.blockPosition(), this.getBlockState().getSoundType().getBreakSound(), SoundSource.BLOCKS, Config.blockBreakVolume.floatValue(), 1.0f);
            if ((double)TickHandler.getSupportStrength(this.getBlockState(), this.blockPosition(), this.level()) * 0.1 - 1.0 + 2.0 * Config.fallingBlockBreakFactor >= this.level().getRandom().nextDouble()) {
                this.level().getServer().getLevel(this.level().dimension()).sendParticles((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, this.getBlockState()), this.getX(), this.getY(), this.getZ(), 50, 0.0, 0.05, 0.0, 0.1);
                this.causeFallDamage(this.fallDistance, Config.dmgMax.intValue(), this.damageSources().fallingBlock((Entity)this));
                if (Config.fallingBlockItemDropChance > this.level().getRandom().nextDouble()) {
                    this.getBlockState().getDrops(new LootParams.Builder((ServerLevel)this.level()).withParameter(LootContextParams.BLOCK_STATE, (Object)this.getBlockState()).withParameter(LootContextParams.ORIGIN, (Object)this.position()).withParameter(LootContextParams.THIS_ENTITY, (Object)this).withParameter(LootContextParams.TOOL, (Object)new ItemStack((ItemLike)Items.IRON_SHOVEL))).forEach(arg_0 -> ((FallingEntityMixin)this).spawnAtLocation(arg_0));
                }
                this.discard();
                ci.cancel();
            } else {
                ArrayList<Direction> validShiftDirList = new ArrayList<Direction>();
                for (int i = 1; i <= 4; ++i) {
                    BlockPos sidePos = this.blockPosition().atY(this.getBlockY()).relative(Direction.from2DDataValue((int)i));
                    BlockPos belowSidePos = this.blockPosition().atY(this.getBlockY() - 1).relative(Direction.from2DDataValue((int)i));
                    if (!this.level().getBlockState(sidePos).getCollisionShape((BlockGetter)this.level(), sidePos, CollisionContext.of((Entity)this)).isEmpty() || !this.level().getBlockState(belowSidePos).getCollisionShape((BlockGetter)this.level(), belowSidePos, CollisionContext.of((Entity)this)).isEmpty()) continue;
                    validShiftDirList.add(Direction.from2DDataValue((int)i));
                }
                if (!validShiftDirList.isEmpty() && Config.fallingBlockShiftFactor > this.level().getRandom().nextDouble()) {
                    Direction fallDir = (Direction)validShiftDirList.get(this.level().getRandom().nextIntBetweenInclusive(0, validShiftDirList.size() - 1));
                    FallingBlockEntity newFallingBlockEntity = FallingBlockEntity.fall((Level)this.level(), (BlockPos)this.blockPosition().relative(fallDir), (BlockState)this.getBlockState());
                    newFallingBlockEntity.setHurtsEntities((float)Config.dmgDist.intValue(), Config.dmgMax.intValue());
                    newFallingBlockEntity.dropItem = false;
                    this.causeFallDamage(this.fallDistance, Config.dmgMax.intValue(), this.damageSources().fallingBlock((Entity)this));
                    this.discard();
                    ci.cancel();
                }
            }
        }
    }
}
