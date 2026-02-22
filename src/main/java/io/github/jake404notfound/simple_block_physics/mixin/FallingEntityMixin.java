package io.github.jake404notfound.simple_block_physics.mixin;

import io.github.jake404notfound.simple_block_physics.Config;
import io.github.jake404notfound.simple_block_physics.utils.TickHandler;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("resource")
@Mixin(FallingBlockEntity.class)
public abstract class FallingEntityMixin extends Entity {

    public FallingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract BlockState getBlockState();

    @Shadow
    public CompoundTag blockData;

    @Shadow
    public abstract boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource);

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;onGround()Z"), cancellable = true)
    public void addTickCheck(@org.jspecify.annotations.NonNull CallbackInfo ci) {
        if (this.onGround()) {
            this.level().playSound(null, this.blockPosition(), this.getBlockState().getSoundType().getBreakSound(),
                    SoundSource.BLOCKS, Config.blockBreakVolume.floatValue(), 1.0f);

            if (TickHandler.getSupportStrength(this.getBlockState(), this.blockPosition(), this.level()) * 0.1
                    - 1.0 + 2.0 * Config.fallingBlockBreakFactor >= this.level().getRandom().nextDouble()) {

                Level currentLevel = this.level();
                if (currentLevel instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, this.getBlockState()),
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            50,
                            0.0,
                            0.05,
                            0.0,
                            0.1);

                    this.causeFallDamage(
                            this.fallDistance,
                            Config.dmgMax.floatValue(),
                            this.damageSources().fallingBlock(this));

                    if (Config.fallingBlockItemDropChance > currentLevel.getRandom().nextDouble()) {
                        serverLevel.addFreshEntity(new ItemEntity(
                                serverLevel,
                                this.getX(),
                                this.getY(),
                                this.getZ(),
                                new ItemStack(this.getBlockState().getBlock())));

                        if (this.blockData != null && this.blockData.contains("Items")) {
                            NonNullList<ItemStack> itemStacks = NonNullList.withSize(256, ItemStack.EMPTY);
                            ContainerHelper.loadAllItems(
                                    TagValueInput.create(ProblemReporter.DISCARDING, serverLevel.registryAccess(),
                                            this.blockData),
                                    itemStacks);
                            itemStacks.forEach(stack -> {
                                if (!stack.isEmpty()) {
                                    serverLevel.addFreshEntity(
                                            new ItemEntity(serverLevel, this.getX(), this.getY(), this.getZ(), stack));
                                }
                            });
                        }
                    }
                }
                this.discard();
                ci.cancel();
            } else {
                ArrayList<Direction> validShiftDirList = new ArrayList<>();
                BlockState landingOn = this.level().getBlockState(this.blockPosition());
                boolean onFullBlock = landingOn.isCollisionShapeFullBlock(this.level(), this.blockPosition());

                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos sidePos = this.blockPosition().atY(this.blockPosition().getY()).relative(dir);
                    BlockPos belowSidePos = this.blockPosition().atY(this.blockPosition().getY() - 1).relative(dir);

                    BlockState sideState = this.level().getBlockState(sidePos);
                    BlockState belowSideState = this.level().getBlockState(belowSidePos);

                    // Can shift/fall into empty space
                    boolean canFall = sideState.getCollisionShape(this.level(), sidePos, CollisionContext.of(this))
                            .isEmpty()
                            && belowSideState.getCollisionShape(this.level(), belowSidePos, CollisionContext.of(this))
                                    .isEmpty();

                    // Can slide onto a full block nearby
                    boolean canSlide = !onFullBlock
                            && sideState.getCollisionShape(this.level(), sidePos, CollisionContext.of(this)).isEmpty()
                            && belowSideState.isCollisionShapeFullBlock(this.level(), belowSidePos);

                    if (canFall || canSlide) {
                        validShiftDirList.add(dir);
                    }
                }
                if (!validShiftDirList.isEmpty()
                        && Config.fallingBlockShiftFactor > this.level().getRandom().nextDouble()) {
                    Direction fallDir = validShiftDirList
                            .get(this.level().getRandom().nextInt(validShiftDirList.size()));

                    FallingBlockEntity newFallingBlockEntity = FallingBlockEntity.fall(this.level(),
                            this.blockPosition().relative(fallDir), this.getBlockState());

                    newFallingBlockEntity.blockData = this.blockData;
                    newFallingBlockEntity.dropItem = true;

                    this.causeFallDamage(this.fallDistance, Config.dmgMax.floatValue(),
                            this.damageSources().fallingBlock(this));
                    this.discard();
                    ci.cancel();
                }
            }
        }
    }
}
