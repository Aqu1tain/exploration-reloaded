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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
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
    private static final int MAX_UPDRAFT_DEPTH = 38;
    private static final int SIGNAL_FIRE_RANGE = 24;
    private static final int CAMPFIRE_RANGE = 10;
    /** Each campfire touching the one below you widens its column. Build a hearth, fly higher. */
    private static final int RANGE_PER_NEIGHBOUR = 3;
    /** Ticks of campfire immunity granted by a launch, so leaving the fire never singes you. */
    private static final int LAUNCH_IMMUNITY_TICKS = 20;
    private static final int BOOST_DURATION_TICKS = 40;
    /** A launch is a longer burn than a mid-air dash, because it has to get you off the ground. */
    private static final int LAUNCH_BOOST_TICKS = 50;
    private static final int BOOST_COOLDOWN_TICKS = 60;
    /** Ticks a player must already have been gliding before a charge may be spent. */
    private static final int BOOST_STARTUP_TICKS = 10;
    private static final int SMOKE_TRAIL_TICKS = 100;
    private static final double CLOUD_LAYER = 100.0;
    private static final double CLOUDSKIPPER_CEILING = 230.0;
    /** Horizontal drag vanilla applies each tick of a glide; Cloudskipper gives part of it back. */
    private static final double GLIDE_DRAG = 0.99;
    private static final double MAX_DRAG_RECOVERY = 0.6;
    /** Instant kick a spent charge gives, in blocks per tick along the look vector. */
    private static final double DASH_IMPULSE = 1.5;

    private ElytraFlight() {
    }

    public static void tick(ServerLevel level, ServerPlayer player) {
        tickCooldown(player);
        tickChargeDecay(player);
        tickCampfireLaunch(level, player);
        tickCampfireCharging(level, player);
        tickBoost(level, player);
        tickUpdrafts(level, player);
        tickCloudskipper(level, player);
        tickSmokeTrail(level, player);
    }

    private static void tickCooldown(ServerPlayer player) {
        int cooldown = FlightState.boostCooldown(player);
        if (cooldown > 0) {
            FlightState.setBoostCooldown(player, cooldown - 1);
        }
        int immunity = FlightState.launchImmunity(player);
        if (immunity > 0) {
            FlightState.setLaunchImmunity(player, immunity - 1);
        }
    }

    /** Charges are carried into flight, not hoarded: landing with nothing armed empties the tank. */
    private static void tickChargeDecay(ServerPlayer player) {
        if (player.onGround() && !player.isFallFlying() && !FlightState.isCharged(player)
                && !isRidingCampfireSmoke(player)) {
            FlightState.setCharges(player, 0);
        }
    }

    /**
     * Cloudskipper trades thrust for reach: the higher you fly, the less the air holds you back. Nothing
     * happens below the cloud layer, and the effect maxes out well above it.
     *
     * <p>Eleron's own version is dead code — its {@code velocity.lerp(velocity, fac)} returns the input
     * untouched whatever the factor — so this is written from the described behaviour rather than ported.
     */
    private static void tickCloudskipper(ServerLevel level, ServerPlayer player) {
        int enchantLevel = FlightEnchantments.cloudskipperLevel(player);
        if (enchantLevel <= 0 || !player.isFallFlying()) {
            return;
        }

        double y = player.getY();
        if (y < CLOUD_LAYER) {
            return;
        }
        // Quadratic in altitude, as upstream: barely felt just above the clouds, full effect near the ceiling.
        double altitude = y >= CLOUDSKIPPER_CEILING ? 1.0 : 0.00006 * Math.pow(y - CLOUD_LAYER, 2);
        double factor = Math.min(altitude, 1.0) * MAX_DRAG_RECOVERY * (enchantLevel / 3.0);

        // Undo part of the drag rather than adding thrust, so it lengthens a glide instead of speeding it.
        double recovered = factor * (1.0 / GLIDE_DRAG - 1.0);
        Vec3 velocity = player.getDeltaMovement();
        push(player, velocity.add(velocity.x * recovered, 0.0, velocity.z * recovered));

        if (player.tickCount % 4 == 0) {
            Vec3 trail = player.position().subtract(player.getLookAngle());
            level.sendParticles(ParticleTypes.POOF, trail.x, trail.y, trail.z,
                    1 + (int) (recovered * 40.0), 0.1, 0.1, 0.1, 0.02);
        }
    }

    /** A rocket lit mid-glide no longer pushes; it just streams smoke behind the player for a while. */
    private static void tickSmokeTrail(ServerLevel level, ServerPlayer player) {
        int remaining = FlightState.smokeTrailTicks(player);
        if (remaining <= 0) {
            return;
        }
        if (!player.isFallFlying()) {
            FlightState.setSmokeTrailTicks(player, 0);
            return;
        }
        FlightState.setSmokeTrailTicks(player, remaining - 1);

        if (player.tickCount % 3 == 0) {
            Vec3 pos = player.position().subtract(player.getLookAngle());
            level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, pos.x, pos.y, pos.z, 2, 0.1, 0.1, 0.1, 0.005);
        }
    }

    public static void startSmokeTrail(ServerPlayer player) {
        FlightState.setSmokeTrailTicks(player, SMOKE_TRAIL_TICKS);
    }

    /**
     * Crouching on a lit campfire means riding its smoke column rather than blundering into the fire.
     * It is what earns the burn exemption, the launch, and the Smokestack charges.
     */
    public static boolean isRidingCampfireSmoke(Player player) {
        if (!player.isShiftKeyDown() || !player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
            return false;
        }
        BlockState below = player.getBlockStateOn();
        return below.is(BlockTags.CAMPFIRES) && below.getValue(CampfireBlock.LIT);
    }

    /**
     * Releasing the crouch is what launches you: the smoke column has been charging under you, and
     * standing up lets it go. The launch is the same directional thrust a spent charge gives, so you
     * aim where you want to go before standing up.
     */
    private static void tickCampfireLaunch(ServerLevel level, ServerPlayer player) {
        if (player.isShiftKeyDown() || !FlightState.isCharged(player)) {
            return;
        }

        FlightState.setCharged(player, false);
        FlightState.setCampfireChargeTime(player, 0);
        FlightState.setBoostTicks(player, LAUNCH_BOOST_TICKS);
        FlightState.setLaunchImmunity(player, LAUNCH_IMMUNITY_TICKS);
        player.startFallFlying();

        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 0.8F);
        Vec3 pos = player.position();
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x, pos.y, pos.z, 30, 0.3, 0.2, 0.3, 0.05);
    }

    private static void tickCampfireCharging(ServerLevel level, ServerPlayer player) {
        if (!isRidingCampfireSmoke(player)) {
            FlightState.setCampfireChargeTime(player, 0);
            return;
        }

        int elapsed = FlightState.campfireChargeTime(player) + 1;
        FlightState.setCampfireChargeTime(player, elapsed);

        int interval = Math.max(1, level.getGameRules().get(FlightRules.SMOKESTACK_CHARGE_TICKS));
        if (elapsed % interval != 0) {
            return;
        }

        // The first interval always arms the launch, even with no Smokestack: taking off must not
        // depend on a treasure enchantment. Further intervals bank charges up to the enchantment level.
        boolean bankable = FlightState.charges(player) < maxCharges(player);
        if (!bankable && FlightState.isCharged(player)) {
            return;
        }
        FlightState.setCharged(player, true);
        if (bankable) {
            grantCharge(level, player);
        }
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
        if (FlightState.boostCooldown(player) > 0 || player.getFallFlyingTicks() <= BOOST_STARTUP_TICKS) {
            return false;
        }
        if (!player.isCreative()) {
            FlightState.setCharges(player, FlightState.charges(player) - 1);
        }
        FlightState.setBoostCooldown(player, BOOST_COOLDOWN_TICKS);

        // A mid-air dash is an instant kick, unlike the launch's long burn. Eleron made both sustained;
        // Aileron's snappier split is the one that reads as a dash.
        push(player, player.getDeltaMovement().add(player.getLookAngle().scale(DASH_IMPULSE)));
        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 0.4F);
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
        if (!state.is(FlightTags.CREATES_UPDRAFT) || !state.getValue(CampfireBlock.LIT)) {
            return;
        }

        int range = state.getValue(CampfireBlock.SIGNAL_FIRE)
                ? SIGNAL_FIRE_RANGE
                : CAMPFIRE_RANGE + RANGE_PER_NEIGHBOUR * adjacentCampfires(level, pos);
        double distance = Math.abs(pos.getY() - player.getY());
        if (distance <= 0 || distance > range) {
            return;
        }

        double lift = Math.min(range / distance / 7.0, 1.0);
        Vec3 velocity = player.getDeltaMovement();
        push(player, new Vec3(velocity.x, Math.min(velocity.y + lift, 1.0), velocity.z));
    }

    private static int adjacentCampfires(ServerLevel level, BlockPos pos) {
        int neighbours = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(side)).is(FlightTags.CREATES_UPDRAFT)) {
                neighbours++;
            }
        }
        return neighbours;
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
