package com.wackyupdate.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * A polished speculum mirror. Any focused beam that enters it leaves through its mirror face (FACING).
 * Right-click a face to point the mirror there; sneak + right-click points it the opposite way.
 */
public class SpeculumMirrorBlock extends FacingBlock {
	public static final MapCodec<SpeculumMirrorBlock> CODEC = createCodec(SpeculumMirrorBlock::new);

	public SpeculumMirrorBlock(Settings settings) {
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
		return aim(state, world, pos, player, hit);
	}

	/** Shared by mirrors and splitters: point FACING at the clicked face (or away from it when sneaking). */
	static ActionResult aim(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		Direction target = player.isSneaking() ? hit.getSide().getOpposite() : hit.getSide();
		if (state.get(FACING) == target) return ActionResult.PASS;
		if (!world.isClient) {
			world.setBlockState(pos, state.with(FACING, target), Block.NOTIFY_ALL);
			world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_STEP, SoundCategory.BLOCKS, 1.0f, 1.4f);
		}
		return ActionResult.success(world.isClient);
	}
}
