package com.akitain.explorationreloaded.mixin.client.flight;

import com.akitain.explorationreloaded.flight.CameraRoll;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Camera.setRotation builds its quaternion with a hardcoded zero roll. Feeding that argument is the
 * whole trick: no extra matrix push, and everything downstream that reads the camera orientation —
 * frustum, view matrix, render state — banks with it.
 */
@Mixin(Camera.class)
public class CameraRollMixin {

    @ModifyArg(
            method = "setRotation",
            at = @At(value = "INVOKE", target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;"),
            index = 2)
    private float erBankIntoTurns(float roll) {
        return roll + CameraRoll.rollRadians();
    }
}
