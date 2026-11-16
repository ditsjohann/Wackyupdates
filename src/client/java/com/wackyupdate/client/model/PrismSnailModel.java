package com.wackyupdate.client.model;

import com.wackyupdate.entity.PrismSnailEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/** The shell is rendered separately, tinted with the snail's shell colour. */
public class PrismSnailModel extends EntityModel<PrismSnailEntity> {
	private final ModelPart foot;
	private final ModelPart leftEye;
	private final ModelPart rightEye;
	private final ModelPart shell;
	private int shellColor = 0xFFFFFFFF;

	public PrismSnailModel(ModelPart root) {
		foot = root.getChild("foot");
		leftEye = root.getChild("left_eye");
		rightEye = root.getChild("right_eye");
		shell = root.getChild("shell");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("foot", ModelPartBuilder.create().uv(0, 0).cuboid(-2f, -2f, -6f, 4, 2, 12), ModelTransform.pivot(0, 24, 0));
		root.addChild("left_eye", ModelPartBuilder.create().uv(0, 14).cuboid(-0.5f, -4f, -0.5f, 1, 4, 1), ModelTransform.pivot(1.2f, 22.5f, -5f));
		root.addChild("right_eye", ModelPartBuilder.create().uv(0, 14).cuboid(-0.5f, -4f, -0.5f, 1, 4, 1), ModelTransform.pivot(-1.2f, 22.5f, -5f));
		root.addChild("shell", ModelPartBuilder.create().uv(0, 16).cuboid(-3f, -7f, -3.5f, 6, 7, 7), ModelTransform.pivot(0, 22.5f, 1.5f));
		return TexturedModelData.of(data, 64, 32);
	}

	@Override
	public void setAngles(PrismSnailEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		shellColor = 0xFF000000 | entity.getShellColor().getEntityColor();
		leftEye.pitch = -0.25f + MathHelper.sin(animationProgress * 0.1f) * 0.15f;
		rightEye.pitch = -0.25f + MathHelper.sin(animationProgress * 0.1f + 1.5f) * 0.15f;
		leftEye.roll = 0.2f;
		rightEye.roll = -0.2f;
		shell.pivotY = 22.5f + MathHelper.sin(limbAngle * 0.8f) * 0.4f * limbDistance;
		foot.zScale = 1f + MathHelper.sin(limbAngle * 0.8f) * 0.08f * limbDistance;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
		foot.render(matrices, vertices, light, overlay, color);
		leftEye.render(matrices, vertices, light, overlay, color);
		rightEye.render(matrices, vertices, light, overlay, color);
		shell.render(matrices, vertices, light, overlay, multiply(color, shellColor));
	}

	private static int multiply(int a, int b) {
		int alpha = ((a >>> 24) * (b >>> 24)) / 255;
		int r = (((a >> 16) & 0xFF) * ((b >> 16) & 0xFF)) / 255;
		int g = (((a >> 8) & 0xFF) * ((b >> 8) & 0xFF)) / 255;
		int bl = ((a & 0xFF) * (b & 0xFF)) / 255;
		return alpha << 24 | r << 16 | g << 8 | bl;
	}
}
