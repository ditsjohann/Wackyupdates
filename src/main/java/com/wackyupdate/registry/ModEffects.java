package com.wackyupdate.registry;

import com.wackyupdate.WackyUpdate;
import com.wackyupdate.effect.PrismaticEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public final class ModEffects {
	public static final RegistryEntry<StatusEffect> PRISMATIC = Registry.registerReference(Registries.STATUS_EFFECT, WackyUpdate.id("prismatic"), new PrismaticEffect());

	public static void init() {}

	private ModEffects() {}
}
