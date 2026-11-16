package com.wackyupdate.block;

import com.wackyupdate.entity.GravelMiteEntity;
import com.wackyupdate.registry.ModEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.ColoredFallingBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ColorCode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;

/** Looks exactly like gravel. Gravel mites hide inside; dig it up and they scurry out. */
public class MiteGravelBlock extends ColoredFallingBlock {
	public MiteGravelBlock(ColorCode color, Settings settings) {
		super(color, settings);
	}

	@Override
	public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
		super.onStacksDropped(state, world, pos, tool, dropExperience);
		if (world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)
			&& !EnchantmentHelper.hasAnyEnchantmentsIn(tool, EnchantmentTags.PREVENTS_INFESTED_SPAWNS)) {
			int count = 1 + world.random.nextInt(2);
			for (int i = 0; i < count; i++) {
				GravelMiteEntity mite = ModEntities.GRAVEL_MITE.create(world);
				if (mite == null) continue;
				mite.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.random.nextFloat() * 360f, 0f);
				world.spawnEntity(mite);
				mite.playSpawnEffects();
			}
		}
	}
}
