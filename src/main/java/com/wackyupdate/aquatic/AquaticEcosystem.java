package com.wackyupdate.aquatic;

import com.wackyupdate.entity.aquatic.PiranhaEntity;
import com.wackyupdate.registry.ModAquaticItems;
import com.wackyupdate.registry.ModTags;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.item.Item;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.LocationCheckLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.potion.Potions;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;

/**
 * The "living ocean" part of the aquatic update:
 * <ul>
 *   <li>Every fish (vanilla cod, salmon, tropical fish, pufferfish and the new ones) now flees from
 *       aquatic predators: barracudas, piranhas, dolphins, axolotls, guardians, drowned...</li>
 *   <li>Fishing catches depend on where you fish: glimmerfin in oceans, carp in rivers and swamps,
 *       piranhas in jungle and swamp waters.</li>
 *   <li>Brewing: stingray barb makes poison, piranha tooth makes strength.</li>
 * </ul>
 */
public final class AquaticEcosystem {
	public static void init() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof FishEntity fish && !(fish instanceof PiranhaEntity)) {
				boolean hasGoal = fish.goalSelector.getGoals().stream().anyMatch(g -> g.getGoal() instanceof PredatorFleeGoal);
				if (!hasGoal) fish.goalSelector.add(1, new PredatorFleeGoal(fish));
			}
		});

		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (!source.isBuiltin() || !key.equals(LootTables.FISHING_FISH_GAMEPLAY)) return;
			tableBuilder.modifyPools(pool -> {
				pool.with(fishIn(ModAquaticItems.GLIMMERFIN, 18, BiomeTags.IS_OCEAN, registries));
				pool.with(fishIn(ModAquaticItems.SUNSCALE_CARP, 30, BiomeTags.IS_RIVER, registries));
				pool.with(fishIn(ModAquaticItems.PIRANHA, 20, BiomeTags.IS_JUNGLE, registries));
				pool.with(fishIn(ModAquaticItems.PIRANHA, 12, BiomeTags.HAS_CLOSER_WATER_FOG, registries)); // swamps & mangroves
			});
		});

		FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
			builder.registerPotionRecipe(Potions.AWKWARD, ModAquaticItems.STINGRAY_BARB, Potions.POISON);
			builder.registerPotionRecipe(Potions.AWKWARD, ModAquaticItems.PIRANHA_TOOTH, Potions.STRENGTH);
		});
	}

	private static ItemEntry.Builder<?> fishIn(Item item, int weight, TagKey<Biome> biomes, RegistryWrapper.WrapperLookup registries) {
		var biomeList = registries.getWrapperOrThrow(RegistryKeys.BIOME).getOrThrow(biomes);
		return ItemEntry.builder(item).weight(weight)
			.conditionally(LocationCheckLootCondition.builder(LocationPredicate.Builder.create().biome(biomeList)));
	}

	/** Flee from anything in {@code #wackyupdate:aquatic_predators}. */
	public static class PredatorFleeGoal extends FleeEntityGoal<LivingEntity> {
		public PredatorFleeGoal(FishEntity fish) {
			super(fish, LivingEntity.class, e -> e.getType().isIn(ModTags.AQUATIC_PREDATORS), 7.0f, 1.4, 1.8, e -> true);
		}
	}

	private AquaticEcosystem() {}
}
