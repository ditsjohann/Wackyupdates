package com.wackyupdate.block.entity;

import com.wackyupdate.block.AnglerLanternBlock;
import com.wackyupdate.registry.ModAquaticBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AnglerLanternBlockEntity extends BlockEntity {
	public static final int RANGE = 16;

	public AnglerLanternBlockEntity(BlockPos pos, BlockState state) {
		super(ModAquaticBlocks.ANGLER_LANTERN_ENTITY, pos, state);
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, AnglerLanternBlockEntity lantern) {
		if (world.getTime() % 40 != Math.floorMod(pos.hashCode(), 40) || !state.get(AnglerLanternBlock.WATERLOGGED)) return;
		Vec3d center = Vec3d.ofCenter(pos);
		for (MobEntity mob : world.getEntitiesByClass(MobEntity.class, new Box(pos).expand(RANGE),
				m -> (m instanceof WaterCreatureEntity || m instanceof AxolotlEntity) && m.getTarget() == null && !m.isLeashed())) {
			if (mob.squaredDistanceTo(center) > 9) {
				mob.getNavigation().startMovingTo(center.x + world.random.nextDouble() * 3 - 1.5, center.y,
					center.z + world.random.nextDouble() * 3 - 1.5, 1.0);
			}
		}
	}
}
