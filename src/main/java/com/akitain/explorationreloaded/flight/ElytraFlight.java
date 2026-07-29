package com.akitain.explorationreloaded.flight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Elytra flight reworked after Aileron: fireworks no longer push, campfires do. Gaining altitude costs
 * infrastructure, which keeps rails and roads worth building.
 *
 * <p>Everything here runs on the server. Gliding is simulated client-side, so a velocity change only
 * sticks if the motion packet is pushed back to the player.
 */
public final class ElytraFlight {

    /** How far below the player an updraft is looked for. */
    private static final int MAX_UPDRAFT_DEPTH = 32;
    private static final int SIGNAL_FIRE_RANGE = 24;
    private static final int CAMPFIRE_RANGE = 10;
    private static final int BOOST_DURATION_TICKS = 10;

    private ElytraFlight() {
    }

    public static void tick(ServerLevel level, ServerPlayer player) {
        tickCampfireCharging(level, player);
        tickBoost(level, player);
        tickUpdrafts(level, player);
    }

    /**
     * Crouching on a lit campfire slowly fills the Smokestack charges the enchantment allows. Crouching
     * is what marks the player as riding the smoke rather than blundering into the fire, and it is what
     * {@link com.akitain.explorationreloaded.mixin.CampfireChargingMixin} keys the burn exemption off.
     */
    public static boolean isChargingFromCampfire(Player player) {
        if (!player.isShiftKeyDown() || smokestackLevel(player) <= 0) {
            return false;
        }
        BlockState below = player.getBlockStateOn();
        return below.is(BlockTags.CAMPFIRES) && below.getValue(CampfireBlock.LIT);
    }

    private static void tickCampfireCharging(ServerLevel level, ServerPlayer player) {
        if (!isChargingFromCampfire(player)) {
            FlightState.setCampfireChargeTime(player, 0);
            return;
        }

        int elapsed = FlightState.campfireChargeTime(player) + 1;
        FlightState.setCampfireChargeTime(player, elapsed);

        int interval = Math.max(1, level.getGameRules().get(FlightRules.SMOKESTACK_CHARGE_TICKS));
        if (elapsed % interval != 0) {
            return;
        }
        grantCharge(level, player);
    }

    public static void grantCharge(ServerLevel level, ServerPlayer player) {
        int charges = FlightState.charges(player);
        if (charges >= maxCharges(player)) {
            return;
        }

        FlightState.setCharges(player, charges + 1);
        level.playSound(null, player.blockPosition(), SoundEvents.CAMPFIRE_CRACKLE, SoundSource.PLAYERS,
                1.0F, 0.8F + FlightState.charges(player) * 0.2F);

        Vec3 pos = player.position();
        level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, 20, 0.5, 0.5, 0.5, 0.1);
        level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 50, 0.5, 0.5, 0.5, 0.4);
    }

    /** Spends one charge into a short forward thrust. Returns false when there is nothing to spend. */
    public static boolean spendCharge(ServerLevel level, ServerPlayer player) {
        if (!player.isFallFlying() || FlightState.charges(player) <= 0) {
            return false;
        }
        FlightState.setCharges(player, FlightState.charges(player) - 1);
        FlightState.setBoostTicks(player, BOOST_DURATION_TICKS);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static void tickBoost(ServerLevel level, ServerPlayer player) {
        int remaining = FlightState.boostTicks(player);
        if (remaining <= 0) {
            return;
        }
        if (!player.isFallFlying()) {
            FlightState.setBoostTicks(player, 0);
            return;
        }
        FlightState.setBoostTicks(player, remaining - 1);

        // Steer towards where the player is looking rather than simply adding speed, so a boost taken
        // mid-turn actually redirects the glide.
        Vec3 look = player.getLookAngle();
        Vec3 velocity = player.getDeltaMovement();
        push(player, velocity.add(
                look.x * 0.1 + (look.x * 1.5 - velocity.x) * 0.5,
                look.y * 0.1 + (look.y * 1.5 - velocity.y) * 0.5,
                look.z * 0.1 + (look.z * 1.5 - velocity.z) * 0.5));

        if (player.tickCount % 3 == 0) {
            Vec3 pos = player.position();
            level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 2, 0.2, 0.2, 0.2, 0.1);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, 3, 0.2, 0.2, 0.2, 0.1);
        }
    }

    /** A lit campfire below a glider pushes them up; a signal fire reaches more than twice as far. */
    private static void tickUpdrafts(ServerLevel level, ServerPlayer player) {
        if (!player.isFallFlying() || !level.getGameRules().get(FlightRules.CAMPFIRE_UPDRAFTS)) {
            return;
        }

        BlockPos.MutableBlockPos pos = player.blockPosition().mutable();
        int depth = 0;
        while (depth < MAX_UPDRAFT_DEPTH && level.isEmptyBlock(pos) && level.isInsideBuildHeight(pos.getY())) {
            depth++;
            pos.move(Direction.DOWN);
        }

        BlockState state = level.getBlockState(pos);
        if (!state.is(BlockTags.CAMPFIRES) || !state.getValue(CampfireBlock.LIT)) {
            return;
        }

        int range = state.getValue(CampfireBlock.SIGNAL_FIRE) ? SIGNAL_FIRE_RANGE : CAMPFIRE_RANGE;
        double distance = Math.abs(pos.getY() - player.getY());
        if (distance <= 0 || distance > range) {
            return;
        }

        double lift = Math.min(range / distance / 7.0, 1.0);
        Vec3 velocity = player.getDeltaMovement();
        push(player, new Vec3(velocity.x, Math.min(velocity.y + lift, 1.0), velocity.z));
    }

    private static void push(ServerPlayer player, Vec3 velocity) {
        player.setDeltaMovement(velocity);
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
    }

    private static int smokestackLevel(Player player) {
        return FlightEnchantments.smokestackLevel(player);
    }

    private static int maxCharges(ServerPlayer player) {
        return smokestackLevel(player);
    }
}
