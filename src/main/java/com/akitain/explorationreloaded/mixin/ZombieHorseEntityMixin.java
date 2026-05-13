package com.akitain.explorationreloaded.mixin;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;

@Mixin(ZombieHorseEntity.class)
public abstract class ZombieHorseEntityMixin {
    @Inject(method = "initAttributes", at = @At("TAIL"))
    private void randomiseAttributes(Random random, CallbackInfo ci) {
        ZombieHorseEntity horse = (ZombieHorseEntity) (Object) this;
        EntityAttributeInstance health = Objects.requireNonNull(horse.getAttributeInstance(EntityAttributes.MAX_HEALTH));
        health.setBaseValue(getChildHealthBonus(random::nextInt));

        EntityAttributeInstance movementSpeed = Objects.requireNonNull(horse.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED));
        movementSpeed.setBaseValue(getChildMovementSpeedBonus(random::nextDouble));
    }

    @Redirect(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/ZombieHorseEntity;isTame()Z"))
    private boolean allowRiding(ZombieHorseEntity horse) {
        return true;
    }

    @Unique
    private float getChildHealthBonus(IntUnaryOperator randomIntGetter) {
        return 15.0F + randomIntGetter.applyAsInt(8) + randomIntGetter.applyAsInt(9);
    }

    @Unique
    private double getChildMovementSpeedBonus(DoubleSupplier randomDoubleGetter) {
        return (0.44999998807907104 + randomDoubleGetter.getAsDouble() * 0.3 + randomDoubleGetter.getAsDouble() * 0.3 + randomDoubleGetter.getAsDouble() * 0.3) * 0.25;
    }
}
