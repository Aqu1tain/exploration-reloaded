package com.akitain.explorationreloaded.flight;

import com.akitain.explorationreloaded.ExplorationReloaded;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;

/**
 * The client tells the server it wants to spend a charge; the server decides whether it may. Nothing
 * about the boost is trusted from the client beyond the intent to press the key.
 */
public final class FlightNetworking {

    public record SpendChargePayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SpendChargePayload> ID =
                new CustomPacketPayload.Type<>(ExplorationReloaded.id("spend_charge"));
        public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, SpendChargePayload> CODEC =
                StreamCodec.unit(new SpendChargePayload());

        @Override
        public CustomPacketPayload.Type<SpendChargePayload> type() {
            return ID;
        }
    }

    private FlightNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(SpendChargePayload.ID, SpendChargePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SpendChargePayload.ID, (payload, context) -> {
            var player = context.player();
            if (player.level() instanceof ServerLevel level) {
                ElytraFlight.spendCharge(level, player);
            }
        });
    }
}
