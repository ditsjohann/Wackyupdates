package com.wackyupdate.registry;

import com.wackyupdate.WackyUpdate;
import com.wackyupdate.entity.*;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;

public final class ModEntities {
	public static final EntityType<GravelMiteEntity> GRAVEL_MITE = register("gravel_mite",
		EntityType.Builder.create(GravelMiteEntity::new, SpawnGroup.AMBIENT).dimensions(0.4f, 0.25f).maxTrackingRange(8));
	public static final EntityType<PrismSnailEntity> PRISM_SNAIL = register("prism_snail",
		EntityType.Builder.create(PrismSnailEntity::new, SpawnGroup.CREATURE).dimensions(0.5f, 0.6f).maxTrackingRange(10));
	public static final EntityType<RollypinEntity> ROLLYPIN = register("rollypin",
		EntityType.Builder.create(RollypinEntity::new, SpawnGroup.CREATURE).dimensions(0.6f, 0.55f).maxTrackingRange(10));
	public static final EntityType<GlowtoadEntity> GLOWTOAD = register("glowtoad",
		EntityType.Builder.create(GlowtoadEntity::new, SpawnGroup.CREATURE).dimensions(0.55f, 0.45f).maxTrackingRange(10));
	public static final EntityType<ShardlingEntity> SHARDLING = register("shardling",
		EntityType.Builder.create(ShardlingEntity::new, SpawnGroup.MONSTER).dimensions(0.7f, 1.5f).maxTrackingRange(8));
	public static final EntityType<SkateGhoulEntity> SKATE_GHOUL = register("skate_ghoul",
		EntityType.Builder.create(SkateGhoulEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.95f).maxTrackingRange(8));
	public static final EntityType<RagdollEntity> RAGDOLL = register("ragdoll",
		EntityType.Builder.<RagdollEntity>create(RagdollEntity::new, SpawnGroup.MISC).dimensions(0.8f, 0.5f)
			.maxTrackingRange(10).trackingTickInterval(1));

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		return Registry.register(Registries.ENTITY_TYPE, WackyUpdate.id(name), builder.build(WackyUpdate.id(name).toString()));
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(GRAVEL_MITE, GravelMiteEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(PRISM_SNAIL, PrismSnailEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ROLLYPIN, RollypinEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(GLOWTOAD, GlowtoadEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(SHARDLING, ShardlingEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(SKATE_GHOUL, SkateGhoulEntity.createAttributes());

		SpawnRestriction.register(PRISM_SNAIL, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
		SpawnRestriction.register(ROLLYPIN, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
		SpawnRestriction.register(GLOWTOAD, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
			(type, world, reason, pos, random) -> world.getBlockState(pos.down()).isIn(BlockTags.ANIMALS_SPAWNABLE_ON)
				|| world.getBlockState(pos.down()).isIn(BlockTags.FROGS_SPAWNABLE_ON));
		SpawnRestriction.register(SHARDLING, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
			(type, world, reason, pos, random) -> pos.getY() < 16 && HostileEntity.canSpawnInDark(type, world, reason, pos, random));
		SpawnRestriction.register(SKATE_GHOUL, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
		SpawnRestriction.register(GRAVEL_MITE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);

		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.SWAMP, BiomeKeys.FOREST, BiomeKeys.DARK_FOREST,
			BiomeKeys.FLOWER_FOREST, BiomeKeys.BIRCH_FOREST, BiomeKeys.LUSH_CAVES), SpawnGroup.CREATURE, PRISM_SNAIL, 8, 1, 3);
		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.SAVANNA, BiomeKeys.SAVANNA_PLATEAU,
			BiomeKeys.WINDSWEPT_SAVANNA, BiomeKeys.SPARSE_JUNGLE), SpawnGroup.CREATURE, ROLLYPIN, 7, 1, 2);
		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.SWAMP, BiomeKeys.MANGROVE_SWAMP, BiomeKeys.RIVER,
			BiomeKeys.MEADOW), SpawnGroup.CREATURE, GLOWTOAD, 8, 2, 4);
		BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, SHARDLING, 25, 1, 2);
		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.PLAINS, BiomeKeys.SUNFLOWER_PLAINS, BiomeKeys.SAVANNA,
			BiomeKeys.FOREST, BiomeKeys.BIRCH_FOREST, BiomeKeys.MEADOW, BiomeKeys.DESERT), SpawnGroup.MONSTER, SKATE_GHOUL, 18, 1, 2);
	}

	private ModEntities() {}
}
