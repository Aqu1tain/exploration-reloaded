package com.akitain.explorationreloaded;

import com.akitain.explorationreloaded.map_book.MapBookFilledProperty;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.item.property.bool.BooleanProperties;

public class ExplorationReloadedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientMapBookNetworking.register();
        BooleanProperties.ID_MAPPER.put(ExplorationReloaded.id("map_book/filled"), MapBookFilledProperty.CODEC);
    }
}
