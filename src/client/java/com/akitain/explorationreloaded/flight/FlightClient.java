package com.akitain.explorationreloaded.flight;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** Only the keypress travels; whether anything happens is the server's call. */
public final class FlightClient {

    private FlightClient() {
    }

    public static void register() {
        FlightKeys.register();
        ClientTickEvents.END_CLIENT_TICK.register(FlightClient::tick);
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || !player.isFallFlying()) {
            return;
        }

        while (FlightKeys.BOOST.consumeClick()) {
            ClientPlayNetworking.send(new FlightNetworking.SpendChargePayload());
        }
        while (FlightKeys.FOLD_WINGS.consumeClick()) {
            ClientPlayNetworking.send(new FlightNetworking.FoldWingsPayload());
        }
    }
}
