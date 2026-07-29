package com.akitain.explorationreloaded.flight;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Jump is dead weight while gliding in vanilla, so it doubles as the boost key. Only the keypress is
 * sent; whether a charge is actually spent is the server's call.
 */
public final class FlightClient {

    private static boolean jumpWasDown;

    private FlightClient() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(FlightClient::tick);
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            jumpWasDown = false;
            return;
        }

        boolean jumpDown = client.options.keyJump.isDown();
        boolean pressed = jumpDown && !jumpWasDown;
        jumpWasDown = jumpDown;

        if (pressed && player.isFallFlying() && FlightState.charges(player) > 0) {
            ClientPlayNetworking.send(new FlightNetworking.SpendChargePayload());
        }
    }
}
