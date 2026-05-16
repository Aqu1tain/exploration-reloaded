package com.akitain.explorationreloaded.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LlamaFollowCaravanGoal.class)
public abstract class FormCaravanGoalMixin {
    @Shadow
    @Final
    public Llama llama;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;<init>(DDD)V"))
    private void catchUpToCaravan(CallbackInfo ci, @Local double distance) {
        if (distance <= 20) {
            return;
        }

        Llama following = this.llama.getCaravanHead();
        if (following == null || !following.onGround()) {
            return;
        }

        Vec3 position = following.position();
        this.llama.teleportTo(position.x, position.y, position.z);
        this.llama.fallDistance = 0;
    }
}
