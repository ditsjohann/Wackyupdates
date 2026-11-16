package com.wackyupdate.client.mechanic;

import com.wackyupdate.client.render.ModRenderLayers;
import com.wackyupdate.item.SpectreLensItem;
import com.wackyupdate.registry.ModBlocks;
import com.wackyupdate.registry.ModParticles;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Spectre Lens + shovel: hidden gravel mites within 12 blocks get a pulsing outline that shows
 * through other blocks, and rainbow sparks fizz out of their gravel.
 */
public final class LensVision {
	private static final int RADIUS = 12;
	private static final List<BlockPos> FOUND = new ArrayList<>();
	private static int scanTimer;

	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(LensVision::tick);
		WorldRenderEvents.LAST.register(context -> {
			if (FOUND.isEmpty() || context.matrixStack() == null) return;
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.world == null) return;
			Vec3d cam = context.camera().getPos();
			MatrixStack matrices = context.matrixStack();
			VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
			VertexConsumer lines = immediate.getBuffer(ModRenderLayers.XRAY_LINES);
			float pulse = 0.55f + 0.45f * MathHelper.sin((client.world.getTime() + context.tickCounter().getTickDelta(false)) * 0.25f);
			matrices.push();
			matrices.translate(-cam.x, -cam.y, -cam.z);
			for (BlockPos pos : FOUND) {
				WorldRenderer.drawBox(matrices, lines, new Box(pos).contract(0.08), 1.0f, 0.45f, 0.95f, pulse);
			}
			matrices.pop();
			immediate.draw(ModRenderLayers.XRAY_LINES);
		});
	}

	private static void tick(MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		ClientWorld world = client.world;
		if (player == null || world == null || !SpectreLensItem.isLensWithShovel(player)) {
			FOUND.clear();
			scanTimer = 0;
			return;
		}
		if (scanTimer-- <= 0) {
			scanTimer = 10;
			FOUND.clear();
			BlockPos center = player.getBlockPos();
			for (BlockPos pos : BlockPos.iterate(center.add(-RADIUS, -RADIUS, -RADIUS), center.add(RADIUS, RADIUS, RADIUS))) {
				if (world.getBlockState(pos).isOf(ModBlocks.MITE_GRAVEL)) {
					FOUND.add(pos.toImmutable());
				}
			}
		}
		if (!FOUND.isEmpty() && world.getTime() % 3 == 0) {
			BlockPos pos = FOUND.get(world.random.nextInt(FOUND.size()));
			world.addParticle(ModParticles.PRISM_SPARK, pos.getX() + world.random.nextDouble(), pos.getY() + 1.05,
				pos.getZ() + world.random.nextDouble(), 0, 0.03, 0);
		}
	}

	private LensVision() {}
}
