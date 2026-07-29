package com.akitain.explorationreloaded.flight;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Real key mappings rather than a hardcoded jump listener: Do a Barrel Roll drives elytra activation
 * off the jump key in its hybrid modes, so a player running both needs to be able to move ours.
 */
public final class FlightKeys {

    public static final KeyMapping BOOST = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.exploration-reloaded.smokestack_boost",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SPACE,
            KeyMapping.Category.MOVEMENT));

    public static final KeyMapping FOLD_WINGS = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.exploration-reloaded.fold_wings",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            KeyMapping.Category.MOVEMENT));

    private FlightKeys() {
    }

    public static void register() {
    }
}
