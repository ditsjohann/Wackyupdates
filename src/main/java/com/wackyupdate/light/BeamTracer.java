package com.wackyupdate.light;

import com.wackyupdate.block.LightReceptorBlock;
import com.wackyupdate.block.PrismSplitterBlock;
import com.wackyupdate.block.SpeculumMirrorBlock;
import com.wackyupdate.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

/**
 * Follows a focused light beam block by block through the world. It runs identically on the
 * server (for redstone and growth effects) and on the client (for drawing), so both always agree.
 *
 * <ul>
 *   <li>Air, plain glass, leaves, water and other non-solid blocks let light through.</li>
 *   <li>Prism glass / stained glass recolours the beam (strength -1).</li>
 *   <li>A Speculum Mirror sends the beam out of its mirror face (lossless).</li>
 *   <li>A Prism Splitter lets the beam through AND sends a copy out of its side face (strength -1 each).</li>
 *   <li>Light Receptors and Budding Speculum absorb the beam (they react to it).</li>
 *   <li>Solid blocks stop it. So do living creatures standing in it - laser tripwires!</li>
 * </ul>
 */
public final class BeamTracer {
	private static final int MAX_SEGMENTS = 64;

	public record Segment(Vec3d start, Vec3d end, BeamColor color, int strength) {}

	public record BlockHit(BlockPos pos, BlockState state, BeamColor color, int strength) {}

	public record Result(List<Segment> segments, List<BlockHit> blockHits, List<LivingEntity> entityHits) {
		public static final Result EMPTY = new Result(List.of(), List.of(), List.of());
	}

	private record Beam(BlockPos from, Vec3d start, Direction dir, BeamColor color, int strength, int range) {}

	private record Visit(BlockPos pos, Direction dir) {}

	public static Result trace(World world, BlockPos lensPos, Direction dir, BeamColor color, int strength, int range) {
		List<Segment> segments = new ArrayList<>();
		List<BlockHit> hits = new ArrayList<>();
		List<LivingEntity> entities = new ArrayList<>();
		Deque<Beam> queue = new ArrayDeque<>();
		Set<Visit> visited = new HashSet<>();
		if (strength > 0) queue.add(new Beam(lensPos, Vec3d.ofCenter(lensPos), dir, color, strength, range));

		while (!queue.isEmpty() && segments.size() < MAX_SEGMENTS) {
			Beam beam = queue.poll();
			if (!visited.add(new Visit(beam.from, beam.dir))) continue; // mirror loops
			Direction d = beam.dir;
			BlockPos cur = beam.from;
			int steps = 0;
			boolean done = false;
			while (steps < beam.range && !done) {
				BlockPos next = cur.offset(d);
				if (!world.isChunkLoaded(next) || world.isOutOfHeightLimit(next)) break;
				cur = next;
				steps++;
				BlockState state = world.getBlockState(cur);
				Vec3d center = Vec3d.ofCenter(cur);
				int left = beam.range - steps;

				if (state.getBlock() instanceof SpeculumMirrorBlock) {
					done = true;
					if (emit(world, beam.start, center, beam, segments, entities)) {
						queue.add(new Beam(cur, center, state.get(SpeculumMirrorBlock.FACING), beam.color, beam.strength, left));
					}
				} else if (state.getBlock() instanceof PrismSplitterBlock) {
					done = true;
					if (emit(world, beam.start, center, beam, segments, entities) && beam.strength > 1) {
						Direction side = state.get(PrismSplitterBlock.FACING);
						queue.add(new Beam(cur, center, d, beam.color, beam.strength - 1, left));
						if (side.getAxis() != d.getAxis()) {
							queue.add(new Beam(cur, center, side, beam.color, beam.strength - 1, left));
						}
					}
				} else if (state.getBlock() instanceof LightReceptorBlock || state.isOf(ModBlocks.BUDDING_SPECULUM)) {
					done = true;
					Vec3d face = center.subtract(d.getOffsetX() * 0.5, d.getOffsetY() * 0.5, d.getOffsetZ() * 0.5);
					if (emit(world, beam.start, face, beam, segments, entities)) {
						hits.add(new BlockHit(cur.toImmutable(), state, beam.color, beam.strength));
					}
				} else {
					BeamColor filter = BeamColor.filterOf(state);
					if (filter != null) {
						if (!filter.equals(beam.color)) {
							done = true;
							if (emit(world, beam.start, center, beam, segments, entities) && beam.strength > 1) {
								queue.add(new Beam(cur, center, d, filter, beam.strength - 1, left));
							}
						}
					} else if (state.isOpaqueFullCube(world, cur)) {
						done = true;
						Vec3d face = center.subtract(d.getOffsetX() * 0.5, d.getOffsetY() * 0.5, d.getOffsetZ() * 0.5);
						emit(world, beam.start, face, beam, segments, entities);
					}
				}
			}
			if (!done) {
				emit(world, beam.start, Vec3d.ofCenter(cur), beam, segments, entities);
			}
		}
		return new Result(segments, hits, entities);
	}

	/** Adds the segment start->end, cut short at the first creature in the way. Returns false if a creature blocked it. */
	private static boolean emit(World world, Vec3d start, Vec3d end, Beam beam, List<Segment> segments, List<LivingEntity> entityHits) {
		LivingEntity closest = null;
		Vec3d closestHit = null;
		double best = Double.MAX_VALUE;
		Box area = new Box(start, end).expand(0.3);
		for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, area, e -> e.isAlive() && !e.isSpectator())) {
			Optional<Vec3d> hit = entity.getBoundingBox().raycast(start, end);
			if (hit.isPresent()) {
				double dist = hit.get().squaredDistanceTo(start);
				if (dist < best) {
					best = dist;
					closest = entity;
					closestHit = hit.get();
				}
			}
		}
		if (closest != null) {
			segments.add(new Segment(start, closestHit, beam.color, beam.strength));
			entityHits.add(closest);
			return false;
		}
		segments.add(new Segment(start, end, beam.color, beam.strength));
		return true;
	}

	private BeamTracer() {}
}
