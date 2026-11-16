package com.wackyupdate.entity.aquatic;

import com.wackyupdate.registry.ModAquaticItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.SchoolingFishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** Small glowing fish that travel in big shimmering schools. Comes in 4 colours (kept in buckets). */
public class GlimmerfinEntity extends SchoolingFishEntity {
	public static final int VARIANTS = 4;
	private static final TrackedData<Integer> VARIANT = DataTracker.registerData(GlimmerfinEntity.class, TrackedDataHandlerRegistry.INTEGER);

	public GlimmerfinEntity(EntityType<? extends SchoolingFishEntity> type, World world) {
		super(type, world);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(VARIANT, 0);
	}

	public int getVariant() {
		return dataTracker.get(VARIANT);
	}

	public void setVariant(int variant) {
		dataTracker.set(VARIANT, Math.floorMod(variant, VARIANTS));
	}

	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
		setVariant(random.nextInt(VARIANTS));
		return super.initialize(world, difficulty, spawnReason, entityData);
	}

	@Override
	public int getMaxGroupSize() {
		return 10;
	}

	@Override
	public ItemStack getBucketItem() {
		return new ItemStack(ModAquaticItems.GLIMMERFIN_BUCKET);
	}

	@Override
	public void copyDataToStack(ItemStack stack) {
		super.copyDataToStack(stack);
		NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, nbt -> nbt.putInt("Variant", getVariant()));
	}

	@Override
	public void copyDataFromNbt(NbtCompound nbt) {
		super.copyDataFromNbt(nbt);
		if (nbt.contains("Variant")) setVariant(nbt.getInt("Variant"));
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("Variant", getVariant());
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		setVariant(nbt.getInt("Variant"));
	}

	@Override
	protected SoundEvent getFlopSound() {
		return SoundEvents.ENTITY_TROPICAL_FISH_FLOP;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_TROPICAL_FISH_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_TROPICAL_FISH_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_TROPICAL_FISH_DEATH;
	}
}
