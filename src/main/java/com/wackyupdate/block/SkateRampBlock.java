package com.wackyupdate.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.util.EnumMap;
import java.util.Map;

/**
 * A 45 degree skate ramp. It rises towards FACING. The collision shape is eight tiny 2px steps,
 * so anything (with or without skates) glides smoothly up it.
 */
public class SkateRampBlock extends HorizontalFacingBlock {
	public static final MapCodec<SkateRampBlock> CODEC = createCodec(SkateRampBlock::new);
	private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

	static {
		for (Direction dir : Direction.Type.HORIZONTAL) {
			VoxelShape shape = VoxelShapes.empty();
			for (int i = 0; i < 8; i++) {
				int h = 2 * (i + 1);
				int a = 2 * i;
				VoxelShape step = switch (dir) {
					case NORTH -> Block.createCuboidShape(0, 0, 0, 16, h, 16 - a);
					case SOUTH -> Block.createCuboidShape(0, 0, a, 16, h, 16);
					case WEST -> Block.createCuboidShape(0, 0, 0, 16 - a, h, 16);
					default -> Block.createCuboidShape(a, 0, 0, 16, h, 16);
				};
				shape = VoxelShapes.union(shape, step);
			}
			SHAPES.put(dir, shape.simplify());
		}
	}

	public SkateRampBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPES.get(state.get(FACING));
	}
}
