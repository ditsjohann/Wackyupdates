package com.wackyupdate.client.model;

import com.wackyupdate.entity.ShardlingEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

public class ShardlingModel extends SinglePartEntityModel<ShardlingEntity> {
	private final ModelPart root, head, rightArm, leftArm, rightLeg, leftLeg;

	public ShardlingModel(ModelPart root) {
		this.root = root;
		head = root.getChild("head");
		rightArm = root.getChild("right_arm");
		leftArm = root.getChild("left_arm");
		rightLeg = root.getChild("right_leg");
		leftLeg = root.getChild("left_leg");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		ModelPartData body = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-4f, 0f, -3f, 8, 9, 6), ModelTransform.pivot(0, 6, 0));
		body.addChild("spike_left", ModelPartBuilder.create().uv(30, 0).cuboid(-1f, -6f, -1f, 2, 6, 2), ModelTransform.of(-2f, 2f, 2.5f, -0.5f, 0, -0.35f));
		body.addChild("spike_right", ModelPartBuilder.create().uv(30, 0).cuboid(-1f, -6f, -1f, 2, 6, 2), ModelTransform.of(2f, 3f, 2.5f, -0.4f, 0, 0.4f));
		ModelPartData head = root.addChild("head", ModelPartBuilder.create().uv(0, 16).cuboid(-3f, -6f, -3f, 6, 6, 6), ModelTransform.pivot(0, 6, 0));
		head.addChild("crown", ModelPartBuilder.create().uv(30, 10).cuboid(-1f, -4f, -1f, 2, 4, 2), ModelTransform.of(0, -6f, 0, 0.15f, 0, 0.2f));
		root.addChild("right_arm", ModelPartBuilder.create().uv(40, 0).cuboid(-1.5f, -1f, -1.5f, 3, 11, 3), ModelTransform.pivot(-5.5f, 7, 0));
		root.addChild("left_arm", ModelPartBuilder.create().uv(40, 0).mirrored().cuboid(-1.5f, -1f, -1.5f, 3, 11, 3), ModelTransform.pivot(5.5f, 7, 0));
		root.addChild("right_leg", ModelPartBuilder.create().uv(40, 16).cuboid(-1.5f, 0f, -1.5f, 3, 9, 3), ModelTransform.pivot(-2f, 15, 0));
		root.addChild("left_leg", ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1.5f, 0f, -1.5f, 3, 9, 3), ModelTransform.pivot(2f, 15, 0));
		return TexturedModelData.of(data, 64, 64);
	}

	@Override
	public ModelPart getPart() {
		return root;
	}

	@Override
	public void setAngles(ShardlingEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		head.yaw = headYaw * MathHelper.RADIANS_PER_DEGREE;
		head.pitch = headPitch * MathHelper.RADIANS_PER_DEGREE;
		rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.3f * limbDistance;
		leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + MathHelper.PI) * 1.3f * limbDistance;
		float swing = handSwingProgress;
		rightArm.pitch = MathHelper.cos(limbAngle * 0.6662f + MathHelper.PI) * limbDistance - MathHelper.sin(swing * MathHelper.PI) * 1.6f;
		leftArm.pitch = MathHelper.cos(limbAngle * 0.6662f) * limbDistance - MathHelper.sin(swing * MathHelper.PI) * 1.6f;
		rightArm.roll = 0.1f + MathHelper.sin(animationProgress * 0.07f) * 0.05f;
		leftArm.roll = -0.1f - MathHelper.sin(animationProgress * 0.07f) * 0.05f;
	}
}
