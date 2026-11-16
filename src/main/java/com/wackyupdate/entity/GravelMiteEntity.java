package com.wackyupdate.entity;

import com.wackyupdate.registry.ModBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * A skittish little gravel bug. It runs from players and, given a moment, burrows back into
 * any gravel it touches, turning it into mite gravel. Drops its tiny feet.
 */
public class GravelMiteEntity extends PathAwareEntity {
	public GravelMiteEntity(EntityType<? extends PathAwareEntity> type, World world) {
		super(type, world);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 4.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(0, new SwimGoal(this));
		goalSelector.add(1, new EscapeDangerGoal(this, 1.6));
		goalSelector.add(2, new FleeEntityGoal<>(this, PlayerEntity.class, 6.0f, 1.2, 1.6));
		goalSelector.add(4, new WanderAroundGoal(this, 1.0));
		goalSelector.add(5, new LookAroundGoal(this));
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		if (!getWorld().isClient && isAlive() && age > 60 && random.nextInt(40) == 0) {
			BlockPos below = getBlockPos().down();
			for (Direction dir : new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
				BlockPos p = dir == Direction.DOWN ? below : getBlockPos().offset(dir);
				if (getWorld().getBlockState(p).isOf(Blocks.GRAVEL)) {
					getWorld().setBlockState(p, ModBlocks.MITE_GRAVEL.getDefaultState());
					playSound(SoundEvents.BLOCK_GRAVEL_PLACE, 0.6f, 1.6f);
					playSpawnEffects();
					discard();
					return;
				}
			}
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_SILVERFISH_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_SILVERFISH_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_SILVERFISH_DEATH;
	}

	@Override
	public float getSoundPitch() {
		return 1.6f;
	}
}
