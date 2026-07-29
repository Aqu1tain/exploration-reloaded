package com.akitain.explorationreloaded.flight;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/**
 * The camera banks into a turn while gliding, the way an aircraft does.
 *
 * <p>The roll comes from how far the player's yaw has run ahead of a smoothed copy of itself: turn
 * hard and the gap widens, hold a heading and it closes. Tracked per tick and interpolated at render
 * time so it does not stutter at low frame rates.
 */
public final class CameraRoll {

    /** How quickly the smoothed yaw catches up. Lower means a longer, lazier bank. */
    private static final float SMOOTHING = 0.25F;
    /** Degrees of roll per degree-per-tick of turn rate, before the speed factor. */
    private static final float ROLL_SCALE = 0.225F;
    private static final float MAX_ROLL_DEGREES = 40.0F;

    private static float smoothedYaw;
    private static float previousSmoothedYaw;
    private static float lastYaw;

    private CameraRoll() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(CameraRoll::tick);
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            smoothedYaw = previousSmoothedYaw = lastYaw = 0.0F;
            return;
        }

        lastYaw = player.getYRot();
        previousSmoothedYaw = smoothedYaw;
        // Unwrapped so a turn across the -180/180 seam does not snap the camera over.
        smoothedYaw += Mth.degreesDifference(smoothedYaw, lastYaw) * SMOOTHING;
    }

    /** Roll in degrees for this frame, or zero when the player is not gliding. */
    public static float rollDegrees(float partialTick) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || !player.isFallFlying() || client.options.getCameraType() != net.minecraft.client.CameraType.FIRST_PERSON) {
            return 0.0F;
        }

        float interpolated = Mth.lerp(partialTick, previousSmoothedYaw, smoothedYaw);
        float lag = Mth.degreesDifference(interpolated, lastYaw);
        float speed = (float) player.getDeltaMovement().length();
        return Mth.clamp(lag * ROLL_SCALE * speed, -MAX_ROLL_DEGREES, MAX_ROLL_DEGREES);
    }
}
