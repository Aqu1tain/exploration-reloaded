package com.akitain.explorationreloaded.registry;

import com.akitain.explorationreloaded.ExplorationReloaded;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.function.Function;

public final class ExplorationItems {
    public static final Item NAUTILUS_ARMOR = register("nautilus_armor", new Item.Settings().nautilusArmor(ArmorMaterials.ARMADILLO_SCUTE));

    private ExplorationItems() {
    }

    public static void register() {
    }

    private static Item register(String name, Item.Settings settings) {
        return register(keyOf(name), Item::new, settings);
    }

    private static Item register(RegistryKey<Item> key, Function<Item.Settings, Item> factory, Item.Settings settings) {
        Item item = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    private static RegistryKey<Item> keyOf(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, ExplorationReloaded.id(name));
    }
}
