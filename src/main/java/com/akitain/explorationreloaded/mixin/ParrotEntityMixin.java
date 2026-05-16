package com.akitain.explorationreloaded.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Parrot.class)
public abstract class ParrotEntityMixin {
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void catchParrot(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(hand);
        Parrot parrot = (Parrot) (Object) this;
        if (!itemStack.isEmpty() || !parrot.isFlying() || !parrot.isTame() || !parrot.isOwnedBy(player)) {
            return;
        }

        if (player.level().isClientSide()) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        if (player instanceof ServerPlayer serverPlayer && parrot.setEntityOnShoulder(serverPlayer)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
