package com.wackyupdate.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;

/** A tiny full-bright rainbow sparkle. */
@Environment(EnvType.CLIENT)
public class PrismSparkParticle extends SpriteBillboardParticle {
	private final SpriteProvider sprites;
	private final float hueOffset;

	protected PrismSparkParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, SpriteProvider sprites) {
		super(world, x, y, z, vx, vy, vz);
		this.sprites = sprites;
		velocityX = vx;
		velocityY = vy;
		velocityZ = vz;
		maxAge = 14 + random.nextInt(10);
		scale = 0.08f + random.nextFloat() * 0.05f;
		gravityStrength = 0f;
		hueOffset = random.nextFloat();
		setSpriteForAge(sprites);
	}

	@Override
	public void tick() {
		super.tick();
		setSpriteForAge(sprites);
		int rgb = MathHelper.hsvToRgb((hueOffset + age * 0.04f) % 1f, 0.7f, 1f);
		setColor(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
		alpha = 1f - (float) age / maxAge;
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	protected int getBrightness(float tint) {
		return LightmapTextureManager.MAX_LIGHT_COORDINATE;
	}

	@Environment(EnvType.CLIENT)
	public static class Factory implements ParticleFactory<SimpleParticleType> {
		private final SpriteProvider sprites;

		public Factory(SpriteProvider sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
			return new PrismSparkParticle(world, x, y, z, vx, vy, vz, sprites);
		}
	}
}
