package com.wackyupdate.registry;

import com.wackyupdate.WackyUpdate;
import com.wackyupdate.block.entity.LightReceptorBlockEntity;
import com.wackyupdate.block.entity.PrismProjectorBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlockEntities {
	public static final BlockEntityType<PrismProjectorBlockEntity> PRISM_PROJECTOR = Registry.register(Registries.BLOCK_ENTITY_TYPE,
		WackyUpdate.id("prism_projector"),
		BlockEntityType.Builder.create(PrismProjectorBlockEntity::new, ModBlocks.PRISM_PROJECTOR).build(null));

	public static final BlockEntityType<LightReceptorBlockEntity> LIGHT_RECEPTOR = Registry.register(Registries.BLOCK_ENTITY_TYPE,
		WackyUpdate.id("light_receptor"),
		BlockEntityType.Builder.create(LightReceptorBlockEntity::new, ModBlocks.LIGHT_RECEPTOR).build(null));

	public static void init() {}

	private ModBlockEntities() {}
}
