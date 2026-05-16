package com.akitain.explorationreloaded.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import com.akitain.explorationreloaded.ExplorationReloaded;

public record MapBookOpenPayload(ItemStack itemStack) implements CustomPacketPayload {
    public static final Type<MapBookOpenPayload> PACKET_ID = new Type<>(ExplorationReloaded.id("map_book_open"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MapBookOpenPayload> PACKET_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            MapBookOpenPayload::itemStack,
            MapBookOpenPayload::new
    );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(PACKET_ID, PACKET_CODEC);
    }
}
