package com.akitain.explorationreloaded.registry.item;

import com.mojang.serialization.Codec;
import com.akitain.explorationreloaded.network.IntArray;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record MapBookAdditionsComponent(List<Integer> additions) {
    public static final MapBookAdditionsComponent DEFAULT = new MapBookAdditionsComponent(new ArrayList<>());

    public static final Codec<MapBookAdditionsComponent> CODEC = Codec.INT.listOf().xmap(MapBookAdditionsComponent::new, MapBookAdditionsComponent::additions);
    public static final StreamCodec<RegistryFriendlyByteBuf, MapBookAdditionsComponent> PACKET_CODEC = StreamCodec.composite(
            IntArray.LIST_CODEC,
            MapBookAdditionsComponent::additions,
            MapBookAdditionsComponent::new
    );
}
