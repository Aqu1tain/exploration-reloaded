package com.akitain.explorationreloaded.flight;

import com.akitain.explorationreloaded.ExplorationReloaded;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;

/**
 * Smokestack and Cloudskipper are found, not bought: they sit in the End, alongside the elytra they
 * belong to. Every chest here is one you only reach after beating the dragon.
 *
 * <p>Two weightings exist because Enchantment Overhaul adds its own book pool to the very same chests.
 * Stacking two full-strength pools would flood the End with books, so when it is installed ours steps
 * back and lets the combined rate land where a single pool would.
 */
public final class FlightLoot {

    private record BookLoot(String lootTable, int emptyWeight, int bookWeight) {}

    /** Weights used when we are the only mod touching these chests. */
    private static final List<BookLoot> VANILLA_LOOT = List.of(
            new BookLoot("chests/end_city_treasure", 60, 20),
            new BookLoot("chests/end_city", 80, 10)
    );

    /** Lighter weights for when Enchantment Overhaul is already adding books to the same chests. */
    private static final List<BookLoot> ENCHANTMENT_OVERHAUL_LOOT = List.of(
            new BookLoot("chests/end_city_treasure", 75, 12),
            new BookLoot("chests/end_city", 88, 6)
    );

    private static final List<ResourceKey<Enchantment>> ENCHANTMENTS =
            List.of(FlightEnchantments.SMOKESTACK, FlightEnchantments.CLOUDSKIPPER);

    private FlightLoot() {
    }

    public static void register() {
        boolean overhauled = FabricLoader.getInstance().isModLoaded("enchantment-overhaul");
        List<BookLoot> table = overhauled ? ENCHANTMENT_OVERHAUL_LOOT : VANILLA_LOOT;

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (key.identifier().getNamespace().equals(ExplorationReloaded.MOD_ID)) {
                return;
            }
            String path = key.identifier().getPath();
            HolderLookup<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
            for (BookLoot loot : table) {
                if (path.equals(loot.lootTable())) {
                    addBookPool(tableBuilder, enchantments, loot);
                    break;
                }
            }
        });
    }

    private static void addBookPool(LootTable.Builder tableBuilder, HolderLookup<Enchantment> registry, BookLoot loot) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1));
        pool.add(EmptyLootItem.emptyItem().setWeight(loot.emptyWeight()));
        for (ResourceKey<Enchantment> key : ENCHANTMENTS) {
            Holder<Enchantment> enchantment = registry.getOrThrow(key);
            pool.add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                    .apply(new SetEnchantmentsFunction.Builder()
                            .withEnchantment(enchantment, ConstantValue.exactly(1)))
                    .setWeight(loot.bookWeight()));
        }
        tableBuilder.withPool(pool);
    }
}
