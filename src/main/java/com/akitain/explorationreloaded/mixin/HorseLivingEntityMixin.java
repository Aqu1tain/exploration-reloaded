package com.akitain.explorationreloaded.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class HorseLivingEntityMixin {
    @ModifyConstant(method = "getVisibilityPercent", constant = @Constant(doubleValue = 1.0))
    private double makeZombieHorseSneaky(double constant) {
        LivingEntity entity = (LivingEntity) (Object) this;
        Entity vehicle = entity.getVehicle();
        if (vehicle instanceof ZombieHorse) {
            return 0.5;
        }
        return 1.0;
    }

    @Inject(method = "canUseSlot", at = @At("HEAD"), cancellable = true)
    private void allowMuleArmorSlot(EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Mule) {
            cir.setReturnValue(true);
        }
    }
}
