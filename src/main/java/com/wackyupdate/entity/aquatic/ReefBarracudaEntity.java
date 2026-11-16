package com.wackyupdate.entity.aquatic;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A long silver reef predator. Hunts fish (healing when it eats one), attacks swimmers, and
 * every few seconds bursts forward in a lightning-fast dash.
 */
public class ReefBarracudaEntity extends SwimmingCreatureEntity {
	private int dashCooldown = 40;

	public ReefBarracudaEntity(EntityType<? extends WaterCreatureEntity> type, World world) {
		super(type, world, 60);
		experiencePoints = 6;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1.3)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(1, new MeleeAttackGoal(this, 1.3, true));
		goalSelector.add(4, new SwimAroundGoal(this, 1.0, 10));
		goalSelector.add(6, new LookAroundGoal(this));
		targetSelector.add(1, new RevengeGoal(this));
		targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, SwimmingCreatureEntity::isSwimmingPlayer));
		targetSelector.add(3, new ActiveTargetGoal<>(this, FishEntity.class, 20, true, false, fish -> !(fish instanceof PiranhaEntity)));
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (dashCooldown > 0) {
			dashCooldown--;
			return;
		}
		LivingEntity target = getTarget();
		if (target != null && isTouchingWater() && canSee(target)) {
			double dist = distanceTo(target);
			if (dist > 3 && dist < 12) {
				Vec3d dir = target.getEyePos().subtract(getPos()).normalize();
				setVelocity(getVelocity().add(dir.multiply(0.9)));
				velocityDirty = true;
				playSound(SoundEvents.ENTITY_DOLPHIN_SWIM, 1.0f, 1.5f);
				dashCooldown = 60 + random.nextInt(40);
			}
		}
	}

	@Override
	public boolean onKilledOther(ServerWorld world, LivingEntity other) {
		if (other instanceof FishEntity) heal(4.0f);
		return super.onKilledOther(world, other);
	}

	@Override
	protected boolean isDisallowedInPeaceful() {
		return true;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_COD_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_COD_DEATH;
	}
}
