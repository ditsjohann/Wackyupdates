package com.wackyupdate.block;

import com.wackyupdate.registry.ModParticles;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** A lantern that attracts fireflies: they drift around it in a 9x9 area. */
public class SpeculumLanternBlock extends LanternBlock {
	public SpeculumLanternBlock(Settings settings) {
		super(settings);
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		int count = 2 + random.nextInt(3);
		for (int i = 0; i < count; i++) {
			double x = pos.getX() + 0.5 + (random.nextDouble() * 9.0 - 4.5);
			double y = pos.getY() + 0.5 + (random.nextDouble() * 4.0 - 1.5);
			double z = pos.getZ() + 0.5 + (random.nextDouble() * 9.0 - 4.5);
			world.addParticle(ModParticles.FIREFLY, x, y, z, 0, 0, 0);
		}
	}
}
