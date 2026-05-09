package com.akitain.fixedexploration;

import com.akitain.fixedexploration.registry.ExplorationRegistries;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedExploration implements ModInitializer {
    public static final String MOD_ID = "fixed-exploration";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ExplorationRegistries.register();
        LOGGER.info("Fixed Exploration loaded");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
