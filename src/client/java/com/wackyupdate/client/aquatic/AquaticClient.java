package com.wackyupdate.client.aquatic;

import com.wackyupdate.client.render.SimpleMobRenderer;
import com.wackyupdate.registry.ModAquaticBlocks;
import com.wackyupdate.registry.ModAquaticEntities;
import com.wackyupdate.registry.ModParticles;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

import static com.wackyupdate.client.aquatic.AquaticRenderers.layer;
import static com.wackyupdate.client.aquatic.AquaticRenderers.tex;

/** Client setup for the aquatic update. */
public final class AquaticClient {
	public static void init() {
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), ModAquaticBlocks.GLOW_ANEMONE, ModAquaticBlocks.ANGLER_LANTERN);
		BlockRenderLayerMap.INSTANCE.putBlock(ModAquaticBlocks.JELLY_LAMP, RenderLayer.getTranslucent());
		ParticleFactoryRegistry.getInstance().register(ModParticles.PLANKTON, PlanktonParticle.Factory::new);

		EntityModelLayerRegistry.registerModelLayer(layer("glimmerfin"), AquaticModels.Glimmerfin::data);
		EntityModelLayerRegistry.registerModelLayer(layer("sunscale_carp"), AquaticModels.Carp::data);
		EntityModelLayerRegistry.registerModelLayer(layer("sea_bunny"), AquaticModels.SeaBunny::data);
		EntityModelLayerRegistry.registerModelLayer(layer("hermit_crab"), AquaticModels.HermitCrab::data);
		EntityModelLayerRegistry.registerModelLayer(layer("seahorse"), AquaticModels.Seahorse::data);
		EntityModelLayerRegistry.registerModelLayer(layer("drift_jelly"), AquaticModels.DriftJelly::data);
		EntityModelLayerRegistry.registerModelLayer(layer("stingray"), AquaticModels.Stingray::data);
		EntityModelLayerRegistry.registerModelLayer(layer("piranha"), AquaticModels.Piranha::data);
		EntityModelLayerRegistry.registerModelLayer(layer("reef_barracuda"), AquaticModels.Barracuda::data);
		EntityModelLayerRegistry.registerModelLayer(layer("deep_angler"), AquaticModels.Angler::data);

		EntityRendererRegistry.register(ModAquaticEntities.GLIMMERFIN, AquaticRenderers.GlimmerfinRenderer::new);
		EntityRendererRegistry.register(ModAquaticEntities.SUNSCALE_CARP, ctx -> new AquaticRenderers.FishRenderer<>(ctx,
			new AquaticModels.Carp(ctx.getPart(layer("sunscale_carp"))), 0.3f, tex("sunscale_carp")));
		EntityRendererRegistry.register(ModAquaticEntities.SEA_BUNNY, ctx -> new SimpleMobRenderer<>(ctx,
			new AquaticModels.SeaBunny(ctx.getPart(layer("sea_bunny"))), 0.15f, tex("sea_bunny")));
		EntityRendererRegistry.register(ModAquaticEntities.HERMIT_CRAB, ctx -> new SimpleMobRenderer<>(ctx,
			new AquaticModels.HermitCrab(ctx.getPart(layer("hermit_crab"))), 0.3f, tex("hermit_crab")));
		EntityRendererRegistry.register(ModAquaticEntities.SEAHORSE, ctx -> new AquaticRenderers.FishRenderer<>(ctx,
			new AquaticModels.Seahorse(ctx.getPart(layer("seahorse"))), 0.15f, tex("seahorse")));
		EntityRendererRegistry.register(ModAquaticEntities.DRIFT_JELLY, AquaticRenderers.DriftJellyRenderer::new);
		EntityRendererRegistry.register(ModAquaticEntities.STINGRAY, ctx -> new SimpleMobRenderer<>(ctx,
			new AquaticModels.Stingray(ctx.getPart(layer("stingray"))), 0.5f, tex("stingray")));
		EntityRendererRegistry.register(ModAquaticEntities.PIRANHA, ctx -> new AquaticRenderers.FishRenderer<>(ctx,
			new AquaticModels.Piranha(ctx.getPart(layer("piranha"))), 0.2f, tex("piranha")).withGlow(tex("piranha_glow")));
		EntityRendererRegistry.register(ModAquaticEntities.REEF_BARRACUDA, ctx -> new AquaticRenderers.FishRenderer<>(ctx,
			new AquaticModels.Barracuda(ctx.getPart(layer("reef_barracuda"))), 0.35f, tex("reef_barracuda")));
		EntityRendererRegistry.register(ModAquaticEntities.DEEP_ANGLER, ctx -> new SimpleMobRenderer<>(ctx,
			new AquaticModels.Angler(ctx.getPart(layer("deep_angler"))), 0.6f, tex("deep_angler")).withGlow(tex("deep_angler_glow")));

		GlowingSeas.init();
	}

	private AquaticClient() {}
}
