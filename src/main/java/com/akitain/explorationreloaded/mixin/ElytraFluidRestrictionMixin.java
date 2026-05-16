package com.akitain.explorationreloaded.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class ElytraFluidRestrictionMixin {
    @Redirect(method = "canGlide", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean blockGlidingInFluids(LivingEntity entity, Holder<MobEffect> effect) {
        boolean blocksGliding = entity.hasEffect(effect);
        return blocksGliding || entity.isInWater() || entity.isInLava();
    }
}
