package com.wackyupdate.registry;

import com.wackyupdate.WackyUpdate;
import com.wackyupdate.item.PrismGlassItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public final class ModItemGroups {
	public static final ItemGroup WACKY = Registry.register(Registries.ITEM_GROUP, WackyUpdate.id("wackyupdate"),
		FabricItemGroup.builder()
			.icon(() -> new ItemStack(ModBlocks.SPECULUM_CLUSTER))
			.displayName(Text.translatable("itemGroup.wackyupdate"))
			.entries((context, entries) -> {
				entries.add(ModItems.SPECULUM_SHARD);
				entries.add(ModBlocks.BUDDING_SPECULUM);
				entries.add(ModBlocks.SMALL_SPECULUM_BUD);
				entries.add(ModBlocks.MEDIUM_SPECULUM_BUD);
				entries.add(ModBlocks.LARGE_SPECULUM_BUD);
				entries.add(ModBlocks.SPECULUM_CLUSTER);
				for (DyeColor color : DyeColor.values()) {
					entries.add(PrismGlassItem.withColor(new ItemStack(ModBlocks.PRISM_GLASS), color));
				}
				entries.add(ModBlocks.PRISM_TILES);
				entries.add(ModBlocks.SPECULUM_BLOCK);
				entries.add(ModBlocks.POLISHED_SPECULUM);
				entries.add(ModBlocks.SPECULUM_BRICKS);
				entries.add(ModBlocks.SPECULUM_LANTERN);
				entries.add(ModBlocks.FIREFLY_JAR);
				entries.add(ModBlocks.PRISM_PROJECTOR);
				entries.add(ModBlocks.SPECULUM_MIRROR);
				entries.add(ModBlocks.PRISM_SPLITTER);
				entries.add(ModBlocks.LIGHT_RECEPTOR);
				entries.add(ModItems.SPECTRE_LENS);
				entries.add(ModItems.SPECULUM_HELMET);
				entries.add(ModItems.SPECULUM_CHESTPLATE);
				entries.add(ModItems.SPECULUM_LEGGINGS);
				entries.add(ModItems.SPECULUM_BOOTS);
				entries.add(ModBlocks.MITE_GRAVEL);
				entries.add(ModItems.MITE_FOOT);
				entries.add(ModBlocks.COMPACTED_FEET_BLOCK);
				entries.add(ModItems.ROLLERSKATES);
				entries.add(ModBlocks.SLICKWOOD_LOG);
				entries.add(ModBlocks.STRIPPED_SLICKWOOD_LOG);
				entries.add(ModBlocks.SLICKWOOD_PLANKS);
				entries.add(ModBlocks.SLICKWOOD_STAIRS);
				entries.add(ModBlocks.SLICKWOOD_SLAB);
				entries.add(ModBlocks.SLICKWOOD_FENCE);
				entries.add(ModBlocks.SLICKWOOD_LEAVES);
				entries.add(ModBlocks.SLICKWOOD_SAPLING);
				entries.add(ModBlocks.SLICKWOOD_RAMP);
				entries.add(ModBlocks.SLICKWOOD_FLOOR);
				entries.add(ModBlocks.SKATE_RAIL);
				entries.add(ModBlocks.BOOST_PAD);
				entries.add(ModItems.PRISM_TONIC);
				entries.add(ModItems.RAGDOLL_DUMMY);
				entries.add(ModItems.GRAVEL_MITE_SPAWN_EGG);
				entries.add(ModItems.PRISM_SNAIL_SPAWN_EGG);
				entries.add(ModItems.ROLLYPIN_SPAWN_EGG);
				entries.add(ModItems.GLOWTOAD_SPAWN_EGG);
				entries.add(ModItems.SHARDLING_SPAWN_EGG);
				entries.add(ModItems.SKATE_GHOUL_SPAWN_EGG);
			})
			.build());

	public static final ItemGroup AQUATIC = Registry.register(Registries.ITEM_GROUP, WackyUpdate.id("aquatic"),
		FabricItemGroup.builder()
			.icon(() -> new ItemStack(ModAquaticItems.GLIMMERFIN_BUCKET))
			.displayName(Text.translatable("itemGroup.wackyupdate.aquatic"))
			.entries((context, entries) -> {
				for (net.minecraft.item.Item item : new net.minecraft.item.Item[]{
					ModAquaticItems.GLIMMERFIN, ModAquaticItems.SUNSCALE_CARP, ModAquaticItems.COOKED_SUNSCALE_CARP,
					ModAquaticItems.PIRANHA, ModAquaticItems.COOKED_PIRANHA, ModAquaticItems.BARRACUDA_FILLET,
					ModAquaticItems.COOKED_BARRACUDA_FILLET, ModAquaticItems.JELLY_GEL, ModAquaticItems.STINGRAY_BARB,
					ModAquaticItems.PIRANHA_TOOTH, ModAquaticItems.ANGLER_LURE, ModAquaticItems.HERMIT_SHELL,
					ModAquaticBlocks.ANGLER_LANTERN.asItem(), ModAquaticBlocks.JELLY_LAMP.asItem(), ModAquaticBlocks.GLOW_ANEMONE.asItem(),
					ModAquaticItems.GLIMMERFIN_BUCKET, ModAquaticItems.SUNSCALE_CARP_BUCKET, ModAquaticItems.SEAHORSE_BUCKET,
					ModAquaticItems.SEA_BUNNY_BUCKET, ModAquaticItems.PIRANHA_BUCKET,
					ModAquaticItems.GLIMMERFIN_SPAWN_EGG, ModAquaticItems.SUNSCALE_CARP_SPAWN_EGG, ModAquaticItems.SEA_BUNNY_SPAWN_EGG,
					ModAquaticItems.HERMIT_CRAB_SPAWN_EGG, ModAquaticItems.SEAHORSE_SPAWN_EGG, ModAquaticItems.DRIFT_JELLY_SPAWN_EGG,
					ModAquaticItems.STINGRAY_SPAWN_EGG, ModAquaticItems.PIRANHA_SPAWN_EGG, ModAquaticItems.REEF_BARRACUDA_SPAWN_EGG,
					ModAquaticItems.DEEP_ANGLER_SPAWN_EGG}) {
					entries.add(item);
				}
			})
			.build());

	public static void init() {}

	private ModItemGroups() {}
}
