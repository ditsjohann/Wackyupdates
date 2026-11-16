package com.wackyupdate.entity.aquatic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * A wide ray that glides along the sea floor. Neutral: it stings (poison II) if you hit it, and
 * also if you touch it without sneaking - shuffle carefully over sandy bottoms!
 */
public class StingrayEntity extends SwimmingCreatureEntity {
	private int touchCooldown;

	public StingrayEntity(EntityType<? extends WaterCreatureEntity> type, World world) {
		super(type, world, 15);
		experiencePoints = 3;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 14.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.9)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 12.0);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(2, new MeleeAttackGoal(this, 1.3, true));
		goalSelector.add(4, new GlideNearFloorGoal(this));
		goalSelector.add(6, new LookAroundGoal(this));
		targetSelector.add(1, new RevengeGoal(this));
	}

	@Override
	public boolean tryAttack(Entity target) {
		boolean hit = super.tryAttack(target);
		if (hit && target instanceof LivingEntity living) {
			living.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1), this);
		}
		return hit;
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (touchCooldown > 0) {
			touchCooldown--;
			return;
		}
		for (PlayerEntity player : getWorld().getEntitiesByClass(PlayerEntity.class, getBoundingBox().expand(0.05),
				p -> !p.isSneaking() && !p.isSpectator() && !p.isCreative())) {
			tryAttack(player);
			playSound(SoundEvents.ENTITY_PUFFER_FISH_STING, 1.0f, 1.2f);
			touchCooldown = 40;
		}
	}

	/** Picks a random spot nearby and settles it onto the sea floor before swimming there. */
	static class GlideNearFloorGoal extends Goal {
		private final StingrayEntity ray;

		GlideNearFloorGoal(StingrayEntity ray) {
			this.ray = ray;
			setControls(EnumSet.of(Control.MOVE));
		}

		@Override
		public boolean canStart() {
			return ray.isTouchingWater() && ray.getNavigation().isIdle() && ray.getRandom().nextInt(50) == 0;
		}

		@Override
		public void start() {
			World world = ray.getWorld();
			BlockPos.Mutable pos = ray.getBlockPos().add(ray.getRandom().nextInt(17) - 8, ray.getRandom().nextInt(5) - 2,
				ray.getRandom().nextInt(17) - 8).mutableCopy();
			if (!world.getFluidState(pos).isIn(FluidTags.WATER)) return;
			for (int i = 0; i < 16 && world.getFluidState(pos.down()).isIn(FluidTags.WATER); i++) {
				pos.move(Direction.DOWN);
			}
			ray.getNavigation().startMovingTo(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 1.0);
		}

		@Override
		public boolean shouldContinue() {
			return !ray.getNavigation().isIdle();
		}
	}
}
