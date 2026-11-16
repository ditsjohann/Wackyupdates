package com.wackyupdate.block.entity;

import com.wackyupdate.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class PrismProjectorBlockEntity extends BlockEntity {
	public enum Mode implements StringIdentifiable {
		SWEEP("sweep"), SPIN("spin"), STROBE("strobe"), FAN("fan"), DISCO("disco");

		private final String name;

		Mode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return name;
		}
	}

	private Mode mode = Mode.SWEEP;
	private int phase = 0;

	public PrismProjectorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PRISM_PROJECTOR, pos, state);
	}

	public Mode getMode() {
		return mode;
	}

	public int getPhase() {
		return phase;
	}

	public void cycleMode() {
		mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
		sync();
	}

	public void cyclePhase() {
		phase = (phase + 1) % 4;
		sync();
	}

	private void sync() {
		markDirty();
		if (world != null) {
			world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		nbt.putString("Mode", mode.asString());
		nbt.putInt("Phase", phase);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		String name = nbt.getString("Mode");
		for (Mode m : Mode.values()) {
			if (m.asString().equals(name)) mode = m;
		}
		phase = nbt.getInt("Phase");
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return createNbt(registryLookup);
	}
}
