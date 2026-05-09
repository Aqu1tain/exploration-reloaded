package com.akitain.explorationreloaded.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WanderingTraderSpawner.class)
public class WanderingTraderSpawnerMixin {
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 75))
    private int raiseMaximumSpawnChance(int constant) {
        return 100;
    }

    @ModifyConstant(method = "spawn", constant = @Constant(intValue = 10))
    private int increaseSpawnAttemptChance(int constant) {
        return 2;
    }

    @Redirect(
            method = "findSpawnPositionNear",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/SpawnPlacementType;isSpawnPositionOk(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntityType;)Z"
            )
    )
    private boolean requireNaturalSpawnGround(SpawnPlacementType spawnPlacementType, LevelReader level, BlockPos pos, EntityType<?> entityType) {
        return spawnPlacementType.isSpawnPositionOk(level, pos, entityType)
                && level.getBlockState(pos.below()).is(BlockTags.AZALEA_ROOT_REPLACEABLE);
    }
}
