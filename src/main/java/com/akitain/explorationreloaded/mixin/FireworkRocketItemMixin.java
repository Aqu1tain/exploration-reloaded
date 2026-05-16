package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.registry.ExplorationItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void requireDragonFireworkForGliding(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (!user.isFallFlying()) {
            return;
        }
        if (itemStack.is(Items.FIREWORK_ROCKET)) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
        if (itemStack.is(ExplorationItems.DRAGON_FIREWORK_ROCKET) && world.isRainingAt(user.blockPosition())) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
        if (user instanceof ServerPlayer serverPlayer && itemStack.is(ExplorationItems.DRAGON_FIREWORK_ROCKET)) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, itemStack);
        }
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void keepDragonFireworkForFlight(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context.getItemInHand().is(ExplorationItems.DRAGON_FIREWORK_ROCKET)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
