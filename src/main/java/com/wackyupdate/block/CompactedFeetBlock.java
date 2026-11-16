package com.wackyupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/** Ten little mite feet, squished together. Springy! */
public class CompactedFeetBlock extends Block {
	public CompactedFeetBlock(Settings settings) {
		super(settings);
	}

	@Override
	public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
		if (entity.bypassesLandingEffects()) {
			super.onLandedUpon(world, state, pos, entity, fallDistance);
		} else {
			entity.handleFallDamage(fallDistance, 0.2f, world.getDamageSources().fall());
		}
	}

	@Override
	public void onEntityLand(BlockView world, Entity entity) {
		if (entity.bypassesLandingEffects()) {
			super.onEntityLand(world, entity);
			return;
		}
		Vec3d v = entity.getVelocity();
		if (v.y < 0.0) {
			double factor = entity instanceof LivingEntity ? 0.85 : 0.7;
			entity.setVelocity(v.x, -v.y * factor, v.z);
		}
	}
}
