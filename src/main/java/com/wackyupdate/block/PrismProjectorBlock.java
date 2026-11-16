package com.wackyupdate.block;

import com.mojang.serialization.MapCodec;
import com.wackyupdate.block.entity.PrismProjectorBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Lightshow block. Put a crystal "lens" directly in front of it (prism glass, stained glass,
 * or any speculum crystal for rainbow) and give it redstone. Right-click cycles the show pattern,
 * sneak + right-click shifts its beat offset so several projectors can play off each other.
 * Stronger redstone signal = longer beams. All projectors share one world-time beat, so they sync.
 */
public class PrismProjectorBlock extends BlockWithEntity {
	public static final MapCodec<PrismProjectorBlock> CODEC = createCodec(PrismProjectorBlock::new);
	public static final DirectionProperty FACING = Properties.FACING;
	public static final BooleanProperty POWERED = Properties.POWERED;

	public PrismProjectorBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(FACING, Direction.UP).with(POWERED, false));
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getSide())
			.with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new PrismProjectorBlockEntity(pos, state);
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
		if (world.isClient) return;
		boolean powered = world.isReceivingRedstonePower(pos);
		if (powered != state.get(POWERED)) {
			world.setBlockState(pos, state.with(POWERED, powered), Block.NOTIFY_ALL);
		} else {
			// Power level may have changed (beam length) - make sure clients re-render.
			world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
		}
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof PrismProjectorBlockEntity projector)) return ActionResult.PASS;
		if (!world.isClient) {
			if (player.isSneaking()) {
				projector.cyclePhase();
				player.sendMessage(Text.translatable("message.wackyupdate.projector.phase", projector.getPhase() + 1)
					.formatted(Formatting.AQUA), true);
			} else {
				projector.cycleMode();
				player.sendMessage(Text.translatable("message.wackyupdate.projector.mode",
					Text.translatable("lightshow.wackyupdate." + projector.getMode().asString())).formatted(Formatting.LIGHT_PURPLE), true);
			}
			world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.BLOCKS, 0.8f, 1.2f + world.random.nextFloat() * 0.4f);
		}
		return ActionResult.success(world.isClient);
	}
}
