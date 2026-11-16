package com.wackyupdate.world;

import com.mojang.serialization.Codec;
import com.wackyupdate.block.BuddingSpeculumBlock;
import com.wackyupdate.registry.ModBlocks;
import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.ArrayList;
import java.util.List;

/**
 * A small hollow crystal pocket deep underground: a tuff shell, an inner lining of randomly
 * coloured prism glass with a few budding speculum blocks, and speculum buds growing inwards.
 */
public class SpeculumPocketFeature extends Feature<DefaultFeatureConfig> {
	public SpeculumPocketFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		StructureWorldAccess world = context.getWorld();
		BlockPos origin = context.getOrigin();
		Random random = context.getRandom();
		if (!world.getBlockState(origin).isOpaqueFullCube(world, origin)) return false;

		double radius = 2.6 + random.nextDouble() * 1.2;
		int r = (int) Math.ceil(radius + 1);
		List<BlockPos> budding = new ArrayList<>();
		BlockPos.Mutable p = new BlockPos.Mutable();
		for (int x = -r; x <= r; x++) {
			for (int y = -r; y <= r; y++) {
				for (int z = -r; z <= r; z++) {
					double d = Math.sqrt(x * x + y * y * 1.3 + z * z) + (random.nextDouble() - 0.5) * 0.4;
					if (d > radius + 1) continue;
					p.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
					BlockState existing = world.getBlockState(p);
					if (existing.isOf(Blocks.BEDROCK) || existing.hasBlockEntity() || !existing.getFluidState().isEmpty()) continue;
					if (d < radius - 1) {
						world.setBlockState(p, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
					} else if (d < radius) {
						double roll = random.nextDouble();
						BlockState lining;
						if (roll < 0.16) {
							lining = ModBlocks.BUDDING_SPECULUM.getDefaultState();
							budding.add(p.toImmutable());
						} else if (roll < 0.7) {
							lining = BuddingSpeculumBlock.randomPrismGlass(random);
						} else {
							lining = Blocks.SMOOTH_BASALT.getDefaultState();
						}
						world.setBlockState(p, lining, Block.NOTIFY_LISTENERS);
					} else if (existing.isOpaqueFullCube(world, p) || existing.isAir()) {
						world.setBlockState(p, Blocks.TUFF.getDefaultState(), Block.NOTIFY_LISTENERS);
					}
				}
			}
		}

		Block[] stages = {ModBlocks.SMALL_SPECULUM_BUD, ModBlocks.MEDIUM_SPECULUM_BUD, ModBlocks.LARGE_SPECULUM_BUD, ModBlocks.SPECULUM_CLUSTER};
		for (BlockPos b : budding) {
			for (Direction dir : Direction.values()) {
				BlockPos target = b.offset(dir);
				if (world.getBlockState(target).isAir() && random.nextFloat() < 0.45f) {
					Block stage = stages[random.nextFloat() < 0.25f ? 3 : random.nextInt(3)];
					world.setBlockState(target, stage.getDefaultState().with(AmethystClusterBlock.FACING, dir), Block.NOTIFY_LISTENERS);
				}
			}
		}
		return true;
	}
}
