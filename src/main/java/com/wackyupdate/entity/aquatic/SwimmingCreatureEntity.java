package com.wackyupdate.entity.aquatic;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.control.YawAdjustingLookControl;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/** Base for the bigger swimmers: real 3D swimming like dolphins (swim navigation + aquatic movement). */
public abstract class SwimmingCreatureEntity extends WaterCreatureEntity {
	protected SwimmingCreatureEntity(EntityType<? extends WaterCreatureEntity> type, World world, int maxPitchChange) {
		super(type, world);
		moveControl = new AquaticMoveControl(this, maxPitchChange, 10, 0.02f, 0.1f, true);
		lookControl = new YawAdjustingLookControl(this, 10);
	}

	@Override
	protected EntityNavigation createNavigation(World world) {
		return new SwimNavigation(this, world);
	}

	/** Predicate for "this player is in the water and fair game". */
	public static boolean isSwimmingPlayer(net.minecraft.entity.LivingEntity entity) {
		return entity instanceof PlayerEntity player && player.isTouchingWater() && !player.isCreative() && !player.isSpectator();
	}
}
