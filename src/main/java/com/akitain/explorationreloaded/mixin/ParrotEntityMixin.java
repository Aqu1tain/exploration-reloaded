package com.akitain.explorationreloaded.mixin;

import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParrotEntity.class)
public abstract class ParrotEntityMixin {
    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void catchParrot(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = player.getStackInHand(hand);
        ParrotEntity parrot = (ParrotEntity) (Object) this;
        if (!itemStack.isEmpty() || !parrot.isInAir() || !parrot.isTamed() || !parrot.isOwner(player)) {
            return;
        }

        if (player.getEntityWorld().isClient()) {
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (player instanceof ServerPlayerEntity serverPlayer && parrot.mountOnto(serverPlayer)) {
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
