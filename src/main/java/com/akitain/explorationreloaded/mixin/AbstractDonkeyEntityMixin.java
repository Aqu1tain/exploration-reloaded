package com.akitain.explorationreloaded.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(AbstractDonkeyEntity.class)
public abstract class AbstractDonkeyEntityMixin extends AbstractHorseEntity {
    protected AbstractDonkeyEntityMixin(EntityType<? extends AbstractHorseEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getInventoryColumns", at = @At("HEAD"), cancellable = true)
    private void reduceMuleInventoryColumns(CallbackInfoReturnable<Integer> cir) {
        if ((AbstractDonkeyEntity) (Object) this instanceof MuleEntity mule) {
            cir.setReturnValue(mule.hasChest() ? 3 : 0);
        }
    }

    @Inject(method = "initAttributes", at = @At("TAIL"))
    private void randomiseDonkeyAttributes(Random random, CallbackInfo ci) {
        AbstractDonkeyEntity donkey = (AbstractDonkeyEntity) (Object) this;
        EntityAttributeInstance jumpStrength = Objects.requireNonNull(donkey.getAttributeInstance(EntityAttributes.JUMP_STRENGTH));
        jumpStrength.setBaseValue(getChildJumpStrengthBonus(random::nextDouble));

        EntityAttributeInstance movementSpeed = Objects.requireNonNull(donkey.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED));
        movementSpeed.setBaseValue(getChildMovementSpeedBonus(random::nextDouble));
    }
}
