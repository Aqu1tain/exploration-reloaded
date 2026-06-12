package com.akitain.explorationreloaded.mixin;

import java.util.Optional;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.waypoint.ServerWaypoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class HorseLivingEntityMixin {
    @ModifyConstant(method = "getAttackDistanceScalingFactor", constant = @Constant(doubleValue = 1.0))
    private double makeZombieHorseSneaky(double constant) {
        LivingEntity entity = (LivingEntity) (Object) this;
        return entity.getVehicle() instanceof ZombieHorseEntity ? 0.5 : 1.0;
    }

    @Inject(method = "canUseSlot", at = @At("HEAD"), cancellable = true)
    private void allowMuleArmorSlot(EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof MuleEntity) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "createTracker", at = @At("HEAD"), cancellable = true)
    private void onlyOwnerSeesHorseIcon(ServerPlayerEntity player, CallbackInfoReturnable<Optional<ServerWaypoint.WaypointTracker>> cir) {
        if (!((Object) this instanceof AbstractHorseEntity horse)) {
            return;
        }

        LazyEntityReference<LivingEntity> owner = horse.getOwnerReference();
        boolean playerIsOwner = owner != null && owner.getUuid().equals(player.getUuid());
        if (!playerIsOwner || horse.hasPassenger(player)) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
