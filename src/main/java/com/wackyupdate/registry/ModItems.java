package com.wackyupdate.registry;

import com.wackyupdate.WackyUpdate;
import com.wackyupdate.item.*;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public final class ModItems {
	// ---- speculum
	public static final Item SPECULUM_SHARD = register("speculum_shard", new Item(new Item.Settings()));
	public static final Item SPECTRE_LENS = register("spectre_lens", new SpectreLensItem(new Item.Settings().maxDamage(128).rarity(Rarity.UNCOMMON)));
	public static final Item SPECULUM_HELMET = register("speculum_helmet", new SpeculumArmorItem(ArmorItem.Type.HELMET));
	public static final Item SPECULUM_CHESTPLATE = register("speculum_chestplate", new SpeculumArmorItem(ArmorItem.Type.CHESTPLATE));
	public static final Item SPECULUM_LEGGINGS = register("speculum_leggings", new SpeculumArmorItem(ArmorItem.Type.LEGGINGS));
	public static final Item SPECULUM_BOOTS = register("speculum_boots", new SpeculumArmorItem(ArmorItem.Type.BOOTS));

	// ---- mites & skating
	public static final Item MITE_FOOT = register("mite_foot", new MiteFootItem(new Item.Settings()));
	public static final Item ROLLERSKATES = register("rollerskates", new RollerSkatesItem(new Item.Settings()
		.maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(12)).rarity(Rarity.UNCOMMON)));

	// ---- misc
	public static final Item PRISM_TONIC = register("prism_tonic", new PrismTonicItem(new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON)));
	public static final Item RAGDOLL_DUMMY = register("ragdoll_dummy", new RagdollDummyItem(new Item.Settings().maxCount(16)));

	// ---- spawn eggs
	public static final Item GRAVEL_MITE_SPAWN_EGG = register("gravel_mite_spawn_egg", new SpawnEggItem(ModEntities.GRAVEL_MITE, 0x8a8380, 0x4e4744, new Item.Settings()));
	public static final Item PRISM_SNAIL_SPAWN_EGG = register("prism_snail_spawn_egg", new SpawnEggItem(ModEntities.PRISM_SNAIL, 0xc9b28f, 0x7ad7f0, new Item.Settings()));
	public static final Item ROLLYPIN_SPAWN_EGG = register("rollypin_spawn_egg", new SpawnEggItem(ModEntities.ROLLYPIN, 0x8b5a2b, 0xd9a066, new Item.Settings()));
	public static final Item GLOWTOAD_SPAWN_EGG = register("glowtoad_spawn_egg", new SpawnEggItem(ModEntities.GLOWTOAD, 0x4f7a3a, 0xf7e26b, new Item.Settings()));
	public static final Item SHARDLING_SPAWN_EGG = register("shardling_spawn_egg", new SpawnEggItem(ModEntities.SHARDLING, 0x3fc7d6, 0xd4f7ff, new Item.Settings()));
	public static final Item SKATE_GHOUL_SPAWN_EGG = register("skate_ghoul_spawn_egg", new SpawnEggItem(ModEntities.SKATE_GHOUL, 0x5d7a5a, 0xe04a8c, new Item.Settings()));

	static {
		// Block items
		for (Block block : new Block[]{
			ModBlocks.BUDDING_SPECULUM, ModBlocks.SPECULUM_CLUSTER, ModBlocks.LARGE_SPECULUM_BUD, ModBlocks.MEDIUM_SPECULUM_BUD,
			ModBlocks.SMALL_SPECULUM_BUD, ModBlocks.SPECULUM_LANTERN, ModBlocks.MITE_GRAVEL, ModBlocks.COMPACTED_FEET_BLOCK,
			ModBlocks.SLICKWOOD_LOG, ModBlocks.STRIPPED_SLICKWOOD_LOG, ModBlocks.SLICKWOOD_PLANKS, ModBlocks.SLICKWOOD_LEAVES,
			ModBlocks.SLICKWOOD_SAPLING, ModBlocks.SLICKWOOD_RAMP, ModBlocks.SLICKWOOD_FLOOR, ModBlocks.SPECULUM_BLOCK,
			ModBlocks.POLISHED_SPECULUM, ModBlocks.SPECULUM_BRICKS, ModBlocks.PRISM_TILES, ModBlocks.FIREFLY_JAR,
			ModBlocks.SKATE_RAIL, ModBlocks.BOOST_PAD, ModBlocks.SLICKWOOD_SLAB, ModBlocks.SLICKWOOD_STAIRS,
			ModBlocks.SLICKWOOD_FENCE, ModBlocks.PRISM_PROJECTOR}) {
			Registry.register(Registries.ITEM, Registries.BLOCK.getId(block), new BlockItem(block, new Item.Settings()));
		}
		Registry.register(Registries.ITEM, Registries.BLOCK.getId(ModBlocks.PRISM_GLASS), new PrismGlassItem(ModBlocks.PRISM_GLASS, new Item.Settings()));
	}

	private static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, WackyUpdate.id(name), item);
	}

	public static void init() {}

	private ModItems() {}
}
