package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.registry.ExplorationItems;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void requireDragonFireworkForGliding(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (!user.isGliding()) {
            return;
        }
        if (itemStack.isOf(Items.FIREWORK_ROCKET)) {
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }
        if (itemStack.isOf(ExplorationItems.DRAGON_FIREWORK_ROCKET) && world.hasRain(user.getBlockPos())) {
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }
        if (user instanceof ServerPlayerEntity serverPlayer && itemStack.isOf(ExplorationItems.DRAGON_FIREWORK_ROCKET)) {
            Criteria.CONSUME_ITEM.trigger(serverPlayer, itemStack);
        }
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void keepDragonFireworkForFlight(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (context.getStack().isOf(ExplorationItems.DRAGON_FIREWORK_ROCKET)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
