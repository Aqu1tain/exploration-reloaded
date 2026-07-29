package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.flight.ElytraFlight;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Crouching on a campfire is how a Smokestack glider fills its charges, so it must not cook them. The
// player is riding the smoke column, not standing in the flames; everyone else still burns.
@Mixin(CampfireBlock.class)
public class CampfireChargingMixin {

    @WrapOperation(method = "entityInside", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean erSpareChargingGliders(Entity entity, DamageSource source, float amount, Operation<Boolean> original) {
        if (entity instanceof Player player && ElytraFlight.isChargingFromCampfire(player)) {
            return false;
        }
        return original.call(entity, source, amount);
    }
}
