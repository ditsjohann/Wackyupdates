package com.wackyupdate.client;

import com.wackyupdate.WackyUpdate;
import com.wackyupdate.block.LightReceptorBlock;
import com.wackyupdate.block.PrismGlassBlock;
import net.minecraft.util.DyeColor;
import com.wackyupdate.client.mechanic.GlimmerNights;
import com.wackyupdate.client.mechanic.LensVision;
import com.wackyupdate.client.model.*;
import com.wackyupdate.client.particle.FireflyParticle;
import com.wackyupdate.client.particle.PrismSparkParticle;
import com.wackyupdate.client.render.PrismProjectorRenderer;
import com.wackyupdate.client.render.RagdollRenderer;
import com.wackyupdate.client.render.SimpleMobRenderer;
import com.wackyupdate.client.render.SkateGhoulRenderer;
import com.wackyupdate.item.PrismGlassItem;
import com.wackyupdate.registry.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class WackyUpdateClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
			ModBlocks.SPECULUM_CLUSTER, ModBlocks.LARGE_SPECULUM_BUD, ModBlocks.MEDIUM_SPECULUM_BUD, ModBlocks.SMALL_SPECULUM_BUD,
			ModBlocks.SPECULUM_LANTERN, ModBlocks.SLICKWOOD_SAPLING, ModBlocks.SKATE_RAIL, ModBlocks.BOOST_PAD,
			ModBlocks.SPECULUM_MIRROR, ModBlocks.LIGHT_RECEPTOR);
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SLICKWOOD_LEAVES, RenderLayer.getCutoutMipped());
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), ModBlocks.PRISM_GLASS, ModBlocks.FIREFLY_JAR, ModBlocks.PRISM_SPLITTER);

		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
			0xFF000000 | state.get(PrismGlassBlock.COLOR).getEntityColor(), ModBlocks.PRISM_GLASS);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) ->
			0xFF000000 | PrismGlassItem.getColor(stack).getEntityColor(), ModBlocks.PRISM_GLASS);

		// Light Receptor: the gem overlay shows the colour filter (white = any colour)
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
			DyeColor dye = state.get(LightReceptorBlock.FILTER).dye();
			return 0xFF000000 | (dye == null ? 0xF0FAFF : dye.getEntityColor());
		}, ModBlocks.LIGHT_RECEPTOR);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> 0xFFF0FAFF, ModBlocks.LIGHT_RECEPTOR);

		ParticleFactoryRegistry.getInstance().register(ModParticles.FIREFLY, FireflyParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.PRISM_SPARK, PrismSparkParticle.Factory::new);

		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.GRAVEL_MITE, GravelMiteModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.PRISM_SNAIL, PrismSnailModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.ROLLYPIN, RollypinModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.GLOWTOAD, GlowtoadModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.SHARDLING, ShardlingModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.RAGDOLL_WIDE, RagdollModels::wide);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.RAGDOLL_SKINNY, RagdollModels::skinny);

		EntityRendererRegistry.register(ModEntities.GRAVEL_MITE, ctx -> new SimpleMobRenderer<>(ctx,
			new GravelMiteModel(ctx.getPart(ModModelLayers.GRAVEL_MITE)), 0.2f, tex("gravel_mite")));
		EntityRendererRegistry.register(ModEntities.PRISM_SNAIL, ctx -> new SimpleMobRenderer<>(ctx,
			new PrismSnailModel(ctx.getPart(ModModelLayers.PRISM_SNAIL)), 0.3f, tex("prism_snail")));
		EntityRendererRegistry.register(ModEntities.ROLLYPIN, ctx -> new SimpleMobRenderer<>(ctx,
			new RollypinModel(ctx.getPart(ModModelLayers.ROLLYPIN)), 0.35f, tex("rollypin")));
		EntityRendererRegistry.register(ModEntities.GLOWTOAD, ctx -> new SimpleMobRenderer<>(ctx,
			new GlowtoadModel(ctx.getPart(ModModelLayers.GLOWTOAD)), 0.3f, tex("glowtoad")).withGlow(tex("glowtoad_glow")));
		EntityRendererRegistry.register(ModEntities.SHARDLING, ctx -> new SimpleMobRenderer<>(ctx,
			new ShardlingModel(ctx.getPart(ModModelLayers.SHARDLING)), 0.5f, tex("shardling")).withGlow(tex("shardling_glow")));
		EntityRendererRegistry.register(ModEntities.SKATE_GHOUL, SkateGhoulRenderer::new);
		EntityRendererRegistry.register(ModEntities.RAGDOLL, RagdollRenderer::new);

		BlockEntityRendererFactories.register(ModBlockEntities.PRISM_PROJECTOR, PrismProjectorRenderer::new);

		LensVision.init();
		GlimmerNights.init();
	}

	private static net.minecraft.util.Identifier tex(String name) {
		return WackyUpdate.id("textures/entity/" + name + ".png");
	}
}
