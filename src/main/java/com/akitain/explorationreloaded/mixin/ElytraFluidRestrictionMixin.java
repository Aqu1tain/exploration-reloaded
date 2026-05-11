package com.akitain.explorationreloaded.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class ElytraFluidRestrictionMixin {
    @Redirect(method = "canGlide", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
    private boolean blockGlidingInFluids(LivingEntity entity, RegistryEntry<StatusEffect> effect) {
        boolean blocksGliding = entity.hasStatusEffect(effect);
        boolean weatherBlocksGliding = entity.getEntityWorld().getDifficulty().getId() > 1 && entity.isTouchingWaterOrRain();
        return blocksGliding || weatherBlocksGliding || entity.isTouchingWater() || entity.isInLava();
    }
}
