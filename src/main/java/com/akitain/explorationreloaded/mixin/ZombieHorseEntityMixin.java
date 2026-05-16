package com.akitain.explorationreloaded.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.ZombieHorse;

@Mixin(ZombieHorse.class)
public abstract class ZombieHorseEntityMixin {
    @Inject(method = "randomizeReinforcementsChance", at = @At("TAIL"))
    private void randomiseAttributes(RandomSource random, CallbackInfo ci) {
        ZombieHorse horse = (ZombieHorse) (Object) this;
        AttributeInstance health = Objects.requireNonNull(horse.getAttribute(Attributes.MAX_HEALTH));
        health.setBaseValue(getChildHealthBonus(random::nextInt));

        AttributeInstance movementSpeed = Objects.requireNonNull(horse.getAttribute(Attributes.MOVEMENT_SPEED));
        movementSpeed.setBaseValue(getChildMovementSpeedBonus(random::nextDouble));
    }

    @Redirect(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/equine/ZombieHorse;isTamed()Z"))
    private boolean allowRiding(ZombieHorse horse) {
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
