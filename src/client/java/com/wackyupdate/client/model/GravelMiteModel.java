package com.wackyupdate.client.model;

import com.wackyupdate.entity.GravelMiteEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

public class GravelMiteModel extends SinglePartEntityModel<GravelMiteEntity> {
	private final ModelPart root;
	private final ModelPart[] legs = new ModelPart[6];

	public GravelMiteModel(ModelPart root) {
		this.root = root;
		for (int i = 0; i < 6; i++) legs[i] = root.getChild("leg" + i);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-2.5f, -2f, -3f, 5, 3, 6), ModelTransform.pivot(0, 22, 0));
		root.addChild("head", ModelPartBuilder.create().uv(0, 9).cuboid(-1.5f, -1f, -2f, 3, 2, 2), ModelTransform.pivot(0, 21.5f, -3f));
		for (int i = 0; i < 6; i++) {
			boolean right = i % 2 == 0;
			float z = -2 + (i / 2) * 2;
			ModelPartBuilder leg = ModelPartBuilder.create().uv(10, 9).cuboid(right ? 0f : -3f, 0f, -0.5f, 3, 1, 1);
			root.addChild("leg" + i, leg, ModelTransform.of(right ? 2.2f : -2.2f, 21.5f, z, 0, 0, right ? 0.6f : -0.6f));
		}
		return TexturedModelData.of(data, 32, 32);
	}

	@Override
	public ModelPart getPart() {
		return root;
	}

	@Override
	public void setAngles(GravelMiteEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		for (int i = 0; i < 6; i++) {
			float phase = (i % 2 == 0 ? 0 : MathHelper.PI) + (i / 2) * 2.1f;
			legs[i].yaw = MathHelper.cos(limbAngle * 2.4f + phase) * 0.7f * limbDistance;
			float baseRoll = i % 2 == 0 ? 0.6f : -0.6f;
			legs[i].roll = baseRoll + MathHelper.sin(limbAngle * 2.4f + phase) * 0.25f * limbDistance * Math.signum(baseRoll);
		}
	}
}
