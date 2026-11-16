package com.wackyupdate.client.model;

import com.wackyupdate.entity.GlowtoadEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

public class GlowtoadModel extends SinglePartEntityModel<GlowtoadEntity> {
	private final ModelPart root, head, throat;
	private final ModelPart[] legs = new ModelPart[4];

	public GlowtoadModel(ModelPart root) {
		this.root = root;
		head = root.getChild("head");
		throat = head.getChild("throat");
		legs[0] = root.getChild("front_right_leg");
		legs[1] = root.getChild("front_left_leg");
		legs[2] = root.getChild("back_right_leg");
		legs[3] = root.getChild("back_left_leg");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-3.5f, -4f, -4.5f, 7, 4, 9), ModelTransform.pivot(0, 22, 0));
		ModelPartData head = root.addChild("head", ModelPartBuilder.create().uv(0, 13).cuboid(-3f, -2.5f, -4f, 6, 3, 4)
			.uv(20, 13).cuboid(-3f, -4f, -3f, 2, 2, 2)
			.uv(20, 13).cuboid(1f, -4f, -3f, 2, 2, 2), ModelTransform.pivot(0, 19.5f, -4f));
		head.addChild("throat", ModelPartBuilder.create().uv(32, 0).cuboid(-2f, 0f, -3.5f, 4, 2, 3), ModelTransform.pivot(0, 0.5f, 0));
		root.addChild("front_right_leg", ModelPartBuilder.create().uv(0, 22).cuboid(-1f, 0f, -1f, 2, 3, 2), ModelTransform.pivot(-2.5f, 21, -3));
		root.addChild("front_left_leg", ModelPartBuilder.create().uv(0, 22).cuboid(-1f, 0f, -1f, 2, 3, 2), ModelTransform.pivot(2.5f, 21, -3));
		root.addChild("back_right_leg", ModelPartBuilder.create().uv(10, 22).cuboid(-1.5f, 0f, -2f, 3, 2, 5), ModelTransform.pivot(-3.5f, 22, 2.5f));
		root.addChild("back_left_leg", ModelPartBuilder.create().uv(10, 22).cuboid(-1.5f, 0f, -2f, 3, 2, 5), ModelTransform.pivot(3.5f, 22, 2.5f));
		return TexturedModelData.of(data, 64, 32);
	}

	@Override
	public ModelPart getPart() {
		return root;
	}

	@Override
	public void setAngles(GlowtoadEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		head.yaw = headYaw * MathHelper.RADIANS_PER_DEGREE * 0.4f;
		float puff = 1f + Math.max(0f, MathHelper.sin(animationProgress * 0.12f)) * 0.35f;
		throat.xScale = puff;
		throat.yScale = puff;
		legs[0].pitch = MathHelper.cos(limbAngle * 1.2f) * 0.8f * limbDistance;
		legs[1].pitch = MathHelper.cos(limbAngle * 1.2f + MathHelper.PI) * 0.8f * limbDistance;
		boolean airborne = !entity.isOnGround();
		legs[2].pitch = airborne ? 0.9f : MathHelper.cos(limbAngle * 1.2f + MathHelper.PI) * 0.6f * limbDistance;
		legs[3].pitch = airborne ? 0.9f : MathHelper.cos(limbAngle * 1.2f) * 0.6f * limbDistance;
	}
}
