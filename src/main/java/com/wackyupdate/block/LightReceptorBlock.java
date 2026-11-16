package com.wackyupdate.block;

import com.mojang.serialization.MapCodec;
import com.wackyupdate.block.entity.LightReceptorBlockEntity;
import com.wackyupdate.light.ColorFilter;
import com.wackyupdate.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Turns focused light into redstone. Outputs a signal equal to the strongest beam hitting it
 * (beam strength = the redstone power going into the projector, minus losses on the way).
 * Right-click with a dye to only react to that colour; right-click empty-handed to react to any.
 * White and rainbow beams pass every filter.
 */
public class LightReceptorBlock extends BlockWithEntity {
	public static final MapCodec<LightReceptorBlock> CODEC = createCodec(LightReceptorBlock::new);
	public static final IntProperty POWER = Properties.POWER;
	public static final EnumProperty<ColorFilter> FILTER = EnumProperty.of("filter", ColorFilter.class);

	public LightReceptorBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(POWER, 0).with(FILTER, ColorFilter.ANY));
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(POWER, FILTER);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new LightReceptorBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, ModBlockEntities.LIGHT_RECEPTOR, LightReceptorBlockEntity::serverTick);
	}

	@Override
	public boolean emitsRedstonePower(BlockState state) {
		return true;
	}

	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		return state.get(POWER);
	}

	@Override
	public ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!(stack.getItem() instanceof DyeItem dye)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		setFilter(state, world, pos, player, ColorFilter.of(dye.getColor()));
		return ItemActionResult.success(world.isClient);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		setFilter(state, world, pos, player, ColorFilter.ANY);
		return ActionResult.success(world.isClient);
	}

	private static void setFilter(BlockState state, World world, BlockPos pos, PlayerEntity player, ColorFilter filter) {
		if (world.isClient) return;
		world.setBlockState(pos, state.with(FILTER, filter), Block.NOTIFY_ALL);
		world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.0f, 1.2f);
		Text name = filter == ColorFilter.ANY
			? Text.translatable("message.wackyupdate.receptor.any")
			: Text.translatable("color.minecraft." + filter.asString());
		player.sendMessage(Text.translatable("message.wackyupdate.receptor.filter", name).formatted(Formatting.AQUA), true);
	}
}
