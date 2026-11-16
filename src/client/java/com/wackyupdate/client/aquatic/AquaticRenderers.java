package com.wackyupdate.client.aquatic;

import com.wackyupdate.WackyUpdate;
import com.wackyupdate.client.render.GlowLayer;
import com.wackyupdate.client.render.SimpleMobRenderer;
import com.wackyupdate.entity.aquatic.DriftJellyEntity;
import com.wackyupdate.entity.aquatic.GlimmerfinEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

public final class AquaticRenderers {
	private AquaticRenderers() {}

	public static Identifier tex(String name) {
		return WackyUpdate.id("textures/entity/" + name + ".png");
	}

	public static EntityModelLayer layer(String name) {
		return new EntityModelLayer(WackyUpdate.id(name), "main");
	}

	/** Fish renderer: swims with a body wiggle and flops on its side out of water (like vanilla cod). */
	public static class FishRenderer<T extends MobEntity, M extends EntityModel<T>> extends SimpleMobRenderer<T, M> {
		public FishRenderer(EntityRendererFactory.Context ctx, M model, float shadow, Identifier texture) {
			super(ctx, model, shadow, texture);
		}

		@Override
		protected void setupTransforms(T entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta, float scale) {
			super.setupTransforms(entity, matrices, animationProgress, bodyYaw, tickDelta, scale);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(4.3f * MathHelper.sin(0.6f * animationProgress)));
			if (!entity.isTouchingWater()) {
				matrices.translate(0.1f, 0.1f, -0.1f);
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
			}
		}
	}

	/** Glimmerfin: picks its colour variant texture and glow overlay. */
	public static class GlimmerfinRenderer extends FishRenderer<GlimmerfinEntity, AquaticModels.Glimmerfin> {
		private static final Identifier[] TEXTURES = new Identifier[GlimmerfinEntity.VARIANTS];
		private static final RenderLayer[] GLOW = new RenderLayer[GlimmerfinEntity.VARIANTS];

		static {
			for (int i = 0; i < GlimmerfinEntity.VARIANTS; i++) {
				TEXTURES[i] = tex("glimmerfin_" + i);
				GLOW[i] = RenderLayer.getEyes(tex("glimmerfin_glow_" + i));
			}
		}

		public GlimmerfinRenderer(EntityRendererFactory.Context ctx) {
			super(ctx, new AquaticModels.Glimmerfin(ctx.getPart(layer("glimmerfin"))), 0.2f, TEXTURES[0]);
			addFeature(new FeatureRenderer<>(this) {
				@Override
				public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, GlimmerfinEntity entity,
						float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
					getContextModel().render(matrices, consumers.getBuffer(GLOW[entity.getVariant()]), 0xF00000, OverlayTexture.DEFAULT_UV);
				}
			});
		}

		@Override
		public Identifier getTexture(GlimmerfinEntity entity) {
			return TEXTURES[Math.floorMod(entity.getVariant(), TEXTURES.length)];
		}
	}

	/** Drift jelly: see-through body plus a glowing overlay. */
	public static class DriftJellyRenderer extends SimpleMobRenderer<DriftJellyEntity, AquaticModels.DriftJelly> {
		private static final Identifier TEXTURE = tex("drift_jelly");

		public DriftJellyRenderer(EntityRendererFactory.Context ctx) {
			super(ctx, new AquaticModels.DriftJelly(ctx.getPart(layer("drift_jelly"))), 0.3f, TEXTURE);
			addFeature(new GlowLayer<>(this, tex("drift_jelly_glow")));
		}

		@Nullable
		@Override
		protected RenderLayer getRenderLayer(DriftJellyEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
			return RenderLayer.getEntityTranslucent(TEXTURE);
		}
	}
}
