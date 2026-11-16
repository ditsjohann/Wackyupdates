package com.wackyupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/** A thin, waxed, slippery skate floor tile. */
public class SkateFloorBlock extends Block {
	private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 2, 16);

	public SkateFloorBlock(Settings settings) {
		super(settings);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}
}
