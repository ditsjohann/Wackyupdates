package com.wackyupdate.registry;

import com.wackyupdate.WackyUpdate;
import com.wackyupdate.entity.aquatic.*;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.BiomeKeys;

/** V2 - Aquatic update creatures. */
public final class ModAquaticEntities {
	// passive
	public static final EntityType<GlimmerfinEntity> GLIMMERFIN = register("glimmerfin",
		EntityType.Builder.create(GlimmerfinEntity::new, SpawnGroup.WATER_AMBIENT).dimensions(0.4f, 0.3f).maxTrackingRange(4));
	public static final EntityType<SunscaleCarpEntity> SUNSCALE_CARP = register("sunscale_carp",
		EntityType.Builder.create(SunscaleCarpEntity::new, SpawnGroup.WATER_AMBIENT).dimensions(0.6f, 0.45f).maxTrackingRange(4));
	public static final EntityType<SeaBunnyEntity> SEA_BUNNY = register("sea_bunny",
		EntityType.Builder.create(SeaBunnyEntity::new, SpawnGroup.WATER_AMBIENT).dimensions(0.35f, 0.25f).maxTrackingRange(4));
	public static final EntityType<HermitCrabEntity> HERMIT_CRAB = register("hermit_crab",
		EntityType.Builder.create(HermitCrabEntity::new, SpawnGroup.CREATURE).dimensions(0.5f, 0.45f).maxTrackingRange(8));
	public static final EntityType<SeahorseEntity> SEAHORSE = register("seahorse",
		EntityType.Builder.create(SeahorseEntity::new, SpawnGroup.WATER_AMBIENT).dimensions(0.3f, 0.6f).maxTrackingRange(4));
	// neutral
	public static final EntityType<DriftJellyEntity> DRIFT_JELLY = register("drift_jelly",
		EntityType.Builder.create(DriftJellyEntity::new, SpawnGroup.WATER_AMBIENT).dimensions(0.7f, 0.9f).maxTrackingRange(6));
	public static final EntityType<StingrayEntity> STINGRAY = register("stingray",
		EntityType.Builder.create(StingrayEntity::new, SpawnGroup.WATER_CREATURE).dimensions(1.0f, 0.3f).maxTrackingRange(8));
	// hostile
	public static final EntityType<PiranhaEntity> PIRANHA = register("piranha",
		EntityType.Builder.create(PiranhaEntity::new, SpawnGroup.WATER_AMBIENT).dimensions(0.4f, 0.4f).maxTrackingRange(6));
	public static final EntityType<ReefBarracudaEntity> REEF_BARRACUDA = register("reef_barracuda",
		EntityType.Builder.create(ReefBarracudaEntity::new, SpawnGroup.WATER_CREATURE).dimensions(0.6f, 0.45f).maxTrackingRange(8));
	public static final EntityType<DeepAnglerEntity> DEEP_ANGLER = register("deep_angler",
		EntityType.Builder.create(DeepAnglerEntity::new, SpawnGroup.WATER_CREATURE).dimensions(1.1f, 1.0f).maxTrackingRange(8));

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		return Registry.register(Registries.ENTITY_TYPE, WackyUpdate.id(name), builder.build(WackyUpdate.id(name).toString()));
	}

	/** In water with a solid sea floor right below. */
	private static boolean onSeaFloor(WorldAccess world, BlockPos pos) {
		return world.getFluidState(pos).isIn(FluidTags.WATER)
			&& world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP);
	}

	private static boolean inWater(WorldAccess world, BlockPos pos) {
		return world.getFluidState(pos).isIn(FluidTags.WATER) && world.getFluidState(pos.up()).isIn(FluidTags.WATER);
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(GLIMMERFIN, FishEntity.createFishAttributes());
		FabricDefaultAttributeRegistry.register(SUNSCALE_CARP, SunscaleCarpEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(SEA_BUNNY, SeaBunnyEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(HERMIT_CRAB, HermitCrabEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(SEAHORSE, FishEntity.createFishAttributes());
		FabricDefaultAttributeRegistry.register(DRIFT_JELLY, DriftJellyEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(STINGRAY, StingrayEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(PIRANHA, PiranhaEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(REEF_BARRACUDA, ReefBarracudaEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(DEEP_ANGLER, DeepAnglerEntity.createAttributes());

		SpawnRestriction.register(GLIMMERFIN, SpawnLocationTypes.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, WaterCreatureEntity::canSpawn);
		SpawnRestriction.register(SUNSCALE_CARP, SpawnLocationTypes.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, WaterCreatureEntity::canSpawn);
		SpawnRestriction.register(SEAHORSE, SpawnLocationTypes.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, WaterCreatureEntity::canSpawn);
		SpawnRestriction.register(SEA_BUNNY, SpawnLocationTypes.IN_WATER, Heightmap.Type.OCEAN_FLOOR,
			(type, world, reason, pos, random) -> onSeaFloor(world, pos));
		SpawnRestriction.register(DRIFT_JELLY, SpawnLocationTypes.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
			(type, world, reason, pos, random) -> inWater(world, pos) && pos.getY() > world.getSeaLevel() - 25);
		SpawnRestriction.register(STINGRAY, SpawnLocationTypes.IN_WATER, Heightmap.Type.OCEAN_FLOOR,
			(type, world, reason, pos, random) -> onSeaFloor(world, pos) && world.getBlockState(pos.down()).isIn(BlockTags.SAND));
		SpawnRestriction.register(HERMIT_CRAB, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
			(type, world, reason, pos, random) -> world.getBlockState(pos.down()).isIn(BlockTags.SAND));
		SpawnRestriction.register(PIRANHA, SpawnLocationTypes.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
			(type, world, reason, pos, random) -> world.getDifficulty() != Difficulty.PEACEFUL && inWater(world, pos));
		SpawnRestriction.register(REEF_BARRACUDA, SpawnLocationTypes.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
			(type, world, reason, pos, random) -> world.getDifficulty() != Difficulty.PEACEFUL && inWater(world, pos));
		SpawnRestriction.register(DEEP_ANGLER, SpawnLocationTypes.IN_WATER, Heightmap.Type.OCEAN_FLOOR,
			(type, world, reason, pos, random) -> world.getDifficulty() != Difficulty.PEACEFUL && inWater(world, pos)
				&& pos.getY() < world.getSeaLevel() - 20 && world.getLightLevel(LightType.SKY, pos) < 6);

		var warm = BiomeSelectors.includeByKey(BiomeKeys.WARM_OCEAN, BiomeKeys.LUKEWARM_OCEAN, BiomeKeys.DEEP_LUKEWARM_OCEAN);
		BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_OCEAN).and(BiomeSelectors.excludeByKey(BiomeKeys.FROZEN_OCEAN,
			BiomeKeys.DEEP_FROZEN_OCEAN)), SpawnGroup.WATER_AMBIENT, GLIMMERFIN, 14, 4, 8);
		BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_RIVER).or(BiomeSelectors.includeByKey(BiomeKeys.SWAMP)),
			SpawnGroup.WATER_AMBIENT, SUNSCALE_CARP, 10, 1, 3);
		BiomeModifications.addSpawn(warm, SpawnGroup.WATER_AMBIENT, SEA_BUNNY, 6, 1, 3);
		BiomeModifications.addSpawn(warm.or(BiomeSelectors.includeByKey(BiomeKeys.OCEAN)), SpawnGroup.WATER_AMBIENT, SEAHORSE, 6, 1, 2);
		BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_OCEAN), SpawnGroup.WATER_AMBIENT, DRIFT_JELLY, 5, 1, 3);
		BiomeModifications.addSpawn(warm, SpawnGroup.WATER_CREATURE, STINGRAY, 3, 1, 1);
		BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_BEACH), SpawnGroup.CREATURE, HERMIT_CRAB, 10, 2, 4);
		BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_JUNGLE).or(BiomeSelectors.includeByKey(BiomeKeys.SWAMP,
			BiomeKeys.MANGROVE_SWAMP)), SpawnGroup.WATER_AMBIENT, PIRANHA, 8, 3, 6);
		BiomeModifications.addSpawn(warm, SpawnGroup.WATER_CREATURE, REEF_BARRACUDA, 3, 1, 2);
		BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_DEEP_OCEAN), SpawnGroup.WATER_CREATURE, DEEP_ANGLER, 4, 1, 1);
	}

	private ModAquaticEntities() {}
}
