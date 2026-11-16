package com.wackyupdate.registry;

import com.wackyupdate.WackyUpdate;
import com.wackyupdate.block.*;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ColorCode;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import java.util.Optional;

public final class ModBlocks {
	public static final RegistryKey<ConfiguredFeature<?, ?>> SLICKWOOD_TREE_KEY =
		RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, WackyUpdate.id("slickwood_tree"));
	public static final SaplingGenerator SLICKWOOD_GROWER =
		new SaplingGenerator("slickwood", Optional.empty(), Optional.of(SLICKWOOD_TREE_KEY), Optional.empty());

	// ---------------------------------------------------------------- Speculum crystal
	public static final Block BUDDING_SPECULUM = register("budding_speculum", new BuddingSpeculumBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.CYAN).ticksRandomly().strength(1.8f).requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)
		.pistonBehavior(PistonBehavior.DESTROY)));
	public static final Block SPECULUM_CLUSTER = register("speculum_cluster", new AmethystClusterBlock(7.0f, 3.0f, crystal().luminance(s -> 6)));
	public static final Block LARGE_SPECULUM_BUD = register("large_speculum_bud", new AmethystClusterBlock(5.0f, 3.0f, crystal().luminance(s -> 4)));
	public static final Block MEDIUM_SPECULUM_BUD = register("medium_speculum_bud", new AmethystClusterBlock(4.0f, 3.0f, crystal().luminance(s -> 2)));
	public static final Block SMALL_SPECULUM_BUD = register("small_speculum_bud", new AmethystClusterBlock(3.0f, 4.0f, crystal().luminance(s -> 1)));

	/** Clear, glowing-inside glass in 16 colours. Luminance 0: it lights itself, never its surroundings. */
	public static final Block PRISM_GLASS = register("prism_glass", new PrismGlassBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.CLEAR).instrument(NoteBlockInstrument.HAT).strength(0.4f).sounds(BlockSoundGroup.GLASS)
		.nonOpaque().emissiveLighting((s, w, p) -> true).allowsSpawning((s, w, p, t) -> false)
		.solidBlock((s, w, p) -> false).suffocates((s, w, p) -> false).blockVision((s, w, p) -> false)));

	public static final Block SPECULUM_LANTERN = register("speculum_lantern", new SpeculumLanternBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.CYAN).solid().strength(3.5f).sounds(BlockSoundGroup.LANTERN).luminance(s -> 15).nonOpaque()
		.pistonBehavior(PistonBehavior.DESTROY)));

	// ---------------------------------------------------------------- Gravel mites
	public static final Block MITE_GRAVEL = register("mite_gravel", new MiteGravelBlock(new ColorCode(-8356741),
		AbstractBlock.Settings.copy(Blocks.GRAVEL)));
	public static final Block COMPACTED_FEET_BLOCK = register("compacted_feet_block", new CompactedFeetBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.TERRACOTTA_PINK).strength(0.8f).sounds(BlockSoundGroup.SLIME)));

	// ---------------------------------------------------------------- Slickwood tree + skate stuff
	public static final Block SLICKWOOD_LOG = register("slickwood_log", Blocks.createLogBlock(MapColor.PALE_YELLOW, MapColor.TERRACOTTA_WHITE));
	public static final Block STRIPPED_SLICKWOOD_LOG = register("stripped_slickwood_log", Blocks.createLogBlock(MapColor.PALE_YELLOW, MapColor.PALE_YELLOW));
	public static final Block SLICKWOOD_PLANKS = register("slickwood_planks", new Block(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS).mapColor(MapColor.PALE_YELLOW)));
	public static final Block SLICKWOOD_LEAVES = register("slickwood_leaves", new LeavesBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_LEAVES).mapColor(MapColor.CYAN)));
	public static final Block SLICKWOOD_SAPLING = register("slickwood_sapling", new SaplingBlock(SLICKWOOD_GROWER, AbstractBlock.Settings.copy(Blocks.BIRCH_SAPLING)));
	public static final Block SLICKWOOD_RAMP = register("slickwood_ramp", new SkateRampBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS).nonOpaque()));
	public static final Block SLICKWOOD_FLOOR = register("slickwood_floor", new SkateFloorBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_PLANKS).slipperiness(0.8f).nonOpaque()));

	// ---------------------------------------------------------------- 10 extra blocks
	public static final Block SPECULUM_BLOCK = register("speculum_block", new SpeculumBlock(speculumStone()));
	public static final Block POLISHED_SPECULUM = register("polished_speculum", new SpeculumBlock(speculumStone()));
	public static final Block SPECULUM_BRICKS = register("speculum_bricks", new SpeculumBlock(speculumStone()));
	public static final Block PRISM_TILES = register("prism_tiles", new Block(AbstractBlock.Settings.create()
		.mapColor(MapColor.WHITE).strength(1.2f).requiresTool().sounds(BlockSoundGroup.GLASS).luminance(s -> 4)));
	public static final Block FIREFLY_JAR = register("firefly_jar", new FireflyJarBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.YELLOW).strength(0.3f).sounds(BlockSoundGroup.GLASS).luminance(s -> 10).nonOpaque()
		.pistonBehavior(PistonBehavior.DESTROY)));
	public static final Block SKATE_RAIL = register("skate_rail", new SkateRailBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.IRON_GRAY).strength(2.0f).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque()));
	public static final Block BOOST_PAD = register("boost_pad", new BoostPadBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.ORANGE).strength(1.0f).sounds(BlockSoundGroup.COPPER).luminance(s -> 7).nonOpaque()));
	public static final Block SLICKWOOD_SLAB = register("slickwood_slab", new SlabBlock(AbstractBlock.Settings.copy(SLICKWOOD_PLANKS)));
	public static final Block SLICKWOOD_STAIRS = register("slickwood_stairs", new StairsBlock(SLICKWOOD_PLANKS.getDefaultState(), AbstractBlock.Settings.copy(SLICKWOOD_PLANKS)));
	public static final Block SLICKWOOD_FENCE = register("slickwood_fence", new FenceBlock(AbstractBlock.Settings.copy(SLICKWOOD_PLANKS)));

	// ---------------------------------------------------------------- Lightshow
	public static final Block PRISM_PROJECTOR = register("prism_projector", new PrismProjectorBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.IRON_GRAY).strength(3.0f).requiresTool().sounds(BlockSoundGroup.METAL)
		.luminance(s -> s.get(PrismProjectorBlock.POWERED) ? 9 : 0)));

	private static AbstractBlock.Settings crystal() {
		return AbstractBlock.Settings.create().mapColor(MapColor.CYAN).solid().nonOpaque().sounds(BlockSoundGroup.AMETHYST_CLUSTER)
			.strength(1.5f).pistonBehavior(PistonBehavior.DESTROY);
	}

	private static AbstractBlock.Settings speculumStone() {
		return AbstractBlock.Settings.create().mapColor(MapColor.CYAN).strength(2.0f).requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK);
	}

	private static Block register(String name, Block block) {
		return Registry.register(Registries.BLOCK, WackyUpdate.id(name), block);
	}

	public static void init() {
		StrippableBlockRegistry.register(SLICKWOOD_LOG, STRIPPED_SLICKWOOD_LOG);
		FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
		fire.add(SLICKWOOD_LOG, 5, 5);
		fire.add(STRIPPED_SLICKWOOD_LOG, 5, 5);
		fire.add(SLICKWOOD_PLANKS, 5, 20);
		fire.add(SLICKWOOD_SLAB, 5, 20);
		fire.add(SLICKWOOD_STAIRS, 5, 20);
		fire.add(SLICKWOOD_FENCE, 5, 20);
		fire.add(SLICKWOOD_RAMP, 5, 20);
		fire.add(SLICKWOOD_FLOOR, 5, 20);
		fire.add(SLICKWOOD_LEAVES, 30, 60);
	}

	private ModBlocks() {}
}
