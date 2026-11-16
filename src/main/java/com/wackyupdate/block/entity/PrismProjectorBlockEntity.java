package com.wackyupdate.block.entity;

import com.wackyupdate.block.PrismProjectorBlock;
import com.wackyupdate.light.BeamColor;
import com.wackyupdate.light.BeamTracer;
import com.wackyupdate.registry.ModBlockEntities;
import com.wackyupdate.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

/**
 * State and server logic for a Prism Projector.
 *
 * Causes:  redstone power (on/off + beam strength), note blocks & jukeboxes (MUSIC mode).
 * Effects: lightshow beams, focused beams that power Light Receptors and feed Budding Speculum,
 *          and a comparator output that pulses on the beat / on each note.
 */
public class PrismProjectorBlockEntity extends BlockEntity implements GameEventListener.Holder<PrismProjectorBlockEntity.NoteListener> {
	public static final int TICKS_PER_BEAT = 10; // 120 BPM
	public static final int EVENT_NOTE = 1, EVENT_JUKEBOX_ON = 2, EVENT_JUKEBOX_OFF = 3;

	public enum Mode implements StringIdentifiable {
		SWEEP("sweep"), SPIN("spin"), STROBE("strobe"), FAN("fan"), DISCO("disco"),
		/** A straight logic laser that travels through the world and can be redirected. */
		FOCUS("focus"),
		/** Fires a burst on every note block note (coloured by pitch) and dances to jukeboxes. */
		MUSIC("music");

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
	private final NoteListener listener;

	// music state (synced to clients through block events)
	private long lastNoteTime = Long.MIN_VALUE / 2;
	private int lastNote;
	private long jukeboxUntil = Long.MIN_VALUE / 2;

	// server-side bookkeeping
	private int comparatorOutput;
	private int focusHits;

	// client-side cache of the traced focus beam, refreshed once per tick
	@Nullable
	public BeamTracer.Result cachedTrace;
	public long cachedTraceTime = Long.MIN_VALUE;

	public PrismProjectorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PRISM_PROJECTOR, pos, state);
		listener = new NoteListener(new BlockPositionSource(pos));
	}

	public Mode getMode() {
		return mode;
	}

	public int getPhase() {
		return phase;
	}

	public long getLastNoteTime() {
		return lastNoteTime;
	}

	public int getLastNote() {
		return lastNote;
	}

	public boolean isJukeboxPlaying(long time) {
		return time < jukeboxUntil;
	}

	public int getComparatorOutput() {
		return comparatorOutput;
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

	/** Beat position for this projector including its phase offset (beats are 10 ticks long). */
	public float beat(float time) {
		return (time + phase * TICKS_PER_BEAT / 4f) / TICKS_PER_BEAT;
	}

	/** Colour this projector emits given the block in front of it, or null if there is no lens. */
	@Nullable
	public static BeamColor lensColor(World world, BlockPos pos, BlockState state) {
		Direction facing = state.get(PrismProjectorBlock.FACING);
		return BeamColor.fromLens(world.getBlockState(pos.offset(facing)));
	}

	/** Traces the focused beam. Strength = redstone power in, range grows with power. */
	public BeamTracer.Result traceFocus(World world, BlockState state) {
		BeamColor color = lensColor(world, pos, state);
		int power = world.getReceivedRedstonePower(pos);
		if (color == null || power <= 0) return BeamTracer.Result.EMPTY;
		Direction facing = state.get(PrismProjectorBlock.FACING);
		return BeamTracer.trace(world, pos.offset(facing), facing, color, power, 16 + power * 2);
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, PrismProjectorBlockEntity projector) {
		long time = world.getTime();
		boolean powered = state.get(PrismProjectorBlock.POWERED);

		if (projector.mode == Mode.FOCUS && powered && time % 2 == 0) {
			BeamTracer.Result result = projector.traceFocus(world, state);
			int lit = 0;
			for (BeamTracer.BlockHit hit : result.blockHits()) {
				if (world.getBlockEntity(hit.pos()) instanceof LightReceptorBlockEntity receptor) {
					receptor.receive(hit.color(), hit.strength(), time);
					lit++;
				} else if (hit.state().isOf(ModBlocks.BUDDING_SPECULUM) && world.random.nextInt(20) == 0) {
					// grow light: a focused beam makes budding speculum grow much faster
					hit.state().randomTick((ServerWorld) world, hit.pos(), world.random);
				}
			}
			projector.focusHits = lit;
		} else if (projector.mode != Mode.FOCUS || !powered) {
			projector.focusHits = 0;
		}

		int output = projector.computeComparatorOutput(time, powered);
		if (output != projector.comparatorOutput) {
			projector.comparatorOutput = output;
			world.updateComparators(pos, state.getBlock());
		}
	}

	/**
	 * Comparator output:
	 * shows    - 15 for the first 3 ticks of every beat (sync pistons, lamps, note blocks...)
	 * FOCUS    - the number of receptors the beam is currently powering
	 * MUSIC    - for 4 ticks after each note, 1-15 scaled by the note's pitch
	 */
	private int computeComparatorOutput(long time, boolean powered) {
		if (mode == Mode.MUSIC) {
			return time - lastNoteTime < 4 ? 1 + Math.round(lastNote * 14f / 24f) : 0;
		}
		if (!powered) return 0;
		if (mode == Mode.FOCUS) return Math.min(15, focusHits);
		float beat = beat(time);
		return beat - MathHelper.floor(beat) < 0.3f ? 15 : 0;
	}

	@Override
	public boolean onSyncedBlockEvent(int type, int data) {
		if (world == null) return false;
		long time = world.getTime();
		switch (type) {
			case EVENT_NOTE -> {
				lastNoteTime = time;
				lastNote = data;
			}
			case EVENT_JUKEBOX_ON -> jukeboxUntil = time + 30;
			case EVENT_JUKEBOX_OFF -> jukeboxUntil = time;
			default -> {
				return super.onSyncedBlockEvent(type, data);
			}
		}
		return true;
	}

	@Override
	public NoteListener getEventListener() {
		return listener;
	}

	/** Hears note blocks and jukeboxes within 16 blocks while the projector is in MUSIC mode. */
	public class NoteListener implements GameEventListener {
		private final PositionSource source;

		NoteListener(PositionSource source) {
			this.source = source;
		}

		@Override
		public PositionSource getPositionSource() {
			return source;
		}

		@Override
		public int getRange() {
			return 16;
		}

		@Override
		public boolean listen(ServerWorld world, RegistryEntry<GameEvent> event, GameEvent.Emitter emitter, Vec3d emitterPos) {
			if (mode != Mode.MUSIC || isRemoved()) return false;
			Block block = getCachedState().getBlock();
			if (event.matchesKey(GameEvent.NOTE_BLOCK_PLAY.registryKey())) {
				BlockState noteState = world.getBlockState(BlockPos.ofFloored(emitterPos));
				int note = noteState.contains(NoteBlock.NOTE) ? noteState.get(NoteBlock.NOTE) : world.random.nextInt(25);
				world.addSyncedBlockEvent(pos, block, EVENT_NOTE, note);
				return true;
			}
			if (event.matchesKey(GameEvent.JUKEBOX_PLAY.registryKey())) {
				world.addSyncedBlockEvent(pos, block, EVENT_JUKEBOX_ON, 0);
				return true;
			}
			if (event.matchesKey(GameEvent.JUKEBOX_STOP_PLAY.registryKey())) {
				world.addSyncedBlockEvent(pos, block, EVENT_JUKEBOX_OFF, 0);
				return true;
			}
			return false;
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
