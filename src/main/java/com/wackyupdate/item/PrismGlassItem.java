package com.wackyupdate.item;

import com.wackyupdate.block.PrismGlassBlock;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;

import java.util.List;

/** Prism glass keeps its colour in the item's block_state component. */
public class PrismGlassItem extends BlockItem {
	public PrismGlassItem(Block block, Settings settings) {
		super(block, settings);
	}

	public static DyeColor getColor(ItemStack stack) {
		BlockStateComponent component = stack.getOrDefault(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT);
		DyeColor color = component.getValue(PrismGlassBlock.COLOR);
		return color != null ? color : DyeColor.WHITE;
	}

	public static ItemStack withColor(ItemStack stack, DyeColor color) {
		stack.set(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT.with(PrismGlassBlock.COLOR, color));
		return stack;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("color.minecraft." + getColor(stack).getName()).formatted(Formatting.GRAY));
	}
}
