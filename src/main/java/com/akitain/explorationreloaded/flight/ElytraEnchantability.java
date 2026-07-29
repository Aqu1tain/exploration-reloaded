package com.akitain.explorationreloaded.flight;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantable;

/**
 * Vanilla gives the elytra a very low enchantment value, which was fine while it had nothing worth
 * rolling. Smokestack and Cloudskipper change that, so it is raised to compete with iron armour.
 */
public final class ElytraEnchantability {

    private static final int ENCHANTMENT_VALUE = 15;

    private ElytraEnchantability() {
    }

    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(context ->
                context.modify(Items.ELYTRA, builder ->
                        builder.set(DataComponents.ENCHANTABLE, new Enchantable(ENCHANTMENT_VALUE))));
    }
}
