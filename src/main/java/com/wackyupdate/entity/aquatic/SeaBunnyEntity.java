package com.wackyupdate.entity.aquatic;

import com.wackyupdate.registry.ModAquaticItems;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/** A fluffy sea slug that crawls slowly across the sea floor. Fits in a bucket. */
public class SeaBunnyEntity extends WaterCreatureEntity implements Bucketable {
	private static final TrackedData<Boolean> FROM_BUCKET = DataTracker.registerData(SeaBunnyEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	public SeaBunnyEntity(EntityType<? extends WaterCreatureEntity> type, World world) {
		super(type, world);
		setPathfindingPenalty(PathNodeType.WATER, 0.0f);
		setPathfindingPenalty(PathNodeType.WATER_BORDER, 0.0f);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 4.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.08);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(FROM_BUCKET, false);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(4, new WanderAroundGoal(this, 1.0, 40));
		goalSelector.add(6, new LookAroundGoal(this));
	}

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		return Bucketable.tryBucket(player, hand, this).orElse(super.interactMob(player, hand));
	}

	@Override
	public boolean cannotDespawn() {
		return super.cannotDespawn() || isFromBucket();
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return !isFromBucket() && !hasCustomName();
	}

	@Override
	public boolean isFromBucket() {
		return dataTracker.get(FROM_BUCKET);
	}

	@Override
	public void setFromBucket(boolean fromBucket) {
		dataTracker.set(FROM_BUCKET, fromBucket);
	}

	@Override
	public void copyDataToStack(ItemStack stack) {
		Bucketable.copyDataToStack(this, stack);
	}

	@Override
	public void copyDataFromNbt(NbtCompound nbt) {
		Bucketable.copyDataFromNbt(this, nbt);
	}

	@Override
	public ItemStack getBucketItem() {
		return new ItemStack(ModAquaticItems.SEA_BUNNY_BUCKET);
	}

	@Override
	public SoundEvent getBucketFillSound() {
		return SoundEvents.ITEM_BUCKET_FILL_AXOLOTL;
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("FromBucket", isFromBucket());
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		setFromBucket(nbt.getBoolean("FromBucket"));
	}
}
