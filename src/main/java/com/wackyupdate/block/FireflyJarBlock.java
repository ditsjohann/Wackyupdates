package com.wackyupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import com.wackyupdate.registry.ModParticles;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class FireflyJarBlock extends Block {
	private static final VoxelShape SHAPE = Block.createCuboidShape(4, 0, 4, 12, 11, 12);

	public FireflyJarBlock(Settings settings) {
		super(settings);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (random.nextInt(2) == 0) {
			world.addParticle(ModParticles.FIREFLY, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.5,
				pos.getY() + 0.6 + random.nextDouble() * 1.2, pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.5, 0, 0, 0);
		}
	}
}
