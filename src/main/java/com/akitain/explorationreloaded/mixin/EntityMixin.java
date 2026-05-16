package com.akitain.explorationreloaded.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @ModifyVariable(method = "refreshDimensions", at = @At(value = "STORE"), ordinal = 1)
    private EntityDimensions shrinkHorseInBoat(EntityDimensions original) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof AbstractHorse && entity.isPassenger()) {
            return EntityDimensions.fixed(original.width() * 0.9F, original.height());
        }
        return original;
    }

    @Inject(method = "resetFallDistance", at = @At("HEAD"))
    private void triggerWhenPigsFly(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof Pig pig) || entity.fallDistance <= 9.5 || !pig.isVehicle()) {
            return;
        }
        if (pig.getControllingPassenger() instanceof ServerPlayer player) {
            CriteriaTriggers.CONSUME_ITEM.trigger(player, Items.SADDLE.getDefaultInstance());
        }
    }
}
