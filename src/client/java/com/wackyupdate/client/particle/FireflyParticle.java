package com.wackyupdate.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;

/** A softly blinking firefly that wanders lazily, then fades away. */
@Environment(EnvType.CLIENT)
public class FireflyParticle extends SpriteBillboardParticle {
	private final float phase;
	private final float blinkSpeed;

	protected FireflyParticle(ClientWorld world, double x, double y, double z, SpriteProvider sprites) {
		super(world, x, y, z);
		setSprite(sprites);
		maxAge = 80 + random.nextInt(80);
		scale = 0.06f + random.nextFloat() * 0.04f;
		gravityStrength = 0f;
		collidesWithWorld = true;
		phase = random.nextFloat() * 6.28f;
		blinkSpeed = 0.12f + random.nextFloat() * 0.1f;
		float g = 0.85f + random.nextFloat() * 0.15f;
		setColor(0.75f + random.nextFloat() * 0.25f, g, 0.25f + random.nextFloat() * 0.2f);
		velocityX = (random.nextFloat() - 0.5f) * 0.02f;
		velocityY = (random.nextFloat() - 0.5f) * 0.01f;
		velocityZ = (random.nextFloat() - 0.5f) * 0.02f;
		alpha = 0f;
	}

	@Override
	public void tick() {
		super.tick();
		// lazy wandering
		velocityX += (random.nextFloat() - 0.5f) * 0.004f;
		velocityY += (random.nextFloat() - 0.5f) * 0.003f;
		velocityZ += (random.nextFloat() - 0.5f) * 0.004f;
		velocityX = MathHelper.clamp(velocityX, -0.03, 0.03);
		velocityY = MathHelper.clamp(velocityY, -0.015, 0.015);
		velocityZ = MathHelper.clamp(velocityZ, -0.03, 0.03);
		float life = age / (float) maxAge;
		float fade = life < 0.15f ? life / 0.15f : life > 0.8f ? (1f - life) / 0.2f : 1f;
		float blink = 0.55f + 0.45f * MathHelper.sin(age * blinkSpeed + phase);
		alpha = MathHelper.clamp(fade * blink, 0f, 1f);
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
			return new FireflyParticle(world, x, y, z, sprites);
		}
	}
}
