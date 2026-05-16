package com.akitain.explorationreloaded.registry;

import com.akitain.explorationreloaded.ExplorationReloaded;
import com.akitain.explorationreloaded.registry.item.MapBookAdditionsComponent;
import com.akitain.explorationreloaded.registry.other.BaitComponent;
import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ExplorationComponents {
    public static final DataComponentType<BaitComponent> BAIT_POWER = register("bait_power", builder -> builder
            .persistent(BaitComponent.CODEC)
            .networkSynchronized(BaitComponent.PACKET_CODEC)
            .cacheEncoding());
    public static final DataComponentType<MapBookAdditionsComponent> MAP_BOOK_ADDITIONS = register("map_book_additions", builder -> builder
            .persistent(MapBookAdditionsComponent.CODEC)
            .networkSynchronized(MapBookAdditionsComponent.PACKET_CODEC)
            .cacheEncoding());

    private ExplorationComponents() {
    }

    public static void register() {
    }

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ExplorationReloaded.id(name), builderOperator.apply(DataComponentType.builder()).build());
    }
}
