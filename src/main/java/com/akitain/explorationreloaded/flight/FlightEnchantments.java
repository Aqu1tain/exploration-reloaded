package com.akitain.explorationreloaded.flight;

import com.akitain.explorationreloaded.ExplorationReloaded;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * Levels are read straight off the equipped glider rather than mirrored into custom attributes: the
 * enchantments are pure data, and nothing needs syncing that the equipment packet does not already carry.
 */
public final class FlightEnchantments {

    public static final ResourceKey<Enchantment> SMOKESTACK =
            ResourceKey.create(Registries.ENCHANTMENT, ExplorationReloaded.id("smokestack"));
    public static final ResourceKey<Enchantment> CLOUDSKIPPER =
            ResourceKey.create(Registries.ENCHANTMENT, ExplorationReloaded.id("cloudskipper"));

    private FlightEnchantments() {
    }

    public static int smokestackLevel(LivingEntity entity) {
        return level(entity, SMOKESTACK);
    }

    public static int cloudskipperLevel(LivingEntity entity) {
        return level(entity, CLOUDSKIPPER);
    }

    private static int level(LivingEntity entity, ResourceKey<Enchantment> key) {
        Level level = entity.level();
        return level.registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(key))
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, entity.getItemBySlot(EquipmentSlot.CHEST)))
                .orElse(0);
    }
}
