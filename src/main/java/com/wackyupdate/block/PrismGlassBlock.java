package com.wackyupdate.block;

import com.mojang.serialization.MapCodec;
import com.wackyupdate.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TransparentBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Clear "connected" glass: faces between any two prism glass blocks are culled, so walls read as
 * one seamless pane. It renders full-bright (glows inwards) but has 0 luminance (no outward light).
 * Right-click with a dye to recolour it, or with a speculum shard to randomise the colour.
 */
public class PrismGlassBlock extends TransparentBlock {
	public static final MapCodec<PrismGlassBlock> CODEC = createCodec(PrismGlassBlock::new);
	public static final EnumProperty<DyeColor> COLOR = EnumProperty.of("color", DyeColor.class);

	public PrismGlassBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(COLOR, DyeColor.WHITE));
	}

	@Override
	public MapCodec<PrismGlassBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(COLOR);
	}

	@Override
	public ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		DyeColor newColor = null;
		if (stack.getItem() instanceof DyeItem dye) {
			newColor = dye.getColor();
		} else if (stack.isOf(ModItems.SPECULUM_SHARD)) {
			DyeColor[] colors = DyeColor.values();
			newColor = colors[world.random.nextInt(colors.length)];
		}
		if (newColor == null || newColor == state.get(COLOR) && !stack.isOf(ModItems.SPECULUM_SHARD)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!world.isClient) {
			world.setBlockState(pos, state.with(COLOR, newColor), Block.NOTIFY_ALL);
			world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.0f, 0.8f + world.random.nextFloat() * 0.6f);
			if (stack.getItem() instanceof DyeItem) stack.decrementUnlessCreative(1, player);
		} else {
			for (int i = 0; i < 6; i++) {
				world.addParticle(ParticleTypes.WAX_OFF, pos.getX() + world.random.nextDouble(), pos.getY() + world.random.nextDouble(),
					pos.getZ() + world.random.nextDouble(), 0, 0, 0);
			}
		}
		return ItemActionResult.success(world.isClient);
	}
}
