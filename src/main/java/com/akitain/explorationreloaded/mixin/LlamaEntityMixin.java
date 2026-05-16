package com.akitain.explorationreloaded.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Llama.class)
public abstract class LlamaEntityMixin extends AbstractChestedHorse {
    public LlamaEntityMixin(EntityType<? extends LlamaEntityMixin> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyArg(method = "getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/animal/equine/Llama;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/equine/Llama;setStrength(I)V"), index = 0)
    private int inheritStrength(int strength, @Local(ordinal = 1) Llama otherParent) {
        Llama llama = (Llama) (Object) this;
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
