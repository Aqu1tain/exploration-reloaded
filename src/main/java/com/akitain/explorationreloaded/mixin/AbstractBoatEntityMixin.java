package com.akitain.explorationreloaded.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBoatEntity.class)
public class AbstractBoatEntityMixin {
    @Inject(method = "isSmallerThanBoat", at = @At("HEAD"), cancellable = true)
    private void allowHorseInBoat(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof AbstractHorseEntity) {
            cir.setReturnValue(true);
        }
    }
}
