package com.wackyupdate.block;

import com.wackyupdate.registry.ModBlocks;
import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

/**
 * Grows speculum buds on its faces like budding amethyst. Once a face holds a fully grown
 * cluster, the crystal pushes out randomly coloured prism glass in front of it.
 */
public class BuddingSpeculumBlock extends Block {
	private static final Direction[] DIRECTIONS = Direction.values();

	public BuddingSpeculumBlock(Settings settings) {
		super(settings);
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (random.nextInt(5) != 0) return;
		Direction dir = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
		BlockPos target = pos.offset(dir);
		BlockState targetState = world.getBlockState(target);

		Block next = null;
		if (canGrowIn(targetState)) {
			next = ModBlocks.SMALL_SPECULUM_BUD;
		} else if (targetState.isOf(ModBlocks.SMALL_SPECULUM_BUD) && targetState.get(AmethystClusterBlock.FACING) == dir) {
			next = ModBlocks.MEDIUM_SPECULUM_BUD;
		} else if (targetState.isOf(ModBlocks.MEDIUM_SPECULUM_BUD) && targetState.get(AmethystClusterBlock.FACING) == dir) {
			next = ModBlocks.LARGE_SPECULUM_BUD;
		} else if (targetState.isOf(ModBlocks.LARGE_SPECULUM_BUD) && targetState.get(AmethystClusterBlock.FACING) == dir) {
			next = ModBlocks.SPECULUM_CLUSTER;
		} else if (targetState.isOf(ModBlocks.SPECULUM_CLUSTER) && random.nextInt(3) == 0) {
			// A grown cluster crystallises coloured glass one step further out.
			BlockPos glassPos = target.offset(dir);
			if (world.getBlockState(glassPos).isAir()) {
				world.setBlockState(glassPos, randomPrismGlass(random), Block.NOTIFY_ALL);
			}
			return;
		}

		if (next != null) {
			BlockState grown = next.getDefaultState()
				.with(AmethystClusterBlock.FACING, dir)
				.with(Properties.WATERLOGGED, targetState.getFluidState().getFluid() == Fluids.WATER);
			world.setBlockState(target, grown);
		}
	}

	public static boolean canGrowIn(BlockState state) {
		return state.isAir() || state.isOf(Blocks.WATER) && state.getFluidState().getLevel() == 8;
	}

	public static BlockState randomPrismGlass(Random random) {
		DyeColor[] colors = DyeColor.values();
		return ModBlocks.PRISM_GLASS.getDefaultState().with(PrismGlassBlock.COLOR, colors[random.nextInt(colors.length)]);
	}
}
