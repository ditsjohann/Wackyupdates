package com.wackyupdate.block;

import com.wackyupdate.mechanic.SkateHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/** A grind rail. Hop onto it wearing rollerskates and you grind along it, keeping your speed. */
public class SkateRailBlock extends Block {
	public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;
	private static final VoxelShape SHAPE_Z = VoxelShapes.union(
		Block.createCuboidShape(6.5, 4, 0, 9.5, 7, 16),
		Block.createCuboidShape(7, 0, 3, 9, 4, 5),
		Block.createCuboidShape(7, 0, 11, 9, 4, 13));
	private static final VoxelShape SHAPE_X = VoxelShapes.union(
		Block.createCuboidShape(0, 4, 6.5, 16, 7, 9.5),
		Block.createCuboidShape(3, 0, 7, 5, 4, 9),
		Block.createCuboidShape(11, 0, 7, 13, 4, 9));

	public SkateRailBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(AXIS, Direction.Axis.Z));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(AXIS, ctx.getHorizontalPlayerFacing().getAxis());
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
	}

	@Override
	public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
		super.onSteppedOn(world, pos, state, entity);
		// Player movement is client-authoritative, so the grind is applied on the player's own client.
		if (!(entity instanceof PlayerEntity player) || !world.isClient || !SkateHandler.isWearingSkates(player)) return;
		Vec3d v = player.getVelocity();
		Direction.Axis axis = state.get(AXIS);
		double along = axis == Direction.Axis.X ? v.x : v.z;
		if (Math.abs(along) < 0.05) {
			Vec3d look = player.getRotationVector();
			along = axis == Direction.Axis.X ? look.x : look.z;
		}
		double speed = Math.copySign(MathHelper.clamp(Math.abs(along) * 1.04, 0.45, 1.3), along);
		double center = axis == Direction.Axis.X ? pos.getZ() + 0.5 - player.getZ() : pos.getX() + 0.5 - player.getX();
		if (axis == Direction.Axis.X) {
			player.setVelocity(speed, v.y, center * 0.35);
		} else {
			player.setVelocity(center * 0.35, v.y, speed);
		}
		if (world.random.nextInt(2) == 0) {
			world.addParticle(ParticleTypes.ELECTRIC_SPARK, player.getX(), pos.getY() + 0.45, player.getZ(),
				(world.random.nextDouble() - 0.5) * 0.3, 0.15, (world.random.nextDouble() - 0.5) * 0.3);
		}
	}
}
