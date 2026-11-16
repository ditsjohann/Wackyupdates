package com.wackyupdate.light;

import com.wackyupdate.block.PrismGlassBlock;
import com.wackyupdate.registry.ModTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Stainable;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

/**
 * The colour of a light beam: one of the 16 dye colours, plain white, or rainbow.
 * White and rainbow light contain every colour, so they pass every receptor filter.
 */
public record BeamColor(@Nullable DyeColor dye, boolean rainbow) {
	public static final BeamColor WHITE = new BeamColor(null, false);
	public static final BeamColor RAINBOW = new BeamColor(null, true);

	public static BeamColor of(DyeColor dye) {
		return new BeamColor(dye, false);
	}

	/** True if a receptor filtering for {@code filter} (null = any colour) reacts to this beam. */
	public boolean matches(@Nullable DyeColor filter) {
		return filter == null || dye == null || dye == filter;
	}

	/** RGB to draw this beam with. {@code time} animates rainbow beams. */
	public int rgb(float time, int index) {
		if (rainbow) return MathHelper.hsvToRgb(((time * 0.01f + index * 0.14f) % 1f + 1f) % 1f, 0.8f, 1f);
		if (dye == null) return 0xFFFFFF;
		return dye.getEntityColor() & 0xFFFFFF;
	}

	/** The colour a projector emits when {@code lens} sits in front of it, or null if it is not a lens. */
	@Nullable
	public static BeamColor fromLens(BlockState lens) {
		BeamColor filtered = filterOf(lens);
		if (filtered != null) return filtered;
		if (lens.isIn(ModTags.RAINBOW_LENSES)) return RAINBOW;
		if (lens.isOf(Blocks.GLASS) || lens.isOf(Blocks.GLASS_PANE)) return WHITE;
		return null;
	}

	/** If a beam passing through this block gets recoloured, the new colour; otherwise null. */
	@Nullable
	public static BeamColor filterOf(BlockState state) {
		if (state.getBlock() instanceof PrismGlassBlock) return of(state.get(PrismGlassBlock.COLOR));
		if (state.getBlock() instanceof Stainable stainable) return of(stainable.getColor());
		return null;
	}
}
