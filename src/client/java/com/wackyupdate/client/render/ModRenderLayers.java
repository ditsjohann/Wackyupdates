package com.wackyupdate.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import java.util.OptionalDouble;

/** Extends RenderLayer only to reach its protected render phases. */
public abstract class ModRenderLayers extends RenderLayer {
	/** Lines drawn on top of everything (no depth test) - used by the Spectre Lens mite vision. */
	public static final RenderLayer XRAY_LINES = RenderLayer.of("wackyupdate_xray_lines", VertexFormats.LINES, VertexFormat.DrawMode.LINES, 1536,
		RenderLayer.MultiPhaseParameters.builder()
			.program(LINES_PROGRAM)
			.lineWidth(new LineWidth(OptionalDouble.of(2.5)))
			.layering(VIEW_OFFSET_Z_LAYERING)
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.writeMaskState(COLOR_MASK)
			.cull(DISABLE_CULLING)
			.depthTest(ALWAYS_DEPTH_TEST)
			.build(false));

	private ModRenderLayers(String name, VertexFormat format, VertexFormat.DrawMode mode, int size, boolean crumbling, boolean translucent,
			Runnable start, Runnable end) {
		super(name, format, mode, size, crumbling, translucent, start, end);
	}
}
