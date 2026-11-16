package com.wackyupdate.light;

import net.minecraft.util.DyeColor;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

/** A Light Receptor's colour filter: any colour, or exactly one dye colour. */
public enum ColorFilter implements StringIdentifiable {
	ANY(null),
	WHITE(DyeColor.WHITE), ORANGE(DyeColor.ORANGE), MAGENTA(DyeColor.MAGENTA), LIGHT_BLUE(DyeColor.LIGHT_BLUE),
	YELLOW(DyeColor.YELLOW), LIME(DyeColor.LIME), PINK(DyeColor.PINK), GRAY(DyeColor.GRAY),
	LIGHT_GRAY(DyeColor.LIGHT_GRAY), CYAN(DyeColor.CYAN), PURPLE(DyeColor.PURPLE), BLUE(DyeColor.BLUE),
	BROWN(DyeColor.BROWN), GREEN(DyeColor.GREEN), RED(DyeColor.RED), BLACK(DyeColor.BLACK);

	@Nullable
	private final DyeColor dye;

	ColorFilter(@Nullable DyeColor dye) {
		this.dye = dye;
	}

	@Nullable
	public DyeColor dye() {
		return dye;
	}

	public static ColorFilter of(DyeColor dye) {
		for (ColorFilter f : values()) {
			if (f.dye == dye) return f;
		}
		return ANY;
	}

	@Override
	public String asString() {
		return dye == null ? "any" : dye.getName();
	}
}
