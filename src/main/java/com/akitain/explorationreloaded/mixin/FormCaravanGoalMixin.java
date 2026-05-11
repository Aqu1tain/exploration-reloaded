package com.akitain.explorationreloaded.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.ai.goal.FormCaravanGoal;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FormCaravanGoal.class)
public abstract class FormCaravanGoalMixin {
    @Shadow
    @Final
    public LlamaEntity llama;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;<init>(DDD)V"))
    private void catchUpToCaravan(CallbackInfo ci, @Local double distance) {
        if (distance <= 20) {
            return;
        }

        LlamaEntity following = this.llama.getFollowing();
        if (following == null || !following.isOnGround()) {
            return;
        }

        Vec3d position = following.getEntityPos();
        this.llama.requestTeleport(position.x, position.y, position.z);
        this.llama.fallDistance = 0;
    }
}
