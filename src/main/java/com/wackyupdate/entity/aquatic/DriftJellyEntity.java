package com.wackyupdate.entity.aquatic;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * A glowing jellyfish that just drifts and pulses. It never attacks, but touching its tentacles
 * stings (poison + slowness). Sea creatures are immune.
 */
public class DriftJellyEntity extends WaterCreatureEntity {
	private int pulseTimer;
	private float driftYaw;
	private int stingCooldown;

	public DriftJellyEntity(EntityType<? extends WaterCreatureEntity> type, World world) {
		super(type, world);
		driftYaw = random.nextFloat() * MathHelper.TAU;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0);
	}

	@Override
	protected void initGoals() {
		// jellies have no brain - they drift
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		if (getWorld().isClient) return;
		if (isTouchingWater()) {
			if (--pulseTimer <= 0) {
				pulseTimer = 30 + random.nextInt(30);
				driftYaw += (random.nextFloat() - 0.5f) * 1.2f;
				boolean nearSurface = getWorld().getFluidState(getBlockPos().up(2)).isEmpty();
				double up = nearSurface ? 0.02 : 0.09;
				setVelocity(getVelocity().add(MathHelper.cos(driftYaw) * 0.05, up, MathHelper.sin(driftYaw) * 0.05));
				velocityDirty = true;
			}
			// sink gently between pulses
			setVelocity(getVelocity().multiply(0.96).add(0, -0.002, 0));
		}
		if (stingCooldown > 0) stingCooldown--;
		if (stingCooldown == 0 && isAlive()) {
			for (LivingEntity entity : getWorld().getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(0.1),
					e -> e != this && e.isAlive() && !e.isSpectator() && !(e instanceof WaterCreatureEntity))) {
				if (entity.damage(getDamageSources().mobAttack(this), 2.0f)) {
					entity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 0), this);
					entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 0), this);
					stingCooldown = 20;
				}
			}
		}
	}

	@Override
	public boolean isPushedByFluids() {
		return true;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.BLOCK_SLIME_BLOCK_HIT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.BLOCK_SLIME_BLOCK_BREAK;
	}

	@Override
	protected void playSwimSound(float volume) {
	}
}
