package com.wackyupdate.entity;

import com.wackyupdate.registry.ModEntities;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** A slow, peaceful snail with a crystal shell that comes in any of the 16 colours. Loves moss. */
public class PrismSnailEntity extends AnimalEntity {
	private static final TrackedData<Integer> SHELL_COLOR = DataTracker.registerData(PrismSnailEntity.class, TrackedDataHandlerRegistry.INTEGER);

	public PrismSnailEntity(EntityType<? extends AnimalEntity> type, World world) {
		super(type, world);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return AnimalEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.12)
			.add(EntityAttributes.GENERIC_ARMOR, 6.0);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(SHELL_COLOR, 0);
	}

	public DyeColor getShellColor() {
		return DyeColor.byId(dataTracker.get(SHELL_COLOR));
	}

	public void setShellColor(DyeColor color) {
		dataTracker.set(SHELL_COLOR, color.getId());
	}

	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
		setShellColor(DyeColor.byId(random.nextInt(16)));
		return super.initialize(world, difficulty, spawnReason, entityData);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(0, new SwimGoal(this));
		goalSelector.add(1, new EscapeDangerGoal(this, 1.5));
		goalSelector.add(2, new AnimalMateGoal(this, 1.0));
		goalSelector.add(3, new TemptGoal(this, 1.2, this::isBreedingItem, false));
		goalSelector.add(4, new FollowParentGoal(this, 1.1));
		goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
		goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
		goalSelector.add(7, new LookAroundGoal(this));
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return stack.isOf(Items.MOSS_BLOCK) || stack.isOf(Items.MOSS_CARPET);
	}

	@Nullable
	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity other) {
		PrismSnailEntity baby = ModEntities.PRISM_SNAIL.create(world);
		if (baby != null) {
			// Shell colour of the baby is a mix: one of the parents' or a brand new one.
			DyeColor color = switch (random.nextInt(3)) {
				case 0 -> getShellColor();
				case 1 -> other instanceof PrismSnailEntity snail ? snail.getShellColor() : getShellColor();
				default -> DyeColor.byId(random.nextInt(16));
			};
			baby.setShellColor(color);
		}
		return baby;
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("ShellColor", dataTracker.get(SHELL_COLOR));
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		dataTracker.set(SHELL_COLOR, nbt.getInt("ShellColor"));
	}
}
