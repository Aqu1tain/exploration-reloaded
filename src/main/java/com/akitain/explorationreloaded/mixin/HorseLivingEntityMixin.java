package com.akitain.explorationreloaded.mixin;

import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.waypoints.WaypointTransmitter;
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

    @Inject(method = "makeWaypointConnectionWith", at = @At("HEAD"), cancellable = true)
    private void onlyOwnerSeesHorseIcon(ServerPlayer player, CallbackInfoReturnable<Optional<WaypointTransmitter.Connection>> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof AbstractHorse horse)) {
            return;
        }

        EntityReference<LivingEntity> owner = horse.getOwnerReference();
        if (owner == null || !owner.matches(player) || horse.hasPassenger(player)) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
