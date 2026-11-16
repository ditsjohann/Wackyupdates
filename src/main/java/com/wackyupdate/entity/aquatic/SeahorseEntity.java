package com.wackyupdate.entity.aquatic;

import com.wackyupdate.registry.ModAquaticItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * A tiny seahorse that loves seagrass and kelp: it follows anyone holding seagrass and likes to
 * cling to kelp to rest. Feed it seagrass and it blesses you with Conduit Power for 30 seconds
 * (one blessing per seahorse every minute).
 */
public class SeahorseEntity extends FishEntity {
	private int clingTicks;
	private int blessingCooldown;

	public SeahorseEntity(EntityType<? extends FishEntity> type, World world) {
		super(type, world);
	}

	@Override
	protected void initGoals() {
		super.initGoals();
		goalSelector.add(1, new TemptGoal(this, 1.3, stack -> stack.isOf(Items.SEAGRASS), false));
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (blessingCooldown > 0) blessingCooldown--;
		if (clingTicks > 0) {
			clingTicks--;
			getNavigation().stop();
			setVelocity(getVelocity().multiply(0.4));
		} else if (random.nextInt(120) == 0 && isPlant(getWorld().getBlockState(getBlockPos()))) {
			clingTicks = 60 + random.nextInt(100);
		}
	}

	private static boolean isPlant(BlockState state) {
		return state.isOf(Blocks.KELP) || state.isOf(Blocks.KELP_PLANT) || state.isOf(Blocks.SEAGRASS) || state.isOf(Blocks.TALL_SEAGRASS);
	}

	public boolean isClinging() {
		return clingTicks > 0;
	}

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (stack.isOf(Items.SEAGRASS)) {
			if (blessingCooldown > 0) return ActionResult.PASS;
			if (getWorld() instanceof ServerWorld server) {
				stack.decrementUnlessCreative(1, player);
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, 600, 0), this);
				server.spawnParticles(ParticleTypes.HEART, getX(), getY() + 0.5, getZ(), 4, 0.3, 0.3, 0.3, 0.0);
				playSound(SoundEvents.ENTITY_DOLPHIN_EAT, 1.0f, 1.6f);
				blessingCooldown = 1200;
			}
			return ActionResult.success(getWorld().isClient);
		}
		return super.interactMob(player, hand);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("BlessingCooldown", blessingCooldown);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		blessingCooldown = nbt.getInt("BlessingCooldown");
	}

	@Override
	public ItemStack getBucketItem() {
		return new ItemStack(ModAquaticItems.SEAHORSE_BUCKET);
	}

	@Override
	protected SoundEvent getFlopSound() {
		return SoundEvents.ENTITY_TROPICAL_FISH_FLOP;
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
