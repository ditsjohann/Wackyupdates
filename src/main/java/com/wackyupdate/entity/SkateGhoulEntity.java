package com.wackyupdate.entity;

import com.wackyupdate.registry.ModItems;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * An undead skater. Shuffles like a zombie, then suddenly kicks off on its rollerskates and dashes
 * at you. Burns in daylight. Very rarely drops its skates.
 */
public class SkateGhoulEntity extends HostileEntity {
	private int dashCooldown = 60;

	public SkateGhoulEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
		experiencePoints = 7;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.5)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(1, new SwimGoal(this));
		goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
		goalSelector.add(5, new WanderAroundFarGoal(this, 0.9));
		goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		goalSelector.add(7, new LookAroundGoal(this));
		targetSelector.add(1, new RevengeGoal(this));
		targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
		targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, false));
	}

	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
		EntityData data = super.initialize(world, difficulty, spawnReason, entityData);
		equipStack(EquipmentSlot.FEET, new ItemStack(ModItems.ROLLERSKATES));
		setEquipmentDropChance(EquipmentSlot.FEET, 0.06f);
		return data;
	}

	@Override
	public void tickMovement() {
		if (isAlive() && isAffectedByDaylight()) {
			setOnFireFor(8.0f);
		}
		super.tickMovement();
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (dashCooldown > 0) {
			dashCooldown--;
			return;
		}
		LivingEntity target = getTarget();
		if (target == null || !isOnGround()) return;
		double dist = distanceTo(target);
		if (dist > 3.5 && dist < 14 && canSee(target)) {
			Vec3d dir = target.getPos().subtract(getPos()).multiply(1, 0, 1).normalize();
			setVelocity(dir.x * 1.1, 0.2, dir.z * 1.1);
			velocityModified = true;
			playSound(SoundEvents.ENTITY_BREEZE_JUMP, 1.0f, 0.7f);
			if (getWorld() instanceof ServerWorld server) {
				server.spawnParticles(ParticleTypes.CLOUD, getX(), getY() + 0.1, getZ(), 8, 0.2, 0.05, 0.2, 0.02);
			}
			dashCooldown = 60 + random.nextInt(50);
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_ZOMBIE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_ZOMBIE_DEATH;
	}

	@Override
	public float getSoundPitch() {
		return 1.25f;
	}
}
