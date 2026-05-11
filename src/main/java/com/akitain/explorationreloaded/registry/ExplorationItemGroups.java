package com.akitain.explorationreloaded.registry;

import com.akitain.explorationreloaded.ExplorationReloaded;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ExplorationItemGroups {
    public static final ItemGroup EXPLORATION_RELOADED = FabricItemGroup.builder()
            .displayName(Text.translatable("itemgroup.exploration-reloaded"))
            .icon(() -> new ItemStack(ExplorationItems.DRAGON_FIREWORK_ROCKET))
            .entries((displayContext, entries) -> {
                entries.add(ExplorationItems.DRAGON_FIREWORK_ROCKET);
                entries.add(ExplorationItems.NAUTILUS_ARMOR);
            })
            .build();

    private ExplorationItemGroups() {
    }

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, ExplorationReloaded.id("exploration_reloaded"), EXPLORATION_RELOADED);
    }
}
