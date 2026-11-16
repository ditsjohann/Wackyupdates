package com.wackyupdate.registry;

import com.wackyupdate.WackyUpdate;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public final class ModArmorMaterials {
	/**
	 * Speculum: a bit stronger than diamond (22 armor points total vs 20, toughness 2.5 vs 2)
	 * but less durable (durability multiplier 22 vs diamond's 33).
	 */
	public static final int SPECULUM_DURABILITY = 22;
	public static final RegistryEntry<ArmorMaterial> SPECULUM = register("speculum", 3, 7, 9, 3, 11, 18,
		SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, () -> Ingredient.ofItems(ModItems.SPECULUM_SHARD), 2.5f, 0.05f);

	public static final RegistryEntry<ArmorMaterial> ROLLERSKATE = register("rollerskate", 1, 1, 1, 1, 1, 10,
		SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, () -> Ingredient.ofItems(ModItems.MITE_FOOT, Items.LEATHER), 0f, 0f);

	private static RegistryEntry<ArmorMaterial> register(String name, int boots, int legs, int chest, int helmet, int body,
			int enchantability, RegistryEntry<SoundEvent> sound, Supplier<Ingredient> repair, float toughness, float knockbackResistance) {
		EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
		defense.put(ArmorItem.Type.BOOTS, boots);
		defense.put(ArmorItem.Type.LEGGINGS, legs);
		defense.put(ArmorItem.Type.CHESTPLATE, chest);
		defense.put(ArmorItem.Type.HELMET, helmet);
		defense.put(ArmorItem.Type.BODY, body);
		List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(WackyUpdate.id(name)));
		return Registry.registerReference(Registries.ARMOR_MATERIAL, WackyUpdate.id(name),
			new ArmorMaterial(defense, enchantability, sound, repair, layers, toughness, knockbackResistance));
	}

	public static void init() {}

	private ModArmorMaterials() {}
}
