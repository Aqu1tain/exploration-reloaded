package com.akitain.explorationreloaded.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LlamaEntity.class)
public abstract class LlamaEntityMixin extends AbstractDonkeyEntity {
    public LlamaEntityMixin(EntityType<? extends LlamaEntityMixin> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(method = "createChild*", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/LlamaEntity;setStrength(I)V"), index = 0)
    private int inheritStrength(int strength, @Local(ordinal = 1) LlamaEntity otherParent) {
        LlamaEntity llama = (LlamaEntity) (Object) this;
        int inheritedStrength = (llama.getStrength() + otherParent.getStrength()) / 2;
        float roll = this.random.nextFloat();
        if (roll < 0.5F) {
            return inheritedStrength - 1;
        }
        if (roll > 0.8F) {
            return inheritedStrength + 1;
        }
        return inheritedStrength;
    }
}
