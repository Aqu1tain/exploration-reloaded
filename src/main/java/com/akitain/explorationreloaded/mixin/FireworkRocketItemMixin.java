package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.flight.FlightRules;
import com.akitain.explorationreloaded.flight.ElytraFlight;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Fireworks no longer propel a glider. Altitude comes from campfires instead, so flying somewhere costs
// infrastructure rather than a stack of rockets. Previously a bespoke dragon firework was the exception;
// it is gone, and no rocket boosts flight now.
@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void erBlockFireworkBoost(Level level, Player player, InteractionHand hand,
                                      CallbackInfoReturnable<InteractionResult> cir) {
        if (!player.isFallFlying()) {
            return;
        }
        // Game rules only exist server-side; the server's result is authoritative either way.
        if (level instanceof ServerLevel serverLevel) {
            if (serverLevel.getGameRules().get(FlightRules.FIREWORK_BOOSTS_FLIGHT)) {
                return;
            }
            // Spent for show rather than speed: the rocket burns into a trail behind the glider.
            if (player instanceof ServerPlayer serverPlayer) {
                ElytraFlight.startSmokeTrail(serverPlayer);
                player.getItemInHand(hand).consume(1, player);
            }
        }
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
