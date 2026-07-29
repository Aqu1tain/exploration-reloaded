package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.flight.ElytraFlight;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerFlightTickMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void erTickElytraFlight(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
            ElytraFlight.tick(level, player);
        }
    }
}
