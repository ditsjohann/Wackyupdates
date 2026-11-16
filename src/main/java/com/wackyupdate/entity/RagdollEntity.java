package com.wackyupdate.entity;

import com.wackyupdate.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A floppy body. Mobs in the {@code wackyupdate:ragdolls} tag leave one behind when they die,
 * and the Ragdoll Dummy item places a permanent one you can beat up, launch with explosions,
 * knock off cliffs and boost-pad across the map.
 *
 * The server simulates the body (gravity, bounces, knockback). Each client then runs a
 * spring-damper "floppy limbs" simulation on top for the visual ragdoll pose.
 */
public class RagdollEntity extends Entity {
	private static final TrackedData<String> SOURCE = DataTracker.registerData(RagdollEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<Boolean> SKINNY = DataTracker.registerData(RagdollEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Boolean> DUMMY = DataTracker.registerData(RagdollEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Integer> HITS = DataTracker.registerData(RagdollEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final int LIFETIME = 200;
	public static final String DUMMY_SOURCE = "wackyupdate:ragdoll_dummy";

	// ---- client-side visual simulation state (degrees) ----
	public static final int HEAD = 0, RIGHT_ARM = 1, LEFT_ARM = 2, RIGHT_LEG = 3, LEFT_LEG = 4;
	public float bodyPitch, prevBodyPitch, bodyRoll, prevBodyRoll;
	private float pitchVel, rollVel;
	public final float[] limbPitch = new float[5], prevLimbPitch = new float[5], limbRoll = new float[5], prevLimbRoll = new float[5];
	private final float[] limbPitchVel = new float[5], limbRollVel = new float[5], splayPitch = new float[5], splayRoll = new float[5];
	private boolean visualsStarted;
	private int lastHits;
	private boolean wasOnGround;
	private double lastVelY;
	private int restTicks;

	public RagdollEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		builder.add(SOURCE, "minecraft:zombie");
		builder.add(SKINNY, false);
		builder.add(DUMMY, false);
		builder.add(HITS, 0);
	}

	public void setSource(String entityTypeId, boolean skinny) {
		dataTracker.set(SOURCE, entityTypeId);
		dataTracker.set(SKINNY, skinny);
	}

	public void setDummy(boolean dummy) {
		dataTracker.set(DUMMY, dummy);
		if (dummy) dataTracker.set(SOURCE, DUMMY_SOURCE);
	}

	public String getSource() {
		return dataTracker.get(SOURCE);
	}

	public boolean isSkinny() {
		return dataTracker.get(SKINNY);
	}

	public boolean isDummy() {
		return dataTracker.get(DUMMY);
	}

	@Override
	public void tick() {
		super.tick();
		Vec3d v = getVelocity();
		lastVelY = v.y;
		if (!hasNoGravity()) {
			v = v.add(0, -0.06, 0);
		}
		setVelocity(v);
		move(MovementType.SELF, getVelocity());
		v = getVelocity();
		if (isOnGround()) {
			// little bounce when it slams into the ground
			if (lastVelY < -0.35 && !wasOnGround) {
				v = new Vec3d(v.x, -lastVelY * 0.3, v.z);
			}
			v = v.multiply(0.6, 1.0, 0.6);
		}
		setVelocity(v.multiply(0.98));

		World world = getWorld();
		if (world.isClient) {
			tickVisuals();
		} else if (!isDummy() && age > LIFETIME) {
			((ServerWorld) world).spawnParticles(ParticleTypes.POOF, getX(), getY() + 0.2, getZ(), 10, 0.3, 0.1, 0.3, 0.02);
			discard();
		}
		wasOnGround = isOnGround();
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (getWorld().isClient || isRemoved() || isInvulnerableTo(source)) return false;
		Entity attacker = source.getAttacker();
		if (isDummy() && attacker instanceof PlayerEntity player) {
			if (player.isSneaking()) {
				dropStack(new ItemStack(ModItems.RAGDOLL_DUMMY));
				playSound(SoundEvents.ENTITY_ARMOR_STAND_BREAK, 1.0f, 1.2f);
				discard();
				return true;
			}
			player.sendMessage(Text.translatable("message.wackyupdate.dummy_hit", String.format("%.1f", amount)).formatted(Formatting.GOLD), true);
		}
		Entity pusher = source.getSource() != null ? source.getSource() : attacker;
		float strength = Math.min(amount, 20f);
		Vec3d push = Vec3d.ZERO;
		if (pusher != null) {
			push = getPos().subtract(pusher.getPos()).multiply(1, 0, 1);
			push = push.lengthSquared() < 1.0E-4 ? Vec3d.ZERO : push.normalize().multiply(0.3 + strength * 0.04);
		}
		setVelocity(getVelocity().add(push.x, 0.2 + strength * 0.025, push.z));
		velocityModified = true;
		dataTracker.set(HITS, dataTracker.get(HITS) + 1);
		playSound(isDummy() ? SoundEvents.BLOCK_WOOL_HIT : SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 0.9f + random.nextFloat() * 0.3f);
		if (!source.isIn(DamageTypeTags.IS_FIRE)) {
			age = Math.max(0, age - 40); // keep a ragdoll around a bit longer while you play with it
		}
		return true;
	}

	@Override
	public boolean canHit() {
		return !isRemoved();
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public boolean shouldSave() {
		return isDummy() && super.shouldSave();
	}

	@Nullable
	@Override
	public ItemStack getPickBlockStack() {
		return isDummy() ? new ItemStack(ModItems.RAGDOLL_DUMMY) : null;
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		setSource(nbt.getString("Source"), nbt.getBoolean("Skinny"));
		setDummy(nbt.getBoolean("Dummy"));
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		nbt.putString("Source", getSource());
		nbt.putBoolean("Skinny", isSkinny());
		nbt.putBoolean("Dummy", isDummy());
	}

	// ------------------------------------------------------------------ visual floppiness (client)

	private void tickVisuals() {
		prevBodyPitch = bodyPitch;
		prevBodyRoll = bodyRoll;
		System.arraycopy(limbPitch, 0, prevLimbPitch, 0, 5);
		System.arraycopy(limbRoll, 0, prevLimbRoll, 0, 5);

		if (!visualsStarted) {
			visualsStarted = true;
			Random seeded = Random.create(getUuid().getLeastSignificantBits());
			for (int i = 0; i < 5; i++) {
				splayPitch[i] = (seeded.nextFloat() - 0.5f) * 50f;
				splayRoll[i] = (seeded.nextFloat() * 35f + 8f) * (i == RIGHT_ARM || i == RIGHT_LEG ? 1 : -1);
			}
			splayRoll[HEAD] = (seeded.nextFloat() - 0.5f) * 40f;
			lastHits = dataTracker.get(HITS);
			if (isDummy()) {
				bodyPitch = prevBodyPitch = 0f;
			} else {
				impulse(1.3f);
			}
		}
		int hits = dataTracker.get(HITS);
		if (hits != lastHits) {
			lastHits = hits;
			impulse(1.0f);
		}
		if (isOnGround() && !wasOnGround && lastVelY < -0.25) {
			impulse(0.5f);
		}

		float oldPitchVel = pitchVel;
		float oldRollVel = rollVel;
		boolean grounded = isOnGround();
		boolean standingDummy = isDummy() && grounded && Math.abs(bodyPitch) < 35f && getVelocity().horizontalLengthSquared() < 0.01;
		if (standingDummy) {
			// a dummy wobbles back upright like a punching bag
			pitchVel += -bodyPitch * 0.15f;
			rollVel += -bodyRoll * 0.15f;
			pitchVel *= 0.82f;
			rollVel *= 0.82f;
		} else if (grounded) {
			float target = bodyPitch >= 0 ? 90f : -90f;
			// dummies get back up after lying still for a couple of seconds
			restTicks = getVelocity().horizontalLengthSquared() < 0.01 ? restTicks + 1 : 0;
			if (isDummy() && restTicks > 50) target = 0f;
			pitchVel += (target - bodyPitch) * 0.14f;
			rollVel += (-bodyRoll) * 0.1f;
			pitchVel *= 0.68f;
			rollVel *= 0.68f;
		} else {
			pitchVel *= 0.99f;
			rollVel *= 0.99f;
		}
		bodyPitch = MathHelper.wrapDegrees(bodyPitch + pitchVel);
		bodyRoll = MathHelper.clamp(bodyRoll + rollVel, -80f, 80f);
		if (Math.abs(bodyPitch - prevBodyPitch) > 180f) prevBodyPitch = bodyPitch; // avoid lerp spin on wrap
		float pitchAccel = pitchVel - oldPitchVel;
		float rollAccel = rollVel - oldRollVel;

		boolean lying = grounded && Math.abs(bodyPitch) > 55f;
		for (int i = 0; i < 5; i++) {
			float targetPitch;
			float targetRoll;
			if (standingDummy) {
				targetPitch = 0f;
				targetRoll = i == HEAD ? 0f : splayRoll[i] * 0.3f;
			} else if (lying) {
				targetPitch = splayPitch[i] * 0.5f;
				targetRoll = splayRoll[i];
			} else {
				// limbs try to hang towards the ground
				targetPitch = MathHelper.clamp(-bodyPitch, -150f, 150f) + splayPitch[i] * 0.3f;
				targetRoll = MathHelper.clamp(-bodyRoll, -70f, 70f) + splayRoll[i] * 0.5f;
			}
			float inertia = i == HEAD ? 0.6f : 1.2f;
			limbPitchVel[i] += (targetPitch - limbPitch[i]) * 0.18f - pitchAccel * inertia;
			limbRollVel[i] += (targetRoll - limbRoll[i]) * 0.18f - rollAccel * inertia;
			limbPitchVel[i] *= 0.78f;
			limbRollVel[i] *= 0.78f;
			limbPitch[i] += limbPitchVel[i];
			limbRoll[i] += limbRollVel[i];
			clampJoint(i);
		}
	}

	private void clampJoint(int i) {
		switch (i) {
			case HEAD -> {
				limbPitch[i] = MathHelper.clamp(limbPitch[i], -70f, 70f);
				limbRoll[i] = MathHelper.clamp(limbRoll[i], -45f, 45f);
			}
			case RIGHT_ARM, LEFT_ARM -> {
				limbPitch[i] = MathHelper.clamp(limbPitch[i], -175f, 70f);
				limbRoll[i] = MathHelper.clamp(limbRoll[i], -130f, 130f);
			}
			default -> {
				limbPitch[i] = MathHelper.clamp(limbPitch[i], -110f, 70f);
				limbRoll[i] = MathHelper.clamp(limbRoll[i], -50f, 50f);
			}
		}
	}

	private void impulse(float strength) {
		pitchVel += (random.nextBoolean() ? 1 : -1) * (12f + random.nextFloat() * 18f) * strength;
		rollVel += (random.nextFloat() - 0.5f) * 20f * strength;
		for (int i = 0; i < 5; i++) {
			limbPitchVel[i] += (random.nextFloat() - 0.5f) * 60f * strength;
			limbRollVel[i] += (random.nextFloat() - 0.5f) * 50f * strength;
		}
	}
}
