package com.wackyupdate.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import com.wackyupdate.registry.ModBlocks;
import net.minecraft.world.World;

/**
 * A walking chunk of speculum crystal that lurks deep underground. Arrows mostly glance off
 * its facets (projectiles deal half damage) - bring a pickaxe or a sword.
 */
public class ShardlingEntity extends HostileEntity {
	public ShardlingEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
		experiencePoints = 8;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 22.0)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
			.add(EntityAttributes.GENERIC_ARMOR, 6.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.24)
			.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.4)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(1, new SwimGoal(this));
		goalSelector.add(2, new MeleeAttackGoal(this, 1.1, false));
		goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
		goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		goalSelector.add(7, new LookAroundGoal(this));
		targetSelector.add(1, new RevengeGoal(this));
		targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
		targetSelector.add(3, new ActiveTargetGoal<>(this, GolemEntity.class, true));
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
			amount *= 0.5f;
		}
		boolean hurt = super.damage(source, amount);
		if (hurt && getWorld() instanceof ServerWorld server) {
			server.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, ModBlocks.SPECULUM_CLUSTER.getDefaultState()),
				getX(), getBodyY(0.6), getZ(), 12, 0.3, 0.4, 0.3, 0.1);
		}
		return hurt;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.BLOCK_AMETHYST_CLUSTER_HIT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK;
	}

	@Override
	protected void playStepSound(net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state) {
		playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_STEP, 0.4f, 0.8f + random.nextFloat() * 0.4f);
	}
}
