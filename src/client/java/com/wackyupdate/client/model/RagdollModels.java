package com.wackyupdate.client.model;

import net.minecraft.client.model.*;

/**
 * Biped body parts laid out around the torso centre, using the standard 64x64 (zombie) or
 * 64x32 (skeleton) biped texture layouts so we can reuse the dead mob's own texture.
 */
public final class RagdollModels {
	public static TexturedModelData wide() {
		return create(4, 64, 64);
	}

	public static TexturedModelData skinny() {
		return create(2, 64, 32);
	}

	private static TexturedModelData create(int limb, int texW, int texH) {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		float h = limb / 2f;
		root.addChild("body", ModelPartBuilder.create().uv(16, 16).cuboid(-4f, -6f, -2f, 8, 12, 4), ModelTransform.NONE);
		root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4f, -8f, -4f, 8, 8, 8), ModelTransform.pivot(0, -6f, 0));
		root.addChild("right_arm", ModelPartBuilder.create().uv(40, 16).cuboid(limb == 4 ? -3f : -1f, -2f, -h, limb, 12, limb),
			ModelTransform.pivot(-5f, -4f, 0));
		root.addChild("left_arm", ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1f, -2f, -h, limb, 12, limb),
			ModelTransform.pivot(5f, -4f, 0));
		root.addChild("right_leg", ModelPartBuilder.create().uv(0, 16).cuboid(-h, 0f, -h, limb, 12, limb), ModelTransform.pivot(-1.9f, 6f, 0));
		root.addChild("left_leg", ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-h, 0f, -h, limb, 12, limb), ModelTransform.pivot(1.9f, 6f, 0));
		return TexturedModelData.of(data, texW, texH);
	}

	private RagdollModels() {}
}
