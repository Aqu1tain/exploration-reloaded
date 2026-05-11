package com.akitain.explorationreloaded.registry;

import com.akitain.explorationreloaded.ExplorationReloaded;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.List;
import java.util.function.Function;

public final class ExplorationItems {
    public static final Item DRAGON_FIREWORK_ROCKET = register("dragon_firework_rocket", FireworkRocketItem::new, new Item.Settings()
            .useCooldown(1.0F)
            .component(DataComponentTypes.FIREWORKS, new FireworksComponent(1, List.of())));
    public static final Item NAUTILUS_ARMOR = register("nautilus_armor", new Item.Settings().nautilusArmor(ArmorMaterials.ARMADILLO_SCUTE));

    private ExplorationItems() {
    }

    public static void register() {
    }

    private static Item register(String name, Item.Settings settings) {
        return register(keyOf(name), Item::new, settings);
    }

    private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        return register(keyOf(name), factory, settings);
    }

    private static Item register(RegistryKey<Item> key, Function<Item.Settings, Item> factory, Item.Settings settings) {
        Item item = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    private static RegistryKey<Item> keyOf(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, ExplorationReloaded.id(name));
    }
}
