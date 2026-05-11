package com.akitain.explorationreloaded;

import com.akitain.explorationreloaded.registry.ExplorationRegistries;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExplorationReloaded implements ModInitializer {
    public static final String MOD_ID = "exploration-reloaded";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ExplorationRegistries.register();
        LOGGER.info("Exploration Reloaded loaded");
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
