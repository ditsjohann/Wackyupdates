package com.wackyupdate.entity.aquatic;

import com.wackyupdate.registry.ModAquaticItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.SchoolingFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Swarming river fish. A school attacks swimmers on sight and goes after any wounded creature
 * (below half health) that wanders into the water. Lives in jungle, swamp and mangrove waters.
 */
public class PiranhaEntity extends SchoolingFishEntity {
	public PiranhaEntity(EntityType<? extends SchoolingFishEntity> type, World world) {
		super(type, world);
		experiencePoints = 3;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return FishEntity.createFishAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(1, new MeleeAttackGoal(this, 1.7, true));
		goalSelector.add(4, new SwimAroundGoal(this, 1.0, 20));
		goalSelector.add(5, new FollowGroupLeaderGoal(this));
		targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
		targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, SwimmingCreatureEntity::isSwimmingPlayer));
		targetSelector.add(3, new ActiveTargetGoal<>(this, LivingEntity.class, 10, true, false, PiranhaEntity::isWoundedSwimmer));
	}

	private static boolean isWoundedSwimmer(LivingEntity entity) {
		return entity.isTouchingWater() && !(entity instanceof PiranhaEntity) && !(entity instanceof PlayerEntity)
			&& entity.getHealth() < entity.getMaxHealth() * 0.5f;
	}

	@Override
	public int getMaxGroupSize() {
		return 6;
	}

	@Override
	protected boolean isDisallowedInPeaceful() {
		return true;
	}

	@Override
	public ItemStack getBucketItem() {
		return new ItemStack(ModAquaticItems.PIRANHA_BUCKET);
	}

	@Override
	protected SoundEvent getFlopSound() {
		return SoundEvents.ENTITY_COD_FLOP;
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
