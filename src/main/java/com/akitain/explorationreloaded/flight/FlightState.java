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

    public static final AttachmentType<Integer> SMOKE_TRAIL_TICKS = AttachmentRegistry.<Integer>builder()
            .initializer(() -> 0)
            .buildAndRegister(ExplorationReloaded.id("smoke_trail_ticks"));

    /** Set once the smoke column is ready to throw the player; consumed when they stand up. */
    public static final AttachmentType<Boolean> CHARGED = AttachmentRegistry.<Boolean>builder()
            .initializer(() -> false)
            .buildAndRegister(ExplorationReloaded.id("charged"));

    public static final AttachmentType<Integer> BOOST_COOLDOWN = AttachmentRegistry.<Integer>builder()
            .initializer(() -> 0)
            .buildAndRegister(ExplorationReloaded.id("boost_cooldown"));

    public static final AttachmentType<Integer> LAUNCH_IMMUNITY = AttachmentRegistry.<Integer>builder()
            .initializer(() -> 0)
            .buildAndRegister(ExplorationReloaded.id("launch_immunity"));

    /** Neighbour-equivalents of the fire being charged on, sampled while crouching. */
    public static final AttachmentType<Integer> HEARTH_POWER = AttachmentRegistry.<Integer>builder()
            .initializer(() -> 0)
            .buildAndRegister(ExplorationReloaded.id("hearth_power"));

    public static final AttachmentType<Integer> LAUNCH_GRACE = AttachmentRegistry.<Integer>builder()
            .initializer(() -> 0)
            .buildAndRegister(ExplorationReloaded.id("launch_grace"));

    public static final AttachmentType<Integer> BOOST_TICKS = AttachmentRegistry.<Integer>builder()
            .syncWith(ByteBufCodecs.VAR_INT.cast(), AttachmentSyncPredicate.targetOnly())
            .initializer(() -> 0)
            .buildAndRegister(ExplorationReloaded.id("boost_ticks"));

    /** Mirrors the game rule so the client, which owns glide physics, knows whether to lift. */
    public static final AttachmentType<Boolean> UPDRAFTS_ENABLED = AttachmentRegistry.<Boolean>builder()
            .syncWith(ByteBufCodecs.BOOL.cast(), AttachmentSyncPredicate.targetOnly())
            .initializer(() -> true)
            .buildAndRegister(ExplorationReloaded.id("updrafts_enabled"));

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

    public static int smokeTrailTicks(Player player) {
        return player.getAttachedOrElse(SMOKE_TRAIL_TICKS, 0);
    }

    public static void setSmokeTrailTicks(Player player, int ticks) {
        player.setAttached(SMOKE_TRAIL_TICKS, Math.max(0, ticks));
    }

    public static boolean isCharged(Player player) {
        return player.getAttachedOrElse(CHARGED, false);
    }

    public static void setCharged(Player player, boolean charged) {
        player.setAttached(CHARGED, charged);
    }

    public static int boostCooldown(Player player) {
        return player.getAttachedOrElse(BOOST_COOLDOWN, 0);
    }

    public static void setBoostCooldown(Player player, int ticks) {
        player.setAttached(BOOST_COOLDOWN, Math.max(0, ticks));
    }

    public static int launchImmunity(Player player) {
        return player.getAttachedOrElse(LAUNCH_IMMUNITY, 0);
    }

    public static void setLaunchImmunity(Player player, int ticks) {
        player.setAttached(LAUNCH_IMMUNITY, Math.max(0, ticks));
    }

    public static int hearthPower(Player player) {
        return player.getAttachedOrElse(HEARTH_POWER, 0);
    }

    public static void setHearthPower(Player player, int power) {
        player.setAttached(HEARTH_POWER, Math.max(0, power));
    }

    public static int launchGrace(Player player) {
        return player.getAttachedOrElse(LAUNCH_GRACE, 0);
    }

    public static void setLaunchGrace(Player player, int ticks) {
        player.setAttached(LAUNCH_GRACE, Math.max(0, ticks));
    }

    public static boolean updraftsEnabled(Player player) {
        return player.getAttachedOrElse(UPDRAFTS_ENABLED, true);
    }

    public static void setUpdraftsEnabled(Player player, boolean enabled) {
        if (updraftsEnabled(player) != enabled) {
            player.setAttached(UPDRAFTS_ENABLED, enabled);
        }
    }

    public static int boostTicks(Player player) {
        return player.getAttachedOrElse(BOOST_TICKS, 0);
    }

    public static void setBoostTicks(Player player, int ticks) {
        player.setAttached(BOOST_TICKS, Math.max(0, ticks));
    }
}
