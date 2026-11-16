package com.wackyupdate.registry;

import com.wackyupdate.WackyUpdate;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModParticles {
	public static final SimpleParticleType FIREFLY = Registry.register(Registries.PARTICLE_TYPE, WackyUpdate.id("firefly"), FabricParticleTypes.simple(true));
	public static final SimpleParticleType PRISM_SPARK = Registry.register(Registries.PARTICLE_TYPE, WackyUpdate.id("prism_spark"), FabricParticleTypes.simple(true));

	public static void init() {}

	private ModParticles() {}
}
