package com.akitain.explorationreloaded.mixin.client.map;

import com.akitain.explorationreloaded.registry.item.MapBookItem;
import com.akitain.explorationreloaded.registry.item.MapStateData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @ModifyVariable(at = @At(value = "HEAD"), method = "renderFirstPersonMap", argsOnly = true)
    private ItemStack sneakySwap(ItemStack original) {
        if (original.getItem() instanceof MapBookItem mapBookItem) {
            assert client.player !=null;
            MapStateData nearestMap = mapBookItem.getNearestMap(original, client.world, client.player.getEntityPos());
            if (nearestMap == null) return original;
            ItemStack map = new ItemStack(Items.FILLED_MAP, 1);
            map.set(DataComponentTypes.MAP_ID, nearestMap.id);
            return map;
        }
        return original;
    }
}
