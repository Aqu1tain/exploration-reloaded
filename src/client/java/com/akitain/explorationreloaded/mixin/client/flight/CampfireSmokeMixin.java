package com.akitain.explorationreloaded.mixin.client.flight;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A hearth of touching campfires lifts a glider further than a lone one, and this is what shows it:
 * the more neighbours a fire has, the taller its smoke column climbs. Without it the extra range is
 * invisible and nobody would ever discover it.
 */
@Mixin(CampfireBlockEntity.class)
public class CampfireSmokeMixin {

    @WrapOperation(method = "particleTick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/CampfireBlock;makeParticles(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ZZ)V"))
    private static void erTallerSmokeForHearths(Level level, BlockPos pos, boolean isSignalFire, boolean smoking,
                                                Operation<Void> original) {
        int neighbours = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(side)).getBlock() instanceof CampfireBlock) {
                neighbours++;
            }
        }

        // A signal fire already draws the tall column, so only plain fires need the upgrade.
        original.call(level, pos, isSignalFire || neighbours > 0, smoking);
    }
}
