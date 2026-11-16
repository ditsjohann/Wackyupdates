package com.wackyupdate.registry;

import com.wackyupdate.WackyUpdate;
import com.wackyupdate.block.AnglerLanternBlock;
import com.wackyupdate.block.GlowAnemoneBlock;
import com.wackyupdate.block.JellyLampBlock;
import com.wackyupdate.block.entity.AnglerLanternBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

/** V2 - Aquatic update blocks. */
public final class ModAquaticBlocks {
	public static final Block ANGLER_LANTERN = register("angler_lantern", new AnglerLanternBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.CYAN).strength(1.5f).sounds(BlockSoundGroup.LANTERN).luminance(s -> 15).nonOpaque()
		.pistonBehavior(PistonBehavior.DESTROY)));
	public static final Block JELLY_LAMP = register("jelly_lamp", new JellyLampBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.PINK).strength(0.6f).sounds(BlockSoundGroup.SLIME).luminance(s -> 13).nonOpaque()
		.allowsSpawning((s, w, p, t) -> false).solidBlock((s, w, p) -> false).suffocates((s, w, p) -> false).blockVision((s, w, p) -> false)));
	public static final Block GLOW_ANEMONE = register("glow_anemone", new GlowAnemoneBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.PINK).strength(0.2f).sounds(BlockSoundGroup.WET_GRASS).luminance(s -> 8).noCollision().nonOpaque()
		.pistonBehavior(PistonBehavior.DESTROY)));

	public static final BlockEntityType<AnglerLanternBlockEntity> ANGLER_LANTERN_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
		WackyUpdate.id("angler_lantern"), BlockEntityType.Builder.create(AnglerLanternBlockEntity::new, ANGLER_LANTERN).build(null));

	private static Block register(String name, Block block) {
		return Registry.register(Registries.BLOCK, WackyUpdate.id(name), block);
	}

	public static void init() {}

	private ModAquaticBlocks() {}
}
