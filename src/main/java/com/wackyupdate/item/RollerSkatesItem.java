package com.wackyupdate.item;

import com.wackyupdate.registry.ModArmorMaterials;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class RollerSkatesItem extends ArmorItem {
	public RollerSkatesItem(Settings settings) {
		super(ModArmorMaterials.ROLLERSKATE, Type.BOOTS, settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.wackyupdate.rollerskates.tooltip").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("item.wackyupdate.rollerskates.tooltip2").formatted(Formatting.GRAY));
	}
}
