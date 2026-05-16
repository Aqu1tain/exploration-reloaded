package com.akitain.explorationreloaded.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.level.Level;

@Mixin(AbstractChestedHorse.class)
public abstract class AbstractDonkeyEntityMixin extends AbstractHorse {
    protected AbstractDonkeyEntityMixin(EntityType<? extends AbstractHorse> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "getInventoryColumns", at = @At("HEAD"), cancellable = true)
    private void reduceMuleInventoryColumns(CallbackInfoReturnable<Integer> cir) {
        if ((AbstractChestedHorse) (Object) this instanceof Mule mule) {
            cir.setReturnValue(mule.hasChest() ? 3 : 0);
        }
    }

    @Inject(method = "randomizeAttributes", at = @At("TAIL"))
    private void randomiseDonkeyAttributes(RandomSource random, CallbackInfo ci) {
        AbstractChestedHorse donkey = (AbstractChestedHorse) (Object) this;
        AttributeInstance jumpStrength = Objects.requireNonNull(donkey.getAttribute(Attributes.JUMP_STRENGTH));
        jumpStrength.setBaseValue(generateJumpStrength(random::nextDouble));

        AttributeInstance movementSpeed = Objects.requireNonNull(donkey.getAttribute(Attributes.MOVEMENT_SPEED));
        movementSpeed.setBaseValue(generateSpeed(random::nextDouble));
    }
}
