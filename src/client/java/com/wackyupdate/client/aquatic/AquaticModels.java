package com.wackyupdate.client.aquatic;

import com.wackyupdate.entity.aquatic.*;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * Models for the aquatic update. The UV layouts here are mirrored in tools/generate_assets.py
 * (paint_box calls) - change both together.
 */
public final class AquaticModels {
	private AquaticModels() {}

	/** Shared base: one root part and a swimming tail wiggle. */
	public abstract static class Base<T extends Entity> extends SinglePartEntityModel<T> {
		protected final ModelPart root;

		protected Base(ModelPart root) {
			this.root = root;
		}

		@Override
		public ModelPart getPart() {
			return root;
		}

		protected static float wiggle(Entity entity, float animationProgress, float speed) {
			float strength = entity.isTouchingWater() ? 1.0f : 1.6f;
			return strength * 0.45f * MathHelper.sin(speed * animationProgress);
		}
	}

	// --------------------------------------------------------------------------------------------- glimmerfin
	public static class Glimmerfin extends Base<GlimmerfinEntity> {
		private final ModelPart tail;

		public Glimmerfin(ModelPart root) {
			super(root);
			tail = root.getChild("tail");
		}

		public static TexturedModelData data() {
			ModelData data = new ModelData();
			ModelPartData r = data.getRoot();
			r.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-1f, -1.5f, -3f, 2, 3, 6), ModelTransform.pivot(0, 22, 0));
			r.addChild("tail", ModelPartBuilder.create().uv(16, 0).cuboid(0f, -1.5f, 0f, 0, 3, 3), ModelTransform.pivot(0, 22, 3));
			r.addChild("dorsal", ModelPartBuilder.create().uv(16, 6).cuboid(0f, -1f, 0f, 0, 1, 3), ModelTransform.pivot(0, 20.5f, -1));
			return TexturedModelData.of(data, 32, 32);
		}

		@Override
		public void setAngles(GlimmerfinEntity e, float limbAngle, float limbDistance, float progress, float headYaw, float headPitch) {
			tail.yaw = -wiggle(e, progress, 0.9f) * 1.6f;
		}
	}

	// --------------------------------------------------------------------------------------------- carp
	public static class Carp extends Base<SunscaleCarpEntity> {
		private final ModelPart tail;

		public Carp(ModelPart root) {
			super(root);
			tail = root.getChild("tail");
		}

		public static TexturedModelData data() {
			ModelData data = new ModelData();
			ModelPartData r = data.getRoot();
			r.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-2f, -2.5f, -4f, 4, 5, 9), ModelTransform.pivot(0, 21.5f, 0));
			r.addChild("head", ModelPartBuilder.create().uv(0, 14).cuboid(-1.5f, -2f, -3f, 3, 4, 3), ModelTransform.pivot(0, 21.5f, -4));
			r.addChild("tail", ModelPartBuilder.create().uv(28, 0).cuboid(0f, -2.5f, 0f, 0, 5, 5), ModelTransform.pivot(0, 21.5f, 5));
			r.addChild("dorsal", ModelPartBuilder.create().uv(28, 10).cuboid(0f, -2f, -3f, 0, 2, 6), ModelTransform.pivot(0, 19, 0));
			return TexturedModelData.of(data, 64, 32);
		}

		@Override
		public void setAngles(SunscaleCarpEntity e, float limbAngle, float limbDistance, float progress, float headYaw, float headPitch) {
			tail.yaw = -wiggle(e, progress, 0.6f) * 1.4f;
		}
	}

	// --------------------------------------------------------------------------------------------- sea bunny
	public static class SeaBunny extends Base<SeaBunnyEntity> {
		private final ModelPart body, leftEar, rightEar;

		public SeaBunny(ModelPart root) {
			super(root);
			body = root.getChild("body");
			leftEar = root.getChild("left_ear");
			rightEar = root.getChild("right_ear");
		}

		public static TexturedModelData data() {
			ModelData data = new ModelData();
			ModelPartData r = data.getRoot();
			r.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-1.5f, -2f, -2.5f, 3, 2, 5), ModelTransform.pivot(0, 24, 0));
			r.addChild("left_ear", ModelPartBuilder.create().uv(0, 8).cuboid(-0.5f, -2f, -0.5f, 1, 2, 1), ModelTransform.pivot(0.9f, 22.2f, -1.8f));
			r.addChild("right_ear", ModelPartBuilder.create().uv(0, 8).cuboid(-0.5f, -2f, -0.5f, 1, 2, 1), ModelTransform.pivot(-0.9f, 22.2f, -1.8f));
			r.addChild("tail", ModelPartBuilder.create().uv(6, 8).cuboid(-1f, -1f, 0f, 2, 1, 1), ModelTransform.pivot(0, 23.5f, 2.5f));
			return TexturedModelData.of(data, 32, 32);
		}

		@Override
		public void setAngles(SeaBunnyEntity e, float limbAngle, float limbDistance, float progress, float headYaw, float headPitch) {
			body.zScale = 1f + MathHelper.sin(limbAngle * 1.2f) * 0.12f * limbDistance;
			leftEar.roll = 0.25f + MathHelper.sin(progress * 0.1f) * 0.15f;
			rightEar.roll = -0.25f - MathHelper.sin(progress * 0.1f + 1f) * 0.15f;
		}
	}

	// --------------------------------------------------------------------------------------------- hermit crab
	public static class HermitCrab extends EntityModel<HermitCrabEntity> {
		public static final int[] SHELL_COLORS = {0xFFF2D7A6, 0xFFE89A8C, 0xFFB7D7F0, 0xFFC9E6A6, 0xFFE6C3F0};
		private final ModelPart root, shell, leftClaw, rightClaw, leftEye, rightEye;
		private final ModelPart[] legs = new ModelPart[4];
		private int shellColor = 0xFFFFFFFF;

		public HermitCrab(ModelPart root) {
			this.root = root;
			shell = root.getChild("shell");
			leftClaw = root.getChild("left_claw");
			rightClaw = root.getChild("right_claw");
			leftEye = root.getChild("left_eye");
			rightEye = root.getChild("right_eye");
			for (int i = 0; i < 4; i++) legs[i] = root.getChild("leg" + i);
		}

		public static TexturedModelData data() {
			ModelData data = new ModelData();
			ModelPartData r = data.getRoot();
			r.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-2f, -1.5f, -2f, 4, 2, 3), ModelTransform.pivot(0, 22, -1));
			r.addChild("left_claw", ModelPartBuilder.create().uv(14, 0).cuboid(-1f, -1f, -2f, 2, 2, 2), ModelTransform.pivot(2.5f, 22, -2.5f));
			r.addChild("right_claw", ModelPartBuilder.create().uv(14, 0).cuboid(-1f, -1f, -2f, 2, 2, 2), ModelTransform.pivot(-2.5f, 22, -2.5f));
			r.addChild("left_eye", ModelPartBuilder.create().uv(0, 5).cuboid(-0.5f, -2f, -0.5f, 1, 2, 1), ModelTransform.pivot(1f, 20.5f, -2.5f));
			r.addChild("right_eye", ModelPartBuilder.create().uv(0, 5).cuboid(-0.5f, -2f, -0.5f, 1, 2, 1), ModelTransform.pivot(-1f, 20.5f, -2.5f));
			float[] z = {-1.5f, 0f};
			for (int i = 0; i < 4; i++) {
				boolean right = i % 2 == 0;
				r.addChild("leg" + i, ModelPartBuilder.create().uv(0, 8).cuboid(right ? 0f : -3f, 0f, -0.5f, 3, 1, 1),
					ModelTransform.of(right ? 1.8f : -1.8f, 22.5f, z[i / 2], 0, 0, right ? 0.5f : -0.5f));
			}
			r.addChild("shell", ModelPartBuilder.create().uv(0, 12).cuboid(-3f, -5f, -3f, 6, 5, 6), ModelTransform.pivot(0, 22.5f, 1.5f));
			return TexturedModelData.of(data, 64, 32);
		}

		@Override
		public void setAngles(HermitCrabEntity e, float limbAngle, float limbDistance, float progress, float headYaw, float headPitch) {
			shellColor = SHELL_COLORS[Math.floorMod(e.getShell(), SHELL_COLORS.length)];
			boolean hiding = e.isHiding();
			leftClaw.visible = rightClaw.visible = leftEye.visible = rightEye.visible = !hiding;
			for (ModelPart leg : legs) leg.visible = !hiding;
			shell.pivotY = hiding ? 24.5f : 22.5f;
			shell.pivotZ = hiding ? 0f : 1.5f;
			for (int i = 0; i < 4; i++) {
				legs[i].yaw = MathHelper.cos(limbAngle * 2f + i * 1.7f) * 0.6f * limbDistance;
			}
			leftClaw.pitch = rightClaw.pitch = MathHelper.sin(progress * 0.15f) * 0.2f;
		}

		@Override
		public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
			for (String name : new String[]{"body", "left_claw", "right_claw", "left_eye", "right_eye", "leg0", "leg1", "leg2", "leg3"}) {
				root.getChild(name).render(matrices, vertices, light, overlay, color);
			}
			shell.render(matrices, vertices, light, overlay, multiply(color, shellColor));
		}
	}

	static int multiply(int a, int b) {
		int alpha = ((a >>> 24) * (b >>> 24)) / 255;
		int r = (((a >> 16) & 0xFF) * ((b >> 16) & 0xFF)) / 255;
		int g = (((a >> 8) & 0xFF) * ((b >> 8) & 0xFF)) / 255;
		int bl = ((a & 0xFF) * (b & 0xFF)) / 255;
		return alpha << 24 | r << 16 | g << 8 | bl;
	}

	// --------------------------------------------------------------------------------------------- seahorse
	public static class Seahorse extends Base<SeahorseEntity> {
		private final ModelPart body, tail, fin;

		public Seahorse(ModelPart root) {
			super(root);
			body = root.getChild("body");
			tail = root.getChild("tail");
			fin = root.getChild("fin");
		}

		public static TexturedModelData data() {
			ModelData data = new ModelData();
			ModelPartData r = data.getRoot();
			r.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-1f, -4f, -1f, 2, 6, 2), ModelTransform.pivot(0, 18, 0));
			r.addChild("head", ModelPartBuilder.create().uv(8, 0).cuboid(-1f, -2f, -2.5f, 2, 2, 3)
				.uv(20, 0).cuboid(-0.5f, -1.2f, -4.5f, 1, 1, 2), ModelTransform.pivot(0, 14, 0));
			ModelPartData tail = r.addChild("tail", ModelPartBuilder.create().uv(0, 8).cuboid(-0.5f, 0f, -0.5f, 1, 4, 1),
				ModelTransform.of(0, 20, 0.3f, 0.5f, 0, 0));
			tail.addChild("tip", ModelPartBuilder.create().uv(4, 8).cuboid(-0.5f, 0f, -0.5f, 1, 2, 1), ModelTransform.of(0, 4, 0, 1.4f, 0, 0));
			r.addChild("fin", ModelPartBuilder.create().uv(8, 6).cuboid(0f, -1.5f, 0f, 0, 3, 2), ModelTransform.pivot(0, 17, 1));
			return TexturedModelData.of(data, 32, 32);
		}

		@Override
		public void setAngles(SeahorseEntity e, float limbAngle, float limbDistance, float progress, float headYaw, float headPitch) {
			fin.yaw = MathHelper.sin(progress * 1.2f) * 0.5f;
			tail.pitch = 0.5f + MathHelper.sin(progress * 0.1f) * 0.15f;
			body.roll = MathHelper.sin(progress * 0.07f) * 0.05f;
		}
	}

	// --------------------------------------------------------------------------------------------- drift jelly
	public static class DriftJelly extends Base<DriftJellyEntity> {
		private final ModelPart bell, oral;
		private final ModelPart[] tentacles = new ModelPart[4];

		public DriftJelly(ModelPart root) {
			super(root);
			bell = root.getChild("bell");
			oral = root.getChild("oral");
			for (int i = 0; i < 4; i++) tentacles[i] = root.getChild("tentacle" + i);
		}

		public static TexturedModelData data() {
			ModelData data = new ModelData();
			ModelPartData r = data.getRoot();
			r.addChild("bell", ModelPartBuilder.create().uv(0, 0).cuboid(-4f, -4f, -4f, 8, 4, 8), ModelTransform.pivot(0, 14, 0));
			r.addChild("oral", ModelPartBuilder.create().uv(36, 0).cuboid(-1f, 0f, -1f, 2, 7, 2), ModelTransform.pivot(0, 14, 0));
			float[][] p = {{2.5f, 2.5f}, {-2.5f, 2.5f}, {2.5f, -2.5f}, {-2.5f, -2.5f}};
			for (int i = 0; i < 4; i++) {
				r.addChild("tentacle" + i, ModelPartBuilder.create().uv(32, 0).cuboid(-0.5f, 0f, -0.5f, 1, 9, 1),
					ModelTransform.pivot(p[i][0], 14, p[i][1]));
			}
			return TexturedModelData.of(data, 64, 32);
		}

		@Override
		public void setAngles(DriftJellyEntity e, float limbAngle, float limbDistance, float progress, float headYaw, float headPitch) {
			float pulse = MathHelper.sin(progress * 0.12f);
			bell.xScale = bell.zScale = 1f + pulse * 0.12f;
			bell.yScale = 1f - pulse * 0.15f;
			for (int i = 0; i < 4; i++) {
				tentacles[i].pitch = MathHelper.sin(progress * 0.08f + i) * 0.25f;
				tentacles[i].roll = MathHelper.cos(progress * 0.07f + i * 1.3f) * 0.25f;
			}
			oral.yaw = progress * 0.02f;
		}
	}

	// --------------------------------------------------------------------------------------------- stingray
	public static class Stingray extends Base<StingrayEntity> {
		private final ModelPart leftWing, rightWing, tail;

		public Stingray(ModelPart root) {
			super(root);
			leftWing = root.getChild("left_wing");
			rightWing = root.getChild("right_wing");
			tail = root.getChild("tail");
		}

		public static TexturedModelData data() {
			ModelData data = new ModelData();
			ModelPartData r = data.getRoot();
			r.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-2f, -1.5f, -5f, 4, 2, 9), ModelTransform.pivot(0, 23, 0));
			r.addChild("right_wing", ModelPartBuilder.create().uv(0, 12).cuboid(-5f, -0.5f, -4f, 5, 1, 8), ModelTransform.pivot(-2, 23, 0));
			r.addChild("left_wing", ModelPartBuilder.create().uv(0, 12).mirrored().cuboid(0f, -0.5f, -4f, 5, 1, 8), ModelTransform.pivot(2, 23, 0));
			r.addChild("tail", ModelPartBuilder.create().uv(26, 0).cuboid(-0.5f, -0.5f, 0f, 1, 1, 10), ModelTransform.pivot(0, 23, 4));
			return TexturedModelData.of(data, 64, 32);
		}

		@Override
		public void setAngles(StingrayEntity e, float limbAngle, float limbDistance, float progress, float headYaw, float headPitch) {
			float flap = MathHelper.sin(progress * 0.2f) * (0.12f + limbDistance * 0.35f);
			leftWing.roll = flap;
			rightWing.roll = -flap;
			tail.yaw = MathHelper.sin(progress * 0.1f) * 0.2f;
		}
	}

	// --------------------------------------------------------------------------------------------- piranha
	public static class Piranha extends Base<PiranhaEntity> {
		private final ModelPart jaw, tail;

		public Piranha(ModelPart root) {
			super(root);
			jaw = root.getChild("jaw");
			tail = root.getChild("tail");
		}

		public static TexturedModelData data() {
			ModelData data = new ModelData();
			ModelPartData r = data.getRoot();
			r.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-1f, -2.5f, -2.5f, 2, 4, 5), ModelTransform.pivot(0, 22, 0));
			r.addChild("jaw", ModelPartBuilder.create().uv(0, 9).cuboid(-1f, 0f, -2f, 2, 1, 2), ModelTransform.pivot(0, 22.5f, -2.5f));
			r.addChild("tail", ModelPartBuilder.create().uv(14, 0).cuboid(0f, -2f, 0f, 0, 4, 3), ModelTransform.pivot(0, 21.5f, 2.5f));
			r.addChild("dorsal", ModelPartBuilder.create().uv(14, 7).cuboid(0f, -1.5f, -1f, 0, 2, 3), ModelTransform.pivot(0, 19.5f, 0));
			return TexturedModelData.of(data, 32, 32);
		}

		@Override
		public void setAngles(PiranhaEntity e, float limbAngle, float limbDistance, float progress, float headYaw, float headPitch) {
			tail.yaw = -wiggle(e, progress, 1.1f) * 1.5f;
			jaw.pitch = e.isAttacking() ? 0.35f + MathHelper.sin(progress * 0.9f) * 0.3f : 0.05f;
		}
	}

	// --------------------------------------------------------------------------------------------- barracuda
	public static class Barracuda extends Base<ReefBarracudaEntity> {
		private final ModelPart jaw, tail;

		public Barracuda(ModelPart root) {
			super(root);
			jaw = root.getChild("jaw");
			tail = root.getChild("tail");
		}

		public static TexturedModelData data() {
			ModelData data = new ModelData();
			ModelPartData r = data.getRoot();
			r.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-1.5f, -2f, -7f, 3, 3, 14), ModelTransform.pivot(0, 22, 0));
			r.addChild("head", ModelPartBuilder.create().uv(0, 17).cuboid(-1f, -1.5f, -4f, 2, 2, 4), ModelTransform.pivot(0, 22, -7));
			r.addChild("jaw", ModelPartBuilder.create().uv(12, 17).cuboid(-1f, 0f, -4.5f, 2, 1, 4), ModelTransform.pivot(0, 22.3f, -7));
			r.addChild("tail", ModelPartBuilder.create().uv(34, 0).cuboid(0f, -3f, 0f, 0, 5, 5), ModelTransform.pivot(0, 21.5f, 7));
			r.addChild("dorsal", ModelPartBuilder.create().uv(34, 10).cuboid(0f, -2f, -2f, 0, 2, 4), ModelTransform.pivot(0, 20, -1));
			return TexturedModelData.of(data, 64, 32);
		}

		@Override
		public void setAngles(ReefBarracudaEntity e, float limbAngle, float limbDistance, float progress, float headYaw, float headPitch) {
			tail.yaw = -wiggle(e, progress, 0.5f) * 1.2f;
			jaw.pitch = e.isAttacking() ? 0.4f : 0.05f;
		}
	}

	// --------------------------------------------------------------------------------------------- deep angler
	public static class Angler extends Base<DeepAnglerEntity> {
		private final ModelPart jaw, tail, stalk;

		public Angler(ModelPart root) {
			super(root);
			jaw = root.getChild("jaw");
			tail = root.getChild("tail");
			stalk = root.getChild("stalk");
		}

		public static TexturedModelData data() {
			ModelData data = new ModelData();
			ModelPartData r = data.getRoot();
			r.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-5f, -5f, -6f, 10, 8, 12), ModelTransform.pivot(0, 21, 0));
			r.addChild("jaw", ModelPartBuilder.create().uv(0, 20).cuboid(-4.5f, 0f, -7.5f, 9, 2, 9), ModelTransform.pivot(0, 22, 0));
			ModelPartData stalk = r.addChild("stalk", ModelPartBuilder.create().uv(44, 0).cuboid(-0.5f, -6f, -0.5f, 1, 6, 1),
				ModelTransform.of(0, 16, -4, -0.55f, 0, 0));
			stalk.addChild("lure", ModelPartBuilder.create().uv(48, 0).cuboid(-1f, -8f, -1f, 2, 2, 2), ModelTransform.NONE);
			r.addChild("tail", ModelPartBuilder.create().uv(0, 32).cuboid(0f, -3f, 0f, 0, 6, 6), ModelTransform.pivot(0, 19, 6));
			return TexturedModelData.of(data, 64, 64);
		}

		@Override
		public void setAngles(DeepAnglerEntity e, float limbAngle, float limbDistance, float progress, float headYaw, float headPitch) {
			tail.yaw = -wiggle(e, progress, 0.3f);
			stalk.pitch = -0.55f + MathHelper.sin(progress * 0.05f) * 0.12f;
			stalk.roll = MathHelper.cos(progress * 0.04f) * 0.1f;
			jaw.pitch = e.isAttacking() ? 0.5f : 0.08f + MathHelper.sin(progress * 0.03f) * 0.05f;
		}
	}
}
