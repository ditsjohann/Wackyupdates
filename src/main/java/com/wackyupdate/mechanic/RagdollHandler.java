package com.wackyupdate.mechanic;

import com.wackyupdate.entity.RagdollEntity;
import com.wackyupdate.registry.ModEntities;
import com.wackyupdate.registry.ModTags;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/**
 * When a mob from the {@code wackyupdate:ragdolls} tag dies, its body is replaced by a ragdoll
 * that gets thrown away from whatever killed it. Loot and XP still drop as normal.
 */
public final class RagdollHandler {
	public static void init() {
		ServerLivingEntityEvents.AFTER_DEATH.register(RagdollHandler::onDeath);
	}

	private static void onDeath(LivingEntity entity, DamageSource source) {
		if (!(entity.getWorld() instanceof ServerWorld world)) return;
		if (!entity.getType().isIn(ModTags.RAGDOLLS) || entity.isBaby()) return;
		if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return; // /kill, the void

		RagdollEntity ragdoll = ModEntities.RAGDOLL.create(world);
		if (ragdoll == null) return;
		ragdoll.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getBodyYaw(), 0f);
		ragdoll.setSource(Registries.ENTITY_TYPE.getId(entity.getType()).toString(), entity instanceof AbstractSkeletonEntity);

		Entity pusher = source.getSource() != null ? source.getSource() : source.getAttacker();
		Vec3d push = new Vec3d(world.random.nextDouble() - 0.5, 0, world.random.nextDouble() - 0.5).multiply(0.2);
		if (pusher != null) {
			Vec3d away = entity.getPos().subtract(pusher.getPos()).multiply(1, 0, 1);
			if (away.lengthSquared() > 1.0E-4) push = away.normalize().multiply(0.45);
		}
		double up = 0.3;
		if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
			push = push.multiply(2.5);
			up = 0.8;
		}
		Vec3d vel = entity.getVelocity();
		ragdoll.setVelocity(vel.x + push.x, Math.max(vel.y, 0) + up, vel.z + push.z);
		world.spawnEntity(ragdoll);

		// Hide the vanilla falling-over corpse; the ragdoll replaces it. Drops already happened.
		entity.setInvisible(true);
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			entity.equipStack(slot, ItemStack.EMPTY);
		}
	}

	private RagdollHandler() {}
}
