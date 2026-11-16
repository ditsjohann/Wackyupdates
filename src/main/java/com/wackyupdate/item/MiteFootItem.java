package com.wackyupdate.item;

import com.wackyupdate.registry.ModBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * A gravel mite's little foot. Hold 10 or more, sneak and use to squish exactly 10 of them into a
 * Compacted Feet Block (a crafting grid only has 9 slots, so the squishing is done by hand).
 */
public class MiteFootItem extends Item {
	public static final int FEET_PER_BLOCK = 10;

	public MiteFootItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!user.isSneaking()) return TypedActionResult.pass(stack);
		if (stack.getCount() < FEET_PER_BLOCK && !user.isCreative()) {
			if (!world.isClient) {
				user.sendMessage(Text.translatable("message.wackyupdate.need_feet", FEET_PER_BLOCK - stack.getCount()).formatted(Formatting.RED), true);
			}
			return TypedActionResult.fail(stack);
		}
		if (!world.isClient) {
			stack.decrementUnlessCreative(FEET_PER_BLOCK, user);
			ItemStack block = new ItemStack(ModBlocks.COMPACTED_FEET_BLOCK);
			if (!user.getInventory().insertStack(block)) {
				user.dropItem(block, false);
			}
			world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.PLAYERS, 1.0f, 1.3f);
		}
		return TypedActionResult.success(stack, world.isClient);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.wackyupdate.mite_foot.tooltip").formatted(Formatting.GRAY));
	}
}
