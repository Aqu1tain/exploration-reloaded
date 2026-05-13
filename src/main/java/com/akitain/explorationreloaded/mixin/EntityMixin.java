package com.akitain.explorationreloaded.mixin;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @ModifyVariable(method = "calculateDimensions", at = @At(value = "STORE"), ordinal = 1)
    private EntityDimensions shrinkHorseInBoat(EntityDimensions original) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof AbstractHorseEntity && entity.hasVehicle()) {
            return EntityDimensions.fixed(original.width() * 0.9F, original.height());
        }
        return original;
    }

    @Inject(method = "onLanding", at = @At("HEAD"))
    private void triggerWhenPigsFly(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof PigEntity pig) || entity.fallDistance <= 9.5 || !pig.hasPassengers()) {
            return;
        }
        if (pig.getControllingPassenger() instanceof ServerPlayerEntity player) {
            Criteria.CONSUME_ITEM.trigger(player, Items.SADDLE.getDefaultStack());
        }
    }
}
