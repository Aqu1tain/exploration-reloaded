package com.akitain.explorationreloaded.map_book;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.akitain.explorationreloaded.registry.ExplorationComponents;
import org.jetbrains.annotations.Nullable;

public class MapBookFilledProperty implements ConditionalItemModelProperty {
    public static MapCodec<MapBookFilledProperty> CODEC  = MapCodec.unit(new MapBookFilledProperty());

    @Override
    public MapCodec<MapBookFilledProperty> type() {
        return CODEC;
    }

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed,
                        ItemDisplayContext displayContext) {
        return stack.has(DataComponents.MAP_ID) || stack.has(ExplorationComponents.MAP_BOOK_ADDITIONS);
    }
}
