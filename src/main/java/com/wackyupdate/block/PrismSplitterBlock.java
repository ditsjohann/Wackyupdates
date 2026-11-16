package com.wackyupdate.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Lets a focused beam pass straight through and also sends a copy out of its marked face (FACING).
 * Each output is one strength weaker. Right-click a face to choose the side output.
 */
public class PrismSplitterBlock extends FacingBlock {
	public static final MapCodec<PrismSplitterBlock> CODEC = createCodec(PrismSplitterBlock::new);

	public PrismSplitterBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(FACING, Direction.UP));
	}

	@Override
	protected MapCodec<? extends FacingBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getSide());
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		return SpeculumMirrorBlock.aim(state, world, pos, player, hit);
	}

	@Override
	protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
		return stateFrom.isOf(this) || super.isSideInvisible(state, stateFrom, direction);
	}
}
