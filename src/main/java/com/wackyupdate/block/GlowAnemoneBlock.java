package com.wackyupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

/**
 * Glowing reef anemone. Its tentacles sting hostile monsters (drowned, guardians, piranhas,
 * barracudas, anglers...) but leave everything else alone - reefs full of anemones are safe havens
 * for fish. Grows in warm and lukewarm oceans; needs a solid floor.
 */
public class GlowAnemoneBlock extends WaterloggableShapeBlock {
	public GlowAnemoneBlock(Settings settings) {
		super(Block.createCuboidShape(2, 0, 2, 14, 10, 14), settings);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos below = pos.down();
		return world.getBlockState(below).isSideSolidFullSquare(world, below, Direction.UP);
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (direction == Direction.DOWN && !canPlaceAt(state, world, pos)) {
			return Blocks.AIR.getDefaultState();
		}
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (!world.isClient && entity instanceof LivingEntity living && entity instanceof Monster && world.getTime() % 10 == 0) {
			living.damage(world.getDamageSources().sweetBerryBush(), 2.0f);
		}
	}
}
