package com.wackyupdate.mechanic;

import com.wackyupdate.WackyUpdate;
import com.wackyupdate.registry.ModItems;
import com.wackyupdate.registry.ModTags;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Rollerskate logic.
 * - On blocks in {@code #wackyupdate:skateable} (slickwood, ramps, floors, rails, pads, smooth stone...)
 *   skaters get +90% speed and a taller step so ramps/curbs are smooth.
 * - Skate tricks: launch off a skate surface and tap SNEAK while airborne. Each tap is a trick;
 *   land to cash in the combo for XP (combos scale quadratically).
 */
public final class SkateHandler {
	private static final Identifier SPEED_ID = WackyUpdate.id("skate_speed");
	private static final Identifier STEP_ID = WackyUpdate.id("skate_step");
	private static final Map<UUID, TrickState> TRICKS = new HashMap<>();

	private static final class TrickState {
		int airTicks;
		int ticksSinceSkateGround = 100;
		boolean lastSneak;
		int tricks;
	}

	public static boolean isWearingSkates(LivingEntity entity) {
		return entity.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.ROLLERSKATES);
	}

	public static boolean isOnSkateSurface(LivingEntity entity) {
		BlockState at = entity.getWorld().getBlockState(entity.getBlockPos());
		BlockState below = entity.getWorld().getBlockState(entity.getSteppingPos());
		return at.isIn(ModTags.SKATEABLE) || below.isIn(ModTags.SKATEABLE);
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(SkateHandler::tick);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> TRICKS.remove(handler.player.getUuid()));
	}

	private static void tick(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			boolean skates = isWearingSkates(player) && !player.isSpectator();
			boolean onSurface = skates && isOnSkateSurface(player);
			setModifier(player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED), SPEED_ID, onSurface ? 0.9 : 0,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			setModifier(player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT), STEP_ID, skates ? 0.5 : 0,
				EntityAttributeModifier.Operation.ADD_VALUE);
			if (skates) {
				tickTricks(player, onSurface);
			} else {
				TRICKS.remove(player.getUuid());
			}
		}
	}

	private static void setModifier(EntityAttributeInstance instance, Identifier id, double value, EntityAttributeModifier.Operation op) {
		if (instance == null) return;
		if (value == 0) {
			if (instance.hasModifier(id)) instance.removeModifier(id);
		} else if (!instance.hasModifier(id)) {
			instance.addTemporaryModifier(new EntityAttributeModifier(id, value, op));
		}
	}

	private static void tickTricks(ServerPlayerEntity player, boolean onSurface) {
		TrickState s = TRICKS.computeIfAbsent(player.getUuid(), u -> new TrickState());
		boolean sneaking = player.isSneaking();
		ServerWorld world = player.getServerWorld();
		if (player.isOnGround()) {
			if (s.tricks > 0 && s.airTicks > 6) {
				int combo = s.tricks;
				int xp = Math.min(combo * combo + 1, 40);
				player.addExperience(xp);
				player.sendMessage(Text.translatable(combo > 1 ? "message.wackyupdate.trick_combo" : "message.wackyupdate.trick", combo, xp)
					.formatted(Formatting.YELLOW, Formatting.BOLD), true);
				world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5f, 1.5f + Math.min(combo, 5) * 0.1f);
				world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 0.2, player.getZ(), 10 + combo * 4, 0.4, 0.1, 0.4, 0.3);
			}
			s.airTicks = 0;
			s.tricks = 0;
			s.ticksSinceSkateGround = onSurface ? 0 : s.ticksSinceSkateGround + 1;
		} else {
			s.airTicks++;
			s.ticksSinceSkateGround++;
			boolean launchedFromSkate = s.ticksSinceSkateGround <= s.airTicks + 2;
			if (launchedFromSkate && s.airTicks > 2 && sneaking && !s.lastSneak && !player.isFallFlying() && !player.getAbilities().flying) {
				s.tricks++;
				world.spawnParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 0.5, player.getZ(), 12, 0.4, 0.4, 0.4, 0.2);
				world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.4f, 1.6f + s.tricks * 0.05f);
			}
		}
		s.lastSneak = sneaking;
	}

	private SkateHandler() {}
}
