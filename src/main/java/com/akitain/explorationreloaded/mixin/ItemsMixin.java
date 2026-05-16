package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.registry.ExplorationComponents;
import com.akitain.explorationreloaded.registry.other.BaitComponent;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import static net.minecraft.world.item.Items.registerItem;

@Mixin(Items.class)
public class ItemsMixin {
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Items;registerItem(Ljava/lang/String;Lnet/minecraft/world/item/Item$Properties;)Lnet/minecraft/world/item/Item;", ordinal = 0), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/item/Items;POTION:Lnet/minecraft/world/item/Item;")))
    private static Item spiderEyeBait(String id, Item.Properties settings) {
        return registerItem("spider_eye", new Item.Properties().food(Foods.SPIDER_EYE, Consumables.SPIDER_EYE).component(ExplorationComponents.BAIT_POWER, new BaitComponent(1)));
    }

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Items;registerItem(Ljava/lang/String;)Lnet/minecraft/world/item/Item;", ordinal = 0), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/item/Items;SPIDER_EYE:Lnet/minecraft/world/item/Item;")))
    private static Item fermentedSpiderEyeBait(String id) {
        return registerItem("fermented_spider_eye", new Item.Properties().component(ExplorationComponents.BAIT_POWER, new BaitComponent(2)));
    }
}
