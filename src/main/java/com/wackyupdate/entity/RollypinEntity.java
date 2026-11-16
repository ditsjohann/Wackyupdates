package com.wackyupdate.entity;

import com.wackyupdate.registry.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
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
import net.minecraft.server.world.ServerWorld;
import com.wackyupdate.WackyUpdate;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A scaly little pangolin-ish critter. When hurt it curls into an armoured ball and rolls away
 * very fast, taking half damage while rolled up.
 */
public class RollypinEntity extends AnimalEntity {
	private static final TrackedData<Integer> ROLL_TICKS = DataTracker.registerData(RollypinEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final net.minecraft.util.Identifier ROLL_SPEED = WackyUpdate.id("rollypin_roll");

	public RollypinEntity(EntityType<? extends AnimalEntity> type, World world) {
		super(type, world);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return AnimalEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 12.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.22);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(ROLL_TICKS, 0);
	}

	public boolean isRolled() {
		return dataTracker.get(ROLL_TICKS) > 0;
	}

	@Override
	protected void initGoals() {
		goalSelector.add(0, new SwimGoal(this));
		goalSelector.add(1, new EscapeDangerGoal(this, 1.8));
		goalSelector.add(2, new AnimalMateGoal(this, 1.0));
		goalSelector.add(3, new TemptGoal(this, 1.2, this::isBreedingItem, false));
		goalSelector.add(4, new FollowParentGoal(this, 1.1));
		goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
		goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
		goalSelector.add(7, new LookAroundGoal(this));
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (isRolled()) amount *= 0.5f;
		boolean hurt = super.damage(source, amount);
		if (hurt && !getWorld().isClient && isAlive()) {
			if (!isRolled()) playSound(SoundEvents.ENTITY_ARMADILLO_ROLL, 1.0f, 1.2f);
			dataTracker.set(ROLL_TICKS, 120);
		}
		return hurt;
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		int roll = dataTracker.get(ROLL_TICKS);
		EntityAttributeInstance speed = getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
		if (roll > 0) {
			dataTracker.set(ROLL_TICKS, roll - 1);
			if (speed != null && !speed.hasModifier(ROLL_SPEED)) {
				speed.addTemporaryModifier(new EntityAttributeModifier(ROLL_SPEED, 1.2, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
			}
		} else if (speed != null && speed.hasModifier(ROLL_SPEED)) {
			speed.removeModifier(ROLL_SPEED);
			playSound(SoundEvents.ENTITY_ARMADILLO_UNROLL_FINISH, 1.0f, 1.2f);
		}
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return stack.isOf(Items.SPIDER_EYE);
	}

	@Nullable
	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity other) {
		return ModEntities.ROLLYPIN.create(world);
	}
}
