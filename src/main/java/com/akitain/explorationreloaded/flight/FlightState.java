package com.akitain.explorationreloaded.flight;

import com.akitain.explorationreloaded.ExplorationReloaded;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Player;

/**
 * Per-player flight state. Charges persist and are synced so the client can show them; the timers are
 * short-lived enough to be rebuilt from scratch on load.
 */
public final class FlightState {

    public static final AttachmentType<Integer> SMOKESTACK_CHARGES = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT)
            .syncWith(ByteBufCodecs.VAR_INT.cast(), AttachmentSyncPredicate.targetOnly())
            .initializer(() -> 0)
            .buildAndRegister(ExplorationReloaded.id("smokestack_charges"));

    public static final AttachmentType<Integer> CAMPFIRE_CHARGE_TIME = AttachmentRegistry.<Integer>builder()
            .initializer(() -> 0)
            .buildAndRegister(ExplorationReloaded.id("campfire_charge_time"));

    public static final AttachmentType<Integer> BOOST_TICKS = AttachmentRegistry.<Integer>builder()
            .initializer(() -> 0)
            .buildAndRegister(ExplorationReloaded.id("boost_ticks"));

    private FlightState() {
    }

    public static void register() {
    }

    public static int charges(Player player) {
        return player.getAttachedOrElse(SMOKESTACK_CHARGES, 0);
    }

    public static void setCharges(Player player, int charges) {
        player.setAttached(SMOKESTACK_CHARGES, Math.max(0, charges));
    }

    public static int campfireChargeTime(Player player) {
        return player.getAttachedOrElse(CAMPFIRE_CHARGE_TIME, 0);
    }

    public static void setCampfireChargeTime(Player player, int ticks) {
        player.setAttached(CAMPFIRE_CHARGE_TIME, ticks);
    }

    public static int boostTicks(Player player) {
        return player.getAttachedOrElse(BOOST_TICKS, 0);
    }

    public static void setBoostTicks(Player player, int ticks) {
        player.setAttached(BOOST_TICKS, Math.max(0, ticks));
    }
}
