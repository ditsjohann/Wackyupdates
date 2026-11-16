package com.wackyupdate.client.aquatic;

import com.wackyupdate.registry.ModParticles;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

/**
 * Bioluminescent plankton. At night the sea surface sparkles with it in every ocean, and in the deep
 * (20+ blocks below sea level) it glows all around you, day or night.
 */
public final class GlowingSeas {
	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(GlowingSeas::tick);
	}

	private static void tick(MinecraftClient client) {
		ClientWorld world = client.world;
		ClientPlayerEntity player = client.player;
		if (world == null || player == null || client.isPaused()) return;
		BlockPos center = player.getBlockPos();
		if (!world.getBiome(center).isIn(BiomeTags.IS_OCEAN)) return;
		long dayTime = Math.floorMod(world.getTimeOfDay(), 24000L);
		boolean night = dayTime > 13000 && dayTime < 23000;
		boolean deep = player.isSubmergedIn(FluidTags.WATER) && center.getY() < world.getSeaLevel() - 20;
		if (!night && !deep) return;

		Random random = world.random;
		BlockPos.Mutable pos = new BlockPos.Mutable();
		int tries = deep ? 10 : 14;
		for (int i = 0; i < tries; i++) {
			pos.set(center.getX() + random.nextInt(25) - 12, center.getY() + random.nextInt(13) - 6, center.getZ() + random.nextInt(25) - 12);
			if (!world.getFluidState(pos).isIn(FluidTags.WATER)) continue;
			boolean surface = world.getFluidState(pos.up()).isEmpty();
			if (deep || (night && surface)) {
				world.addParticle(ModParticles.PLANKTON, pos.getX() + random.nextDouble(),
					surface ? pos.getY() + 0.88 : pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(),
					0, surface ? 0 : 0.003, 0);
			}
		}
	}

	private GlowingSeas() {}
}
