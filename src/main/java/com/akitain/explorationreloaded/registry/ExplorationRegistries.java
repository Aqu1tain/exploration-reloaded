package com.akitain.explorationreloaded.registry;

import com.akitain.explorationreloaded.ExplorationReloaded;
import com.akitain.explorationreloaded.world.loot.ExplorationCompassFunction;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.MapColor;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class ExplorationRegistries {
    public static final RegistryEntry<MapDecorationType> OUTPOST = mapDecoration("outpost", MapColor.BROWN.color);
    public static final RegistryEntry<MapDecorationType> RUINED_PORTAL = mapDecoration("ruined_portal", MapColor.PURPLE.color);
    public static final RegistryEntry<MapDecorationType> TRAIL_RUINS = mapDecoration("trail_ruins", MapColor.LIGHT_GRAY.color);

    public static final LootFunctionType<ExplorationCompassFunction> EXPLORATION_COMPASS = lootFunction("exploration_compass", ExplorationCompassFunction.CODEC);

    private ExplorationRegistries() {
    }

    public static void register() {
    }

    private static RegistryEntry<MapDecorationType> mapDecoration(String name, int color) {
        RegistryKey<MapDecorationType> key = RegistryKey.of(RegistryKeys.MAP_DECORATION_TYPE, ExplorationReloaded.id(name));
        Identifier assetId = ExplorationReloaded.id(name);
        MapDecorationType decorationType = new MapDecorationType(assetId, true, color, true, false);
        return Registry.registerReference(Registries.MAP_DECORATION_TYPE, key, decorationType);
    }

    private static <T extends LootFunction> LootFunctionType<T> lootFunction(String name, MapCodec<T> codec) {
        return Registry.register(Registries.LOOT_FUNCTION_TYPE, ExplorationReloaded.id(name), new LootFunctionType<>(codec));
    }
}
