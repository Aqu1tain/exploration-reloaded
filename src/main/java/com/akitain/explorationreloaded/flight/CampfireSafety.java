package com.akitain.explorationreloaded.flight;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;

/**
 * Crouching on a campfire is how a Smokestack glider fills its charges, so it must not cook them. The
 * player is riding the smoke column, not standing in the flames; everyone else still burns.
 */
public final class CampfireSafety {

    private CampfireSafety() {
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!source.is(DamageTypes.CAMPFIRE) || !(entity instanceof Player player)) {
                return true;
            }
            return !ElytraFlight.isRidingCampfireSmoke(player);
        });
    }
}
