package com.wackyupdate.block.entity;

import com.wackyupdate.block.LightReceptorBlock;
import com.wackyupdate.light.BeamColor;
import com.wackyupdate.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/** Remembers the beams that hit this receptor recently and turns them into a redstone level. */
public class LightReceptorBlockEntity extends BlockEntity {
	/** Beams are traced every 2 ticks; allow a little slack before the signal drops. */
	private static final int HOLD_TICKS = 4;
	private long lastHitTime = Long.MIN_VALUE / 2;
	private long windowTime = Long.MIN_VALUE / 2;
	private int windowStrength;
	private int heldStrength;

	public LightReceptorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.LIGHT_RECEPTOR, pos, state);
	}

	/** Called by a projector's beam trace on the server. */
	public void receive(BeamColor color, int strength, long time) {
		if (!color.matches(getCachedState().get(LightReceptorBlock.FILTER).dye())) return;
		if (time != windowTime) {
			windowTime = time;
			windowStrength = 0;
		}
		windowStrength = Math.max(windowStrength, strength);
		heldStrength = windowStrength;
		lastHitTime = time;
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, LightReceptorBlockEntity receptor) {
		int power = world.getTime() - receptor.lastHitTime <= HOLD_TICKS ? MathHelper.clamp(receptor.heldStrength, 0, 15) : 0;
		if (power != state.get(LightReceptorBlock.POWER)) {
			world.setBlockState(pos, state.with(LightReceptorBlock.POWER, power), Block.NOTIFY_ALL);
		}
	}
}
