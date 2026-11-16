package com.wackyupdate;

import com.wackyupdate.mechanic.RagdollHandler;
import com.wackyupdate.mechanic.SkateHandler;
import com.wackyupdate.registry.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WackyUpdate implements ModInitializer {
	public static final String MOD_ID = "wackyupdate";
	public static final Logger LOGGER = LoggerFactory.getLogger("WackyUpdate");

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModParticles.init();
		ModBlocks.init();
		ModBlockEntities.init();
		ModEntities.init();
		ModArmorMaterials.init();
		ModItems.init();
		ModEffects.init();
		ModAquaticBlocks.init();
		ModAquaticEntities.init();
		ModAquaticItems.init();
		ModItemGroups.init();
		ModWorldGen.init();
		SkateHandler.init();
		RagdollHandler.init();
		com.wackyupdate.aquatic.AquaticEcosystem.init();
		LOGGER.info("WackyUpdate loaded. Mind the gravel - and the piranhas.");
	}
}
