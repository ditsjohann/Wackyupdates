package com.wackyupdate.registry;

import com.wackyupdate.WackyUpdate;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/** V2 - Aquatic update items. */
public final class ModAquaticItems {
	// food
	public static final Item GLIMMERFIN = register("glimmerfin", new Item(new Item.Settings().food(new FoodComponent.Builder()
		.nutrition(1).saturationModifier(0.2f).statusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0), 1.0f).build())));
	public static final Item SUNSCALE_CARP = register("sunscale_carp", food(2, 0.1f));
	public static final Item COOKED_SUNSCALE_CARP = register("cooked_sunscale_carp", food(6, 0.8f));
	public static final Item PIRANHA = register("piranha", food(2, 0.1f));
	public static final Item COOKED_PIRANHA = register("cooked_piranha", food(5, 0.6f));
	public static final Item BARRACUDA_FILLET = register("barracuda_fillet", food(3, 0.2f));
	public static final Item COOKED_BARRACUDA_FILLET = register("cooked_barracuda_fillet", food(8, 0.8f));

	// drops & materials
	public static final Item JELLY_GEL = register("jelly_gel", new Item(new Item.Settings()));
	public static final Item STINGRAY_BARB = register("stingray_barb", new Item(new Item.Settings()));
	public static final Item PIRANHA_TOOTH = register("piranha_tooth", new Item(new Item.Settings()));
	public static final Item ANGLER_LURE = register("angler_lure", new Item(new Item.Settings().rarity(net.minecraft.util.Rarity.UNCOMMON)));
	public static final Item HERMIT_SHELL = register("hermit_shell", new Item(new Item.Settings()));

	// buckets
	public static final Item GLIMMERFIN_BUCKET = register("glimmerfin_bucket", bucket(ModAquaticEntities.GLIMMERFIN, SoundEvents.ITEM_BUCKET_EMPTY_FISH));
	public static final Item SUNSCALE_CARP_BUCKET = register("sunscale_carp_bucket", bucket(ModAquaticEntities.SUNSCALE_CARP, SoundEvents.ITEM_BUCKET_EMPTY_FISH));
	public static final Item SEAHORSE_BUCKET = register("seahorse_bucket", bucket(ModAquaticEntities.SEAHORSE, SoundEvents.ITEM_BUCKET_EMPTY_FISH));
	public static final Item SEA_BUNNY_BUCKET = register("sea_bunny_bucket", bucket(ModAquaticEntities.SEA_BUNNY, SoundEvents.ITEM_BUCKET_EMPTY_AXOLOTL));
	public static final Item PIRANHA_BUCKET = register("piranha_bucket", bucket(ModAquaticEntities.PIRANHA, SoundEvents.ITEM_BUCKET_EMPTY_FISH));

	// spawn eggs
	public static final Item GLIMMERFIN_SPAWN_EGG = egg("glimmerfin", ModAquaticEntities.GLIMMERFIN, 0x2b7fd6, 0x7ff7ff);
	public static final Item SUNSCALE_CARP_SPAWN_EGG = egg("sunscale_carp", ModAquaticEntities.SUNSCALE_CARP, 0xe0a420, 0xfff0a0);
	public static final Item SEA_BUNNY_SPAWN_EGG = egg("sea_bunny", ModAquaticEntities.SEA_BUNNY, 0xf4f4f4, 0x222222);
	public static final Item HERMIT_CRAB_SPAWN_EGG = egg("hermit_crab", ModAquaticEntities.HERMIT_CRAB, 0xd8553a, 0xf2d7a6);
	public static final Item SEAHORSE_SPAWN_EGG = egg("seahorse", ModAquaticEntities.SEAHORSE, 0xf28c28, 0xffd25a);
	public static final Item DRIFT_JELLY_SPAWN_EGG = egg("drift_jelly", ModAquaticEntities.DRIFT_JELLY, 0xd889ff, 0x8ff0ff);
	public static final Item STINGRAY_SPAWN_EGG = egg("stingray", ModAquaticEntities.STINGRAY, 0x8a7a63, 0x3d342a);
	public static final Item PIRANHA_SPAWN_EGG = egg("piranha", ModAquaticEntities.PIRANHA, 0x8f9aa3, 0xd22f2f);
	public static final Item REEF_BARRACUDA_SPAWN_EGG = egg("reef_barracuda", ModAquaticEntities.REEF_BARRACUDA, 0xc3ccd3, 0x2f3a45);
	public static final Item DEEP_ANGLER_SPAWN_EGG = egg("deep_angler", ModAquaticEntities.DEEP_ANGLER, 0x1b1f2e, 0x7fffd4);

	static {
		for (Block block : new Block[]{ModAquaticBlocks.ANGLER_LANTERN, ModAquaticBlocks.JELLY_LAMP, ModAquaticBlocks.GLOW_ANEMONE}) {
			Registry.register(Registries.ITEM, Registries.BLOCK.getId(block), new BlockItem(block, new Item.Settings()));
		}
	}

	private static Item food(int nutrition, float saturation) {
		return new Item(new Item.Settings().food(new FoodComponent.Builder().nutrition(nutrition).saturationModifier(saturation).build()));
	}

	private static Item bucket(EntityType<?> type, SoundEvent emptySound) {
		return new EntityBucketItem(type, Fluids.WATER, emptySound,
			new Item.Settings().maxCount(1).component(DataComponentTypes.BUCKET_ENTITY_DATA, NbtComponent.DEFAULT));
	}

	private static Item egg(String name, EntityType<? extends net.minecraft.entity.mob.MobEntity> type, int primary, int secondary) {
		return register(name + "_spawn_egg", new SpawnEggItem(type, primary, secondary, new Item.Settings()));
	}

	private static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, WackyUpdate.id(name), item);
	}

	public static void init() {}

	private ModAquaticItems() {}
}
