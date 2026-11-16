package com.wackyupdate.client.render;

import com.wackyupdate.block.PrismProjectorBlock;
import com.wackyupdate.block.entity.PrismProjectorBlockEntity;
import com.wackyupdate.light.BeamColor;
import com.wackyupdate.light.BeamTracer;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Draws the lightshow beams. Everything is driven by world time, so every projector in the
 * world shares the same beat (one beat = 10 ticks = 120 BPM) and they stay in sync.
 * FOCUS beams are traced through the world with the same {@link BeamTracer} the server uses.
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
		if (!state.contains(PrismProjectorBlock.POWERED)) return;
		boolean powered = state.get(PrismProjectorBlock.POWERED);
		PrismProjectorBlockEntity.Mode mode = projector.getMode();
		if (!powered && mode != PrismProjectorBlockEntity.Mode.MUSIC) return;

		BlockPos pos = projector.getPos();
		BeamColor lens = PrismProjectorBlockEntity.lensColor(world, pos, state);
		if (lens == null) return; // no crystal lens, no show

		long time = world.getTime();
		float t = time + tickDelta;
		if (mode == PrismProjectorBlockEntity.Mode.FOCUS) {
			renderFocus(projector, world, state, time, t, tickDelta, matrices, consumers);
			return;
		}

		Direction facing = state.get(PrismProjectorBlock.FACING);
		int power = world.getReceivedRedstonePower(pos);
		int length = 6 + power * 2;
		float beat = projector.beat(t);
		int beatIndex = MathHelper.floor(beat);
		float beatFrac = beat - beatIndex;
		float shifted = t + projector.getPhase() * 2.5f;

		matrices.push();
		matrices.translate(0.5 + facing.getOffsetX(), 0.5 + facing.getOffsetY(), 0.5 + facing.getOffsetZ());
		matrices.multiply(facing.getRotationQuaternion());

		switch (mode) {
			case SWEEP -> {
				float swing = MathHelper.sin(shifted * MathHelper.TAU / 40f) * 38f;
				beam(matrices, consumers, tickDelta, time, 0f, swing, length, lens.rgb(t, 0));
				beam(matrices, consumers, tickDelta, time, 0f, -swing, length, lens.rgb(t, 3));
			}
			case SPIN -> {
				for (int k = 0; k < 3; k++) {
					beam(matrices, consumers, tickDelta, time, shifted * 6f + k * 120f, 28f, length, lens.rgb(t, k));
				}
			}
			case STROBE -> {
				if (beatFrac < 0.35f) {
					if (beatIndex % 2 == 0) {
						beam(matrices, consumers, tickDelta, time, 0f, 0f, length, lens.rgb(beatIndex * 7f, 0));
					} else {
						for (int k = 0; k < 4; k++) {
							beam(matrices, consumers, tickDelta, time, 45f + k * 90f, 32f, length, lens.rgb(beatIndex * 7f, k));
						}
					}
				}
			}
			case FAN -> {
				float yaw = (Math.floorMod(beatIndex, 4)) * 45f;
				int fanLength = Math.max(2, Math.round(length * (0.55f + 0.45f * MathHelper.abs(MathHelper.sin(beat * MathHelper.PI)))));
				for (int k = -2; k <= 2; k++) {
					beam(matrices, consumers, tickDelta, time, yaw, k * 18f, fanLength, lens.rgb(t, k + 2));
				}
			}
			case DISCO -> disco(matrices, consumers, tickDelta, time, pos.asLong() * 31L + beatIndex, 6, length, lens);
			case MUSIC -> renderMusic(projector, matrices, consumers, tickDelta, time, t, power, beatIndex, beatFrac, lens);
			default -> {
			}
		}
		matrices.pop();
	}

	private static void renderMusic(PrismProjectorBlockEntity projector, MatrixStack matrices, VertexConsumerProvider consumers,
			float tickDelta, long time, float t, int power, int beatIndex, float beatFrac, BeamColor lens) {
		int length = 12 + power * 2;
		// note burst: direction seeded by the note's time, colour by pitch (rainbow lens) or lens colour
		float since = time - projector.getLastNoteTime() + tickDelta;
		if (since >= 0 && since < 10) {
			float fade = 1f - since / 10f;
			int rgb = lens.rainbow() ? MathHelper.hsvToRgb(projector.getLastNote() / 24f, 0.85f, 1f) : lens.rgb(t, 0);
			Random random = Random.create(projector.getLastNoteTime() * 31L + projector.getPos().asLong());
			int count = 2 + projector.getLastNote() / 8;
			for (int k = 0; k < count; k++) {
				beam(matrices, consumers, tickDelta, time, random.nextFloat() * 360f, 8f + random.nextFloat() * 40f,
					Math.max(1, Math.round(length * fade)), rgb);
			}
		}
		// jukebox playing: dance on the beat
		if (projector.isJukeboxPlaying(time) && beatFrac < 0.5f) {
			disco(matrices, consumers, tickDelta, time, projector.getPos().asLong() * 17L + beatIndex, 4, length,
				lens.rainbow() || lens.dye() == null ? BeamColor.RAINBOW : lens);
		}
	}

	private static void disco(MatrixStack matrices, VertexConsumerProvider consumers, float tickDelta, long time, long seed,
			int count, int length, BeamColor color) {
		Random random = Random.create(seed);
		for (int k = 0; k < count; k++) {
			float yaw = random.nextFloat() * 360f;
			float tilt = 12f + random.nextFloat() * 45f;
			int c = color.rainbow() ? MathHelper.hsvToRgb(random.nextFloat(), 0.8f, 1f) : color.rgb(0, 0);
			beam(matrices, consumers, tickDelta, time, yaw, tilt, length, c);
		}
	}

	private static void renderFocus(PrismProjectorBlockEntity projector, World world, BlockState state, long time, float t,
			float tickDelta, MatrixStack matrices, VertexConsumerProvider consumers) {
		if (projector.cachedTrace == null || projector.cachedTraceTime != time) {
			projector.cachedTrace = projector.traceFocus(world, state);
			projector.cachedTraceTime = time;
		}
		BlockPos pos = projector.getPos();
		int index = 0;
		for (BeamTracer.Segment segment : projector.cachedTrace.segments()) {
			Vec3d delta = segment.end().subtract(segment.start());
			double len = delta.length();
			if (len < 0.01) continue;
			Direction dir = Direction.getFacing(delta.x, delta.y, delta.z);
			float radius = 0.035f + segment.strength() * 0.004f;
			int whole = MathHelper.ceil(len);
			matrices.push();
			matrices.translate(segment.start().x - pos.getX(), segment.start().y - pos.getY(), segment.start().z - pos.getZ());
			matrices.multiply(dir.getRotationQuaternion());
			matrices.scale(1f, (float) (len / whole), 1f);
			matrices.translate(-0.5, 0, -0.5);
			BeaconBlockEntityRenderer.renderBeam(matrices, consumers, BEAM, tickDelta, 1f, time, 0, whole,
				0xFF000000 | segment.color().rgb(t, index++), radius, radius * 1.8f);
			matrices.pop();
		}
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
