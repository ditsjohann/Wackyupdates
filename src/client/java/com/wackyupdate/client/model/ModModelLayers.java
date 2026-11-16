package com.wackyupdate.client.model;

import com.wackyupdate.WackyUpdate;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public final class ModModelLayers {
	public static final EntityModelLayer GRAVEL_MITE = layer("gravel_mite");
	public static final EntityModelLayer PRISM_SNAIL = layer("prism_snail");
	public static final EntityModelLayer ROLLYPIN = layer("rollypin");
	public static final EntityModelLayer GLOWTOAD = layer("glowtoad");
	public static final EntityModelLayer SHARDLING = layer("shardling");
	public static final EntityModelLayer RAGDOLL_WIDE = layer("ragdoll_wide");
	public static final EntityModelLayer RAGDOLL_SKINNY = layer("ragdoll_skinny");

	private static EntityModelLayer layer(String name) {
		return new EntityModelLayer(WackyUpdate.id(name), "main");
	}

	private ModModelLayers() {}
}
