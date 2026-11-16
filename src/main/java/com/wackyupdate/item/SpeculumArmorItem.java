package com.wackyupdate.item;

import com.wackyupdate.registry.ModArmorMaterials;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.Rarity;

/** Full crystallised-glass armour: tougher than diamond, but it chips faster. */
public class SpeculumArmorItem extends ArmorItem {
	public SpeculumArmorItem(Type type) {
		super(ModArmorMaterials.SPECULUM, type, new Settings()
			.maxDamage(type.getMaxDamage(ModArmorMaterials.SPECULUM_DURABILITY)).rarity(Rarity.RARE));
	}
}
