package com.akitain.explorationreloaded.flight;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/**
 * The camera banks into a turn while gliding, the way an aircraft does.
 *
 * <p>The roll is how far the player's heading has run ahead of a lagging copy of itself: turn hard and
 * the gap opens, hold a heading and it closes. The lagging copy advances once per tick, but the roll is
 * read against the <em>live</em> yaw every frame and the lag value is extrapolated across the frame, so
 * the bank tracks the mouse instead of stepping twenty times a second.
 */
public final class CameraRoll {

    /** How quickly the lagging heading catches up each tick. Lower means a longer, lazier bank. */
    private static final float CATCH_UP = 0.25F;
    /** Degrees of roll per degree of lag, before the speed factor. */
    private static final float ROLL_SCALE = 0.225F;
    private static final float MAX_ROLL_DEGREES = 40.0F;

    private static float laggingYaw;
    private static float previousLaggingYaw;

    private CameraRoll() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(CameraRoll::tick);
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            laggingYaw = previousLaggingYaw = 0.0F;
            return;
        }

        previousLaggingYaw = laggingYaw;
        // Stepped through the shortest arc so a turn across the -180/180 seam does not flip the camera.
        laggingYaw += Mth.degreesDifference(laggingYaw, player.getYRot()) * CATCH_UP;
    }

    /** Roll in radians for this frame, or zero when the player is not gliding in first person. */
    public static float rollRadians() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || !player.isFallFlying()) {
            return 0.0F;
        }
        if (client.options.getCameraType() != CameraType.FIRST_PERSON) {
            return 0.0F;
        }

        float partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        float lagging = laggingYaw + (laggingYaw - previousLaggingYaw) * partial;
        float lag = Mth.degreesDifference(lagging, player.getYRot());
        float speed = (float) player.getDeltaMovement().length();

        float degrees = Mth.clamp(lag * ROLL_SCALE * speed, -MAX_ROLL_DEGREES, MAX_ROLL_DEGREES);
        return degrees * Mth.DEG_TO_RAD;
    }
}
