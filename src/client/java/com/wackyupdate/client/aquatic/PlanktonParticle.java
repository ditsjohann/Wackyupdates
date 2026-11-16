package com.wackyupdate.client.aquatic;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;

/** A tiny blue-green speck of bioluminescent plankton that twinkles and drifts. */
public class PlanktonParticle extends SpriteBillboardParticle {
	private final float phase;

	protected PlanktonParticle(ClientWorld world, double x, double y, double z, double vy, SpriteProvider sprites) {
		super(world, x, y, z);
		setSprite(sprites);
		maxAge = 40 + random.nextInt(60);
		scale = 0.04f + random.nextFloat() * 0.04f;
		gravityStrength = 0f;
		velocityX = (random.nextFloat() - 0.5f) * 0.01f;
		velocityY = vy + (random.nextFloat() - 0.5f) * 0.005f;
		velocityZ = (random.nextFloat() - 0.5f) * 0.01f;
		phase = random.nextFloat() * 6.28f;
		float hueShift = random.nextFloat();
		setColor(0.2f + hueShift * 0.3f, 0.8f + hueShift * 0.2f, 1.0f);
		alpha = 0f;
	}

	@Override
	public void tick() {
		super.tick();
		float life = age / (float) maxAge;
		float fade = life < 0.2f ? life / 0.2f : (1f - life) / 0.8f;
		alpha = MathHelper.clamp(fade * (0.6f + 0.4f * MathHelper.sin(age * 0.4f + phase)), 0f, 1f);
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	protected int getBrightness(float tint) {
		return LightmapTextureManager.MAX_LIGHT_COORDINATE;
	}

	public static class Factory implements ParticleFactory<SimpleParticleType> {
		private final SpriteProvider sprites;

		public Factory(SpriteProvider sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
			return new PlanktonParticle(world, x, y, z, vy, sprites);
		}
	}
}
