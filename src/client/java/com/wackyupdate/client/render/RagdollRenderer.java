package com.wackyupdate.client.render;

import com.wackyupdate.WackyUpdate;
import com.wackyupdate.client.model.ModModelLayers;
import com.wackyupdate.entity.RagdollEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.Map;

public class RagdollRenderer extends EntityRenderer<RagdollEntity> {
	private static final Identifier DUMMY_TEXTURE = WackyUpdate.id("textures/entity/ragdoll_dummy.png");
	private static final Map<String, Identifier> TEXTURES = Map.of(
		"minecraft:zombie", Identifier.ofVanilla("textures/entity/zombie/zombie.png"),
		"minecraft:husk", Identifier.ofVanilla("textures/entity/zombie/husk.png"),
		"minecraft:drowned", Identifier.ofVanilla("textures/entity/zombie/drowned.png"),
		"minecraft:skeleton", Identifier.ofVanilla("textures/entity/skeleton/skeleton.png"),
		"minecraft:stray", Identifier.ofVanilla("textures/entity/skeleton/stray.png"),
		"minecraft:wither_skeleton", Identifier.ofVanilla("textures/entity/skeleton/wither_skeleton.png"),
		"minecraft:bogged", Identifier.ofVanilla("textures/entity/skeleton/bogged.png"),
		"wackyupdate:skate_ghoul", WackyUpdate.id("textures/entity/skate_ghoul.png"),
		RagdollEntity.DUMMY_SOURCE, DUMMY_TEXTURE);

	private final Parts wide;
	private final Parts skinny;

	private record Parts(ModelPart root, ModelPart head, ModelPart rightArm, ModelPart leftArm, ModelPart rightLeg, ModelPart leftLeg) {
		static Parts of(ModelPart root) {
			return new Parts(root, root.getChild("head"), root.getChild("right_arm"), root.getChild("left_arm"),
				root.getChild("right_leg"), root.getChild("left_leg"));
		}
	}

	public RagdollRenderer(EntityRendererFactory.Context ctx) {
		super(ctx);
		this.shadowRadius = 0.4f;
		wide = Parts.of(ctx.getPart(ModModelLayers.RAGDOLL_WIDE));
		skinny = Parts.of(ctx.getPart(ModModelLayers.RAGDOLL_SKINNY));
	}

	@Override
	public void render(RagdollEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		Parts parts = entity.isSkinny() ? skinny : wide;
		float pitch = MathHelper.lerp(tickDelta, entity.prevBodyPitch, entity.bodyPitch);
		float roll = MathHelper.lerp(tickDelta, entity.prevBodyRoll, entity.bodyRoll);
		float upright = Math.abs(MathHelper.cos(pitch * MathHelper.RADIANS_PER_DEGREE)) * Math.abs(MathHelper.cos(roll * MathHelper.RADIANS_PER_DEGREE));
		float centerHeight = 0.16f + upright * 0.97f;

		matrices.push();
		matrices.translate(0, centerHeight, 0);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f - entity.getYaw()));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
		matrices.scale(-1f, -1f, 1f);

		pose(parts.head, entity, RagdollEntity.HEAD, tickDelta);
		pose(parts.rightArm, entity, RagdollEntity.RIGHT_ARM, tickDelta);
		pose(parts.leftArm, entity, RagdollEntity.LEFT_ARM, tickDelta);
		pose(parts.rightLeg, entity, RagdollEntity.RIGHT_LEG, tickDelta);
		pose(parts.leftLeg, entity, RagdollEntity.LEFT_LEG, tickDelta);

		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(getTexture(entity)));
		parts.root.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV);
		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}

	private static void pose(ModelPart part, RagdollEntity entity, int index, float tickDelta) {
		part.pitch = MathHelper.lerp(tickDelta, entity.prevLimbPitch[index], entity.limbPitch[index]) * MathHelper.RADIANS_PER_DEGREE;
		part.roll = MathHelper.lerp(tickDelta, entity.prevLimbRoll[index], entity.limbRoll[index]) * MathHelper.RADIANS_PER_DEGREE;
	}

	@Override
	public Identifier getTexture(RagdollEntity entity) {
		return TEXTURES.getOrDefault(entity.getSource(), DUMMY_TEXTURE);
	}
}
