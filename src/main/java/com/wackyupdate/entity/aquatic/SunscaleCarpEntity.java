package com.wackyupdate.entity.aquatic;

import com.wackyupdate.registry.ModAquaticItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/** A chunky golden river fish. Good eating once cooked. */
public class SunscaleCarpEntity extends FishEntity {
	public SunscaleCarpEntity(EntityType<? extends FishEntity> type, World world) {
		super(type, world);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return FishEntity.createFishAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0);
	}

	@Override
	public ItemStack getBucketItem() {
		return new ItemStack(ModAquaticItems.SUNSCALE_CARP_BUCKET);
	}

	@Override
	protected SoundEvent getFlopSound() {
		return SoundEvents.ENTITY_SALMON_FLOP;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_SALMON_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_SALMON_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_SALMON_DEATH;
	}
}
