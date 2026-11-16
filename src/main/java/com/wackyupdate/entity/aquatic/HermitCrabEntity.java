package com.wackyupdate.entity.aquatic;

import com.wackyupdate.registry.ModAquaticEntities;
import com.wackyupdate.registry.ModAquaticItems;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
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
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Beach and shallows crab living in a borrowed shell (5 shell colours). It ducks into its shell
 * when anyone walks up - sneak to get close. Now and then it outgrows its shell and leaves it behind.
 * Breathes air and water (via the minecraft:can_breathe_under_water tag). Breed with kelp.
 */
public class HermitCrabEntity extends AnimalEntity {
	public static final int SHELLS = 5;
	private static final TrackedData<Integer> SHELL = DataTracker.registerData(HermitCrabEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Boolean> HIDING = DataTracker.registerData(HermitCrabEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private int hideTicks;
	private int shedTimer = 6000 + (int) (Math.random() * 6000);

	public HermitCrabEntity(EntityType<? extends AnimalEntity> type, World world) {
		super(type, world);
		setPathfindingPenalty(PathNodeType.WATER, 0.0f);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return AnimalEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
			.add(EntityAttributes.GENERIC_ARMOR, 4.0);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(SHELL, 0);
		builder.add(HIDING, false);
	}

	public int getShell() {
		return dataTracker.get(SHELL);
	}

	public boolean isHiding() {
		return dataTracker.get(HIDING);
	}

	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
		dataTracker.set(SHELL, random.nextInt(SHELLS));
		return super.initialize(world, difficulty, spawnReason, entityData);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(1, new EscapeDangerGoal(this, 1.6));
		goalSelector.add(2, new AnimalMateGoal(this, 1.0));
		goalSelector.add(3, new TemptGoal(this, 1.2, this::isBreedingItem, false));
		goalSelector.add(4, new FollowParentGoal(this, 1.1));
		goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
		goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
		goalSelector.add(7, new LookAroundGoal(this));
	}

	@Override
	public boolean isPushedByFluids() {
		return false;
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		PlayerEntity near = getWorld().getClosestPlayer(this, 4.0);
		if (near != null && !near.isSneaking() && !near.isSpectator() && !isBaby()) {
			if (hideTicks == 0) playSound(SoundEvents.ENTITY_ARMADILLO_ROLL, 0.6f, 1.6f);
			hideTicks = 50;
		}
		if (hideTicks > 0) {
			hideTicks--;
			getNavigation().stop();
		}
		dataTracker.set(HIDING, hideTicks > 0);

		if (!isBaby() && --shedTimer <= 0) {
			dropItem(ModAquaticItems.HERMIT_SHELL);
			playSound(SoundEvents.ENTITY_TURTLE_SHAMBLE, 1.0f, 1.4f);
			dataTracker.set(SHELL, random.nextInt(SHELLS)); // moves into a new one
			shedTimer = 6000 + random.nextInt(6000);
		}
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		return super.damage(source, isHiding() ? amount * 0.4f : amount);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return stack.isOf(Items.KELP);
	}

	@Nullable
	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity other) {
		HermitCrabEntity baby = ModAquaticEntities.HERMIT_CRAB.create(world);
		if (baby != null) baby.dataTracker.set(SHELL, random.nextInt(SHELLS));
		return baby;
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("Shell", getShell());
		nbt.putInt("ShedTimer", shedTimer);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		dataTracker.set(SHELL, Math.floorMod(nbt.getInt("Shell"), SHELLS));
		if (nbt.contains("ShedTimer")) shedTimer = nbt.getInt("ShedTimer");
	}
}
