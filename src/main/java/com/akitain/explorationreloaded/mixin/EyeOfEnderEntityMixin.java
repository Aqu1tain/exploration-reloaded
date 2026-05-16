package com.akitain.explorationreloaded.mixin;

import net.minecraft.world.entity.projectile.EyeOfEnder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EyeOfEnder.class)
public class EyeOfEnderEntityMixin {
    @ModifyArg(method = "signalTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"), index = 0)
    private int keepEndCityEyesIntactLonger(int bound) {
        return 10;
    }
}
