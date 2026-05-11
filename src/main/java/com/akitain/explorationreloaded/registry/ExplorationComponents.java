package com.akitain.explorationreloaded.registry;

import com.akitain.explorationreloaded.ExplorationReloaded;
import com.akitain.explorationreloaded.registry.other.BaitComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.UnaryOperator;

public final class ExplorationComponents {
    public static final ComponentType<BaitComponent> BAIT_POWER = register("bait_power", builder -> builder
            .codec(BaitComponent.CODEC)
            .packetCodec(BaitComponent.PACKET_CODEC)
            .cache());

    private ExplorationComponents() {
    }

    public static void register() {
    }

    private static <T> ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, ExplorationReloaded.id(name), builderOperator.apply(ComponentType.builder()).build());
    }
}
