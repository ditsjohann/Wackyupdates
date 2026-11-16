package com.wackyupdate.client.model;

import com.wackyupdate.entity.RollypinEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

public class RollypinModel extends SinglePartEntityModel<RollypinEntity> {
	private final ModelPart root, body, head, tail, ball;
	private final ModelPart[] legs = new ModelPart[4];

	public RollypinModel(ModelPart root) {
		this.root = root;
		body = root.getChild("body");
		head = root.getChild("head");
		tail = root.getChild("tail");
		ball = root.getChild("ball");
		for (int i = 0; i < 4; i++) legs[i] = root.getChild("leg" + i);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-3.5f, -3f, -6f, 7, 6, 12), ModelTransform.pivot(0, 17, 0));
		root.addChild("head", ModelPartBuilder.create().uv(0, 20).cuboid(-2.5f, -2f, -5f, 5, 4, 5)
			.uv(22, 20).cuboid(-1f, 0f, -7f, 2, 2, 2), ModelTransform.pivot(0, 17.5f, -6f));
		root.addChild("tail", ModelPartBuilder.create().uv(0, 30).cuboid(-2f, -1.5f, 0f, 4, 3, 9), ModelTransform.of(0, 16.5f, 5.5f, -0.6f, 0, 0));
		root.addChild("ball", ModelPartBuilder.create().uv(0, 44).cuboid(-4f, -4f, -4f, 8, 8, 8), ModelTransform.pivot(0, 20, 0));
		float[][] pos = {{2.5f, -4f}, {-2.5f, -4f}, {2.5f, 4f}, {-2.5f, 4f}};
		for (int i = 0; i < 4; i++) {
			root.addChild("leg" + i, ModelPartBuilder.create().uv(40, 20).cuboid(-1f, 0f, -1f, 2, 4, 2), ModelTransform.pivot(pos[i][0], 20, pos[i][1]));
		}
		return TexturedModelData.of(data, 64, 64);
	}

	@Override
	public ModelPart getPart() {
		return root;
	}

	@Override
	public void setAngles(RollypinEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		boolean rolled = entity.isRolled();
		ball.visible = rolled;
		body.visible = head.visible = tail.visible = !rolled;
		for (ModelPart leg : legs) leg.visible = !rolled;
		if (rolled) {
			ball.pitch = limbAngle * 1.6f;
			return;
		}
		head.yaw = headYaw * MathHelper.RADIANS_PER_DEGREE * 0.6f;
		head.pitch = headPitch * MathHelper.RADIANS_PER_DEGREE * 0.5f;
		tail.yaw = MathHelper.sin(animationProgress * 0.15f) * 0.15f + MathHelper.cos(limbAngle * 0.6f) * 0.2f * limbDistance;
		for (int i = 0; i < 4; i++) {
			float phase = (i == 0 || i == 3) ? 0 : MathHelper.PI;
			legs[i].pitch = MathHelper.cos(limbAngle * 0.9f + phase) * 1.2f * limbDistance;
		}
	}
}
