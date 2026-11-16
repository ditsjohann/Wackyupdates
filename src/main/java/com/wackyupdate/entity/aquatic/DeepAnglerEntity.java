package com.wackyupdate.entity.aquatic;

import com.wackyupdate.registry.ModParticles;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Lurks motionless in the dark deep ocean behind a glowing lure. Fish don't recognise it as a
 * predator: they swim right up to the light and get eaten. Anything that comes close gets a
 * sudden, very hard lunge-bite.
 */
public class DeepAnglerEntity extends SwimmingCreatureEntity {
	private int lureTimer;
	private int lungeCooldown;

	public DeepAnglerEntity(EntityType<? extends WaterCreatureEntity> type, World world) {
		super(type, world, 20);
		experiencePoints = 10;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 26.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.7)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0)
			.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 12.0);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(1, new MeleeAttackGoal(this, 1.6, false));
		goalSelector.add(5, new SwimAroundGoal(this, 0.5, 240));
		targetSelector.add(1, new RevengeGoal(this));
		targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, SwimmingCreatureEntity::isSwimmingPlayer));
		targetSelector.add(3, new ActiveTargetGoal<>(this, FishEntity.class, 10, true, false, null));
	}

	/** Where the lure bulb hangs, in world space. */
	public Vec3d getLurePos() {
		float yaw = getBodyYaw() * MathHelper.RADIANS_PER_DEGREE;
		return getPos().add(-MathHelper.sin(yaw) * 0.9, 1.25, MathHelper.cos(yaw) * 0.9);
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		World world = getWorld();
		if (world.isClient) {
			if (random.nextInt(4) == 0) {
				Vec3d lure = getLurePos();
				world.addParticle(ModParticles.PLANKTON, lure.x, lure.y, lure.z, 0, 0.01, 0);
			}
			return;
		}
		// the lure: nearby fish swim towards the light
		if (++lureTimer >= 30) {
			lureTimer = 0;
			Vec3d lure = getLurePos();
			for (FishEntity fish : world.getEntitiesByClass(FishEntity.class, getBoundingBox().expand(14), f -> !(f instanceof PiranhaEntity))) {
				fish.getNavigation().startMovingTo(lure.x, lure.y, lure.z, 1.0);
			}
		}
		if (lungeCooldown > 0) lungeCooldown--;
		LivingEntity target = getTarget();
		if (target != null && lungeCooldown == 0 && isTouchingWater() && distanceTo(target) < 6 && canSee(target)) {
			Vec3d dir = target.getPos().subtract(getPos()).normalize();
			setVelocity(dir.multiply(1.1));
			velocityDirty = true;
			playSound(SoundEvents.ENTITY_EVOKER_FANGS_ATTACK, 1.0f, 0.6f);
			lungeCooldown = 80;
		}
	}

	@Override
	public boolean onKilledOther(ServerWorld world, LivingEntity other) {
		heal(6.0f);
		return super.onKilledOther(world, other);
	}

	@Override
	protected boolean isDisallowedInPeaceful() {
		return true;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_GUARDIAN_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_GUARDIAN_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_GUARDIAN_DEATH;
	}

	@Override
	public float getSoundPitch() {
		return 0.6f;
	}
}
