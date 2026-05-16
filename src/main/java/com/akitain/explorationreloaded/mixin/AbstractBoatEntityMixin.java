package com.akitain.explorationreloaded.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBoat.class)
public class AbstractBoatEntityMixin {
    @Inject(method = "hasEnoughSpaceFor", at = @At("HEAD"), cancellable = true)
    private void allowHorseInBoat(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof AbstractHorse) {
            cir.setReturnValue(true);
        }
    }
}
