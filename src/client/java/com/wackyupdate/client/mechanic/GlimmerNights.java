package com.wackyupdate.client.mechanic;

import com.wackyupdate.registry.ModParticles;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

/**
 * Ambient fireflies over flowers and tall grass at night. Roughly one night in three is a
 * "Glimmer Night", when the meadows light up with far more of them.
 */
public final class GlimmerNights {
	private static long announcedNight = -1;

	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(GlimmerNights::tick);
	}

	public static boolean isGlimmerNight(long day) {
		return Math.floorMod(day * 2654435761L ^ 0x5DEECE66DL, 3L) == 0;
	}

	private static void tick(MinecraftClient client) {
		ClientWorld world = client.world;
		if (world == null || client.player == null || client.isPaused()) return;
		if (!world.getDimension().hasSkyLight() || world.getDimension().hasFixedTime() || world.isRaining()) return;
		long time = world.getTimeOfDay();
		long dayTime = Math.floorMod(time, 24000L);
		if (dayTime < 13000 || dayTime > 23000) return;
		long day = Math.floorDiv(time, 24000L);
		boolean glimmer = isGlimmerNight(day);

		if (glimmer && announcedNight != day && dayTime < 14000) {
			announcedNight = day;
			client.player.sendMessage(Text.translatable("message.wackyupdate.glimmer_night").formatted(Formatting.YELLOW, Formatting.ITALIC), true);
		}

		Random random = world.random;
		BlockPos center = client.player.getBlockPos();
		int tries = glimmer ? 14 : 3;
		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int i = 0; i < tries; i++) {
			pos.set(center.getX() + random.nextInt(33) - 16, center.getY() + random.nextInt(13) - 6, center.getZ() + random.nextInt(33) - 16);
			BlockState state = world.getBlockState(pos);
			boolean plant = state.isIn(BlockTags.FLOWERS) || state.isOf(Blocks.TALL_GRASS) || state.isOf(Blocks.SHORT_GRASS) && glimmer;
			if (plant && random.nextFloat() < (glimmer ? 0.6f : 0.25f) && world.getLightLevel(net.minecraft.world.LightType.SKY, pos) > 8) {
				world.addParticle(ModParticles.FIREFLY, pos.getX() + random.nextDouble(), pos.getY() + 0.4 + random.nextDouble() * 1.5,
					pos.getZ() + random.nextDouble(), 0, 0, 0);
			}
		}
	}

	private GlimmerNights() {}
}
