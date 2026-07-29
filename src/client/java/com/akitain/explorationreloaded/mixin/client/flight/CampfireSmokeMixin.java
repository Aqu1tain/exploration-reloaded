package com.akitain.explorationreloaded.mixin.client.flight;

import com.akitain.explorationreloaded.flight.Hearth;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The smoke column grows with the hearth, because the hearth is what decides how far a glider is
 * thrown and lifted. Without this the whole scale is invisible and nobody would think to build wider.
 *
 * <p>Vanilla offers no height control — only a choice between cosy and signal smoke — so the extra
 * height comes from emitting more of the long-lived signal smoke, rising faster the bigger the hearth.
 */
@Mixin(CampfireBlockEntity.class)
public class CampfireSmokeMixin {

    /** One extra plume per this much hearth power, on top of the vanilla puff. */
    private static final int POWER_PER_EXTRA_PLUME = 2;
    private static final double BASE_RISE = 0.07;
    private static final double RISE_PER_POWER = 0.006;

    @WrapOperation(method = "particleTick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/CampfireBlock;makeParticles(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ZZ)V"))
    private static void erScaleSmokeToHearth(Level level, BlockPos pos, boolean isSignalFire, boolean smoking,
                                             Operation<Void> original) {
        int power = Hearth.power(level, pos);

        // Any hearth at all already earns the tall signal column; size then piles more on top.
        original.call(level, pos, isSignalFire || power > 0, smoking);
        if (power <= 0) {
            return;
        }

        RandomSource random = level.getRandom();
        int plumes = power / POWER_PER_EXTRA_PLUME;
        double rise = BASE_RISE + RISE_PER_POWER * power;

        for (int i = 0; i < plumes; i++) {
            level.addAlwaysVisibleParticle(
                    ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                    true,
                    pos.getX() + 0.5 + random.nextDouble() / 2.5 * (random.nextBoolean() ? 1 : -1),
                    pos.getY() + random.nextDouble() + random.nextDouble(),
                    pos.getZ() + 0.5 + random.nextDouble() / 2.5 * (random.nextBoolean() ? 1 : -1),
                    0.0,
                    rise,
                    0.0);
        }
    }
}
