package com.wackyupdate.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/** A mob renderer with a fixed texture and optional scale for babies. */
public class SimpleMobRenderer<T extends MobEntity, M extends EntityModel<T>> extends MobEntityRenderer<T, M> {
	private final Identifier texture;

	public SimpleMobRenderer(EntityRendererFactory.Context ctx, M model, float shadow, Identifier texture) {
		super(ctx, model, shadow);
		this.texture = texture;
	}

	/** Adds a full-bright glow overlay texture. */
	public SimpleMobRenderer<T, M> withGlow(Identifier glowTexture) {
		addFeature(new GlowLayer<>(this, glowTexture));
		return this;
	}

	@Override
	protected void scale(T entity, MatrixStack matrices, float amount) {
		if (entity.isBaby()) matrices.scale(0.5f, 0.5f, 0.5f);
	}

	@Override
	public Identifier getTexture(T entity) {
		return texture;
	}
}
