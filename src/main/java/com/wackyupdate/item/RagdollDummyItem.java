package com.wackyupdate.item;

import com.wackyupdate.entity.RagdollEntity;
import com.wackyupdate.registry.ModEntities;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class RagdollDummyItem extends Item {
	public RagdollDummyItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		if (world instanceof ServerWorld server) {
			BlockPos pos = context.getBlockPos().offset(context.getSide());
			RagdollEntity dummy = ModEntities.RAGDOLL.create(server);
			if (dummy == null) return ActionResult.FAIL;
			float yaw = context.getPlayer() != null ? context.getPlayer().getYaw() + 180f : 0f;
			dummy.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0f);
			dummy.setDummy(true);
			server.spawnEntity(dummy);
			world.playSound(null, pos, SoundEvents.ENTITY_ARMOR_STAND_PLACE, SoundCategory.BLOCKS, 1.0f, 0.8f);
			ItemStack stack = context.getStack();
			stack.decrementUnlessCreative(1, context.getPlayer());
		}
		return ActionResult.success(world.isClient);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.wackyupdate.ragdoll_dummy.tooltip").formatted(Formatting.GRAY));
	}
}
