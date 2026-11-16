package com.wackyupdate.registry;

import com.wackyupdate.WackyUpdate;
import com.wackyupdate.world.SpeculumPocketFeature;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

public final class ModWorldGen {
	public static final Feature<DefaultFeatureConfig> SPECULUM_POCKET = Registry.register(Registries.FEATURE,
		WackyUpdate.id("speculum_pocket"), new SpeculumPocketFeature(DefaultFeatureConfig.CODEC));

	private static RegistryKey<PlacedFeature> placed(String name) {
		return RegistryKey.of(RegistryKeys.PLACED_FEATURE, WackyUpdate.id(name));
	}

	public static void init() {
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_DECORATION, placed("speculum_pocket"));
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, placed("mite_gravel"));
		BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.PLAINS, BiomeKeys.SUNFLOWER_PLAINS, BiomeKeys.MEADOW,
			BiomeKeys.FOREST, BiomeKeys.BIRCH_FOREST, BiomeKeys.FLOWER_FOREST, BiomeKeys.SAVANNA, BiomeKeys.CHERRY_GROVE),
			GenerationStep.Feature.VEGETAL_DECORATION, placed("slickwood_tree"));
	}

	private ModWorldGen() {}
}
