package com.wackyupdate.registry;

import com.wackyupdate.WackyUpdate;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class ModTags {
	/** Blocks that rollerskates go fast on. */
	public static final TagKey<Block> SKATEABLE = TagKey.of(RegistryKeys.BLOCK, WackyUpdate.id("skateable"));
	/** Blocks that act as a crystal lens in front of a Prism Projector and give a rainbow beam. */
	public static final TagKey<Block> RAINBOW_LENSES = TagKey.of(RegistryKeys.BLOCK, WackyUpdate.id("rainbow_lenses"));
	/** Mobs that turn into a ragdoll when they die. */
	public static final TagKey<EntityType<?>> RAGDOLLS = TagKey.of(RegistryKeys.ENTITY_TYPE, WackyUpdate.id("ragdolls"));

	/** Sea creatures that small fish recognise as danger and flee from. */
	public static final TagKey<EntityType<?>> AQUATIC_PREDATORS = TagKey.of(RegistryKeys.ENTITY_TYPE, WackyUpdate.id("aquatic_predators"));

	private ModTags() {}
}
