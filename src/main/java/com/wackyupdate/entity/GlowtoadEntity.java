package com.wackyupdate.entity;

import com.wackyupdate.registry.ModEntities;
import com.wackyupdate.registry.ModParticles;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** A chubby toad with a glowing throat sac. At night fireflies gather around it. Eats glow berries. */
public class GlowtoadEntity extends AnimalEntity {
	public GlowtoadEntity(EntityType<? extends AnimalEntity> type, World world) {
		super(type, world);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return AnimalEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
			.add(EntityAttributes.GENERIC_JUMP_STRENGTH, 0.55);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(0, new SwimGoal(this));
		goalSelector.add(1, new EscapeDangerGoal(this, 1.6));
		goalSelector.add(2, new AnimalMateGoal(this, 1.0));
		goalSelector.add(3, new TemptGoal(this, 1.2, this::isBreedingItem, false));
		goalSelector.add(4, new FollowParentGoal(this, 1.1));
		goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
		goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
		goalSelector.add(7, new LookAroundGoal(this));
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		World world = getWorld();
		if (!world.isClient && isOnGround() && getNavigation().isFollowingPath() && random.nextInt(12) == 0) {
			jump(); // hop hop
		}
		if (world.isClient && world.isNight() && random.nextInt(25) == 0) {
			world.addParticle(ModParticles.FIREFLY, getX() + (random.nextDouble() - 0.5) * 3, getY() + 0.5 + random.nextDouble() * 1.5,
				getZ() + (random.nextDouble() - 0.5) * 3, 0, 0, 0);
		}
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return stack.isOf(Items.GLOW_BERRIES);
	}

	@Nullable
	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity other) {
		return ModEntities.GLOWTOAD.create(world);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_FROG_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_FROG_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_FROG_DEATH;
	}

	@Override
	public float getSoundPitch() {
		return 0.7f + random.nextFloat() * 0.2f;
	}
}
