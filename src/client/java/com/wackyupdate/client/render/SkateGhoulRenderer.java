package com.wackyupdate.client.render;

import com.wackyupdate.WackyUpdate;
import com.wackyupdate.entity.SkateGhoulEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public class SkateGhoulRenderer extends BipedEntityRenderer<SkateGhoulEntity, BipedEntityModel<SkateGhoulEntity>> {
	private static final Identifier TEXTURE = WackyUpdate.id("textures/entity/skate_ghoul.png");

	public SkateGhoulRenderer(EntityRendererFactory.Context ctx) {
		super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
		addFeature(new ArmorFeatureRenderer<>(this,
			new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_INNER_ARMOR)),
			new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_OUTER_ARMOR)),
			ctx.getModelManager()));
		addFeature(new GlowLayer<>(this, WackyUpdate.id("textures/entity/skate_ghoul_glow.png")));
	}

	@Override
	public Identifier getTexture(SkateGhoulEntity entity) {
		return TEXTURE;
	}
}
