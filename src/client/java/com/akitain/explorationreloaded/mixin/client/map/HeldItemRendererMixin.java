package com.akitain.explorationreloaded.mixin.client.map;

import com.akitain.explorationreloaded.registry.item.MapBookItem;
import com.akitain.explorationreloaded.registry.item.MapStateData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemInHandRenderer.class)
public class HeldItemRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyVariable(at = @At(value = "HEAD"), method = "renderMap", argsOnly = true)
    private ItemStack sneakySwap(ItemStack original) {
        if (original.getItem() instanceof MapBookItem mapBookItem) {
            assert minecraft.player !=null;
            MapStateData nearestMap = mapBookItem.getNearestMap(original, minecraft.level, minecraft.player.position());
            if (nearestMap == null) return original;
            ItemStack map = new ItemStack(Items.FILLED_MAP, 1);
            map.set(DataComponents.MAP_ID, nearestMap.id);
            return map;
        }
        return original;
    }
}
