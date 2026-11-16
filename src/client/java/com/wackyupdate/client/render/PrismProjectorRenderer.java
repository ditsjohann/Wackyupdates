package com.wackyupdate.client.render;

import com.wackyupdate.block.PrismGlassBlock;
import com.wackyupdate.block.PrismProjectorBlock;
import com.wackyupdate.block.entity.PrismProjectorBlockEntity;
import com.wackyupdate.registry.ModTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Stainable;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Draws the lightshow beams. Everything is driven by world time, so every projector in the
 * world shares the same beat (one beat = 10 ticks = 120 BPM) and they stay in sync.
 */
public class PrismProjectorRenderer implements BlockEntityRenderer<PrismProjectorBlockEntity> {
	private static final Identifier BEAM = Identifier.ofVanilla("textures/entity/beacon_beam.png");
	private static final float INNER = 0.07f;
	private static final float OUTER = 0.13f;

	public PrismProjectorRenderer(BlockEntityRendererFactory.Context ctx) {
	}

	@Override
	public void render(PrismProjectorBlockEntity projector, float tickDelta, MatrixStack matrices, VertexConsumerProvider consumers, int light, int overlay) {
		World world = projector.getWorld();
		if (world == null) return;
		BlockState state = projector.getCachedState();
		if (!state.contains(PrismProjectorBlock.POWERED) || !state.get(PrismProjectorBlock.POWERED)) return;
		Direction facing = state.get(PrismProjectorBlock.FACING);
		BlockPos pos = projector.getPos();
		BlockState lens = world.getBlockState(pos.offset(facing));

		int baseColor;
		boolean rainbow = false;
		if (lens.getBlock() instanceof PrismGlassBlock) {
			baseColor = lens.get(PrismGlassBlock.COLOR).getEntityColor();
		} else if (lens.getBlock() instanceof Stainable stainable) {
			baseColor = stainable.getColor().getEntityColor();
		} else if (lens.isIn(ModTags.RAINBOW_LENSES)) {
			baseColor = 0xFFFFFF;
			rainbow = true;
		} else if (lens.isOf(Blocks.GLASS)) {
			baseColor = 0xFFFFFF;
		} else {
			return; // no crystal lens, no show
		}

		int power = world.getReceivedRedstonePower(pos);
		int length = 6 + power * 2;
		long time = world.getTime();
		float t = time + tickDelta + projector.getPhase() * 2.5f;
		float beat = t / 10f;
		int beatIndex = MathHelper.floor(beat);
		float beatFrac = beat - beatIndex;

		matrices.push();
		matrices.translate(0.5 + facing.getOffsetX(), 0.5 + facing.getOffsetY(), 0.5 + facing.getOffsetZ());
		matrices.multiply(facing.getRotationQuaternion());

		switch (projector.getMode()) {
			case SWEEP -> {
				float swing = MathHelper.sin(t * MathHelper.TAU / 40f) * 38f;
				beam(matrices, consumers, tickDelta, time, 0f, swing, length, color(baseColor, rainbow, t, 0));
				beam(matrices, consumers, tickDelta, time, 0f, -swing, length, color(baseColor, rainbow, t, 3));
			}
			case SPIN -> {
				for (int k = 0; k < 3; k++) {
					beam(matrices, consumers, tickDelta, time, t * 6f + k * 120f, 28f, length, color(baseColor, rainbow, t, k));
				}
			}
			case STROBE -> {
				if (beatFrac < 0.35f) {
					if (beatIndex % 2 == 0) {
						beam(matrices, consumers, tickDelta, time, 0f, 0f, length, color(baseColor, rainbow, beatIndex * 7f, 0));
					} else {
						for (int k = 0; k < 4; k++) {
							beam(matrices, consumers, tickDelta, time, 45f + k * 90f, 32f, length, color(baseColor, rainbow, beatIndex * 7f, k));
						}
					}
				}
			}
			case FAN -> {
				float yaw = (beatIndex % 4) * 45f;
				int fanLength = Math.max(2, Math.round(length * (0.55f + 0.45f * MathHelper.abs(MathHelper.sin(beat * MathHelper.PI)))));
				for (int k = -2; k <= 2; k++) {
					beam(matrices, consumers, tickDelta, time, yaw, k * 18f, fanLength, color(baseColor, rainbow, t, k + 2));
				}
			}
			case DISCO -> {
				Random random = Random.create(pos.asLong() * 31L + beatIndex);
				for (int k = 0; k < 6; k++) {
					float yaw = random.nextFloat() * 360f;
					float tilt = 12f + random.nextFloat() * 45f;
					int c = rainbow ? MathHelper.hsvToRgb(random.nextFloat(), 0.8f, 1f) : baseColor;
					beam(matrices, consumers, tickDelta, time, yaw, tilt, length, c);
				}
			}
		}
		matrices.pop();
	}

	private static int color(int base, boolean rainbow, float t, int index) {
		if (!rainbow) return base;
		return MathHelper.hsvToRgb(((t * 0.01f + index * 0.14f) % 1f + 1f) % 1f, 0.8f, 1f);
	}

	private static void beam(MatrixStack matrices, VertexConsumerProvider consumers, float tickDelta, long time,
			float yaw, float tilt, int length, int rgb) {
		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(tilt));
		matrices.translate(-0.5, 0, -0.5);
		BeaconBlockEntityRenderer.renderBeam(matrices, consumers, BEAM, tickDelta, 1f, time, 0, length, 0xFF000000 | rgb, INNER, OUTER);
		matrices.pop();
	}

	@Override
	public boolean rendersOutsideBoundingBox(PrismProjectorBlockEntity blockEntity) {
		return true;
	}

	@Override
	public int getRenderDistance() {
		return 256;
	}
}
