package com.wackyupdate.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
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
 * Two uses:
 * 1. Hold it together with a shovel (one in each hand) and hidden gravel mites show up through
 *    the gravel (handled client-side, see LensVision).
 * 2. Use it on its own for "Spectre Sight": every creature within 24 blocks glows through walls
 *    for 6 seconds.
 */
public class SpectreLensItem extends Item {
	public SpectreLensItem(Settings settings) {
		super(settings);
	}

	public static boolean isLensWithShovel(PlayerEntity player) {
		ItemStack main = player.getMainHandStack();
		ItemStack off = player.getOffHandStack();
		return main.getItem() instanceof SpectreLensItem && off.getItem() instanceof ShovelItem
			|| off.getItem() instanceof SpectreLensItem && main.getItem() instanceof ShovelItem;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (isLensWithShovel(user)) {
			return TypedActionResult.pass(stack);
		}
		if (!world.isClient) {
			List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, user.getBoundingBox().expand(24), e -> e != user && e.isAlive());
			for (LivingEntity target : targets) {
				target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 120, 0, false, false), user);
			}
			world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS, 1.0f, 1.6f);
			stack.damage(1, user, LivingEntity.getSlotForHand(hand));
		}
		user.getItemCooldownManager().set(this, 300);
		return TypedActionResult.success(stack, world.isClient);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.wackyupdate.spectre_lens.tooltip").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("item.wackyupdate.spectre_lens.tooltip2").formatted(Formatting.GRAY));
	}
}
