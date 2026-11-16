package com.wackyupdate.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/** Step on it and get flung forward in the direction of the arrow. */
public class BoostPadBlock extends HorizontalFacingBlock {
	public static final MapCodec<BoostPadBlock> CODEC = createCodec(BoostPadBlock::new);
	private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 1, 16);
	private static final double MAX_SPEED = 1.5;

	public BoostPadBlock(Settings settings) {
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
		return SHAPE;
	}

	@Override
	public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
		super.onSteppedOn(world, pos, state, entity);
		// Players move themselves on their client; every other entity is moved by the server.
		boolean player = entity instanceof PlayerEntity;
		if (player != world.isClient) return;
		Direction facing = state.get(FACING);
		Vec3d v = entity.getVelocity();
		Vec3d push = new Vec3d(facing.getOffsetX(), 0, facing.getOffsetZ()).multiply(0.5);
		Vec3d boosted = v.add(push);
		double horizontal = boosted.horizontalLength();
		if (horizontal > MAX_SPEED) {
			boosted = new Vec3d(boosted.x / horizontal * MAX_SPEED, boosted.y, boosted.z / horizontal * MAX_SPEED);
		}
		entity.setVelocity(boosted.x, Math.max(boosted.y, 0.12), boosted.z);
		entity.velocityModified = true;
		if (world.isClient && v.horizontalLength() < 0.9) {
			for (int i = 0; i < 4; i++) {
				world.addParticle(ParticleTypes.FIREWORK, entity.getX(), pos.getY() + 0.1, entity.getZ(),
					-facing.getOffsetX() * 0.2 + (world.random.nextDouble() - 0.5) * 0.1, 0.05,
					-facing.getOffsetZ() * 0.2 + (world.random.nextDouble() - 0.5) * 0.1);
			}
			world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_BREEZE_JUMP, SoundCategory.BLOCKS, 0.4f, 1.6f, false);
		}
	}
}
