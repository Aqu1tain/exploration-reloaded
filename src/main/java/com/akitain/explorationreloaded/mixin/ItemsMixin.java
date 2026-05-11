package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.registry.ExplorationComponents;
import com.akitain.explorationreloaded.registry.other.BaitComponent;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import static net.minecraft.item.Items.register;

@Mixin(Items.class)
public class ItemsMixin {
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Items;register(Ljava/lang/String;Lnet/minecraft/item/Item$Settings;)Lnet/minecraft/item/Item;", ordinal = 0), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/item/Items;POTION:Lnet/minecraft/item/Item;")))
    private static Item spiderEyeBait(String id, Item.Settings settings) {
        return register("spider_eye", new Item.Settings().food(FoodComponents.SPIDER_EYE, ConsumableComponents.SPIDER_EYE).component(ExplorationComponents.BAIT_POWER, new BaitComponent(1)));
    }

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Items;register(Ljava/lang/String;)Lnet/minecraft/item/Item;", ordinal = 0), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/item/Items;SPIDER_EYE:Lnet/minecraft/item/Item;")))
    private static Item fermentedSpiderEyeBait(String id) {
        return register("fermented_spider_eye", new Item.Settings().component(ExplorationComponents.BAIT_POWER, new BaitComponent(2)));
    }
}
