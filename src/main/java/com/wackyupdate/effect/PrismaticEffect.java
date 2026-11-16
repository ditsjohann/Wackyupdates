package com.wackyupdate.effect;

import com.wackyupdate.WackyUpdate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

/** Leaves a cycling rainbow trail behind you and gives +15% movement speed. */
public class PrismaticEffect extends StatusEffect {
	public PrismaticEffect() {
		super(StatusEffectCategory.BENEFICIAL, 0xff7ae8);
		addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, WackyUpdate.id("effect.prismatic"), 0.15,
			EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
		if (entity.getWorld() instanceof ServerWorld world) {
			float hue = (entity.age % 60) / 60f;
			int rgb = MathHelper.hsvToRgb(hue, 0.85f, 1.0f);
			Vector3f color = new Vector3f(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
			world.spawnParticles(new DustParticleEffect(color, 1.3f), entity.getX(), entity.getY() + 0.15, entity.getZ(),
				2, 0.2, 0.05, 0.2, 0.0);
		}
		return true;
	}
}
