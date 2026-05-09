package com.akitain.explorationreloaded.registry;

import com.akitain.explorationreloaded.ExplorationReloaded;
import com.akitain.explorationreloaded.world.loot.ExplorationCompassFunction;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public final class ExplorationRegistries {
    public static final Holder<MapDecorationType> OUTPOST = mapDecoration("outpost", MapColor.TERRACOTTA_BROWN.col);
    public static final Holder<MapDecorationType> RUINED_PORTAL = mapDecoration("ruined_portal", MapColor.COLOR_PURPLE.col);
    public static final Holder<MapDecorationType> TRAIL_RUINS = mapDecoration("trail_ruins", MapColor.COLOR_LIGHT_GRAY.col);

    static {
        lootFunction("exploration_compass", ExplorationCompassFunction.CODEC);
    }

    private ExplorationRegistries() {
    }

    public static void register() {
    }

    private static Holder<MapDecorationType> mapDecoration(String name, int color) {
        ResourceKey<MapDecorationType> key = ResourceKey.create(BuiltInRegistries.MAP_DECORATION_TYPE.key(), ExplorationReloaded.id(name));
        Identifier assetId = ExplorationReloaded.id(name);
        return Registry.registerForHolder(
                BuiltInRegistries.MAP_DECORATION_TYPE,
                key,
                new MapDecorationType(assetId, true, color, true, false)
        );
    }

    private static void lootFunction(String name, MapCodec<? extends LootItemFunction> codec) {
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, ExplorationReloaded.id(name), codec);
    }
}
