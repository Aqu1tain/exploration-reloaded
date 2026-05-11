package com.akitain.explorationreloaded.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WanderingTraderManager;
import net.minecraft.world.WorldView;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(WanderingTraderManager.class)
public abstract class WanderingTraderManagerMixin {
    @Shadow
    @Final
    private ServerWorldProperties properties;

    @Shadow
    @Final
    private Random random;

    @Shadow
    @Nullable
    protected abstract BlockPos getNearbySpawnPos(WorldView world, BlockPos pos, int range);

    @Shadow
    protected abstract boolean doesNotSuffocateAt(BlockView world, BlockPos pos);

    @Shadow
    protected abstract void spawnLlama(ServerWorld world, WanderingTraderEntity wanderingTrader, int range);

    @ModifyExpressionValue(method = "getNearbySpawnPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/SpawnLocation;isSpawnPositionOk(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/EntityType;)Z"))
    private boolean naturalSpawnsWanderingTrader(boolean original, @Local(argsOnly = true) WorldView world, @Local(argsOnly = true) BlockPos pos) {
        return original && world.getBlockState(pos.down()).isIn(BlockTags.AZALEA_ROOT_REPLACEABLE);
    }

    @Inject(method = "trySpawn", at = @At("HEAD"), cancellable = true)
    private void spawnMoreOften(ServerWorld world, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = world.getRandomAlivePlayer();
        if (player == null) {
            cir.setReturnValue(true);
            return;
        }

        if (random.nextInt(2) == 0) {
            cir.setReturnValue(false);
            return;
        }

        BlockPos playerPos = player.getBlockPos();
        Optional<BlockPos> meetingPoint = world.getPointOfInterestStorage().getPosition(
                poiType -> poiType.matchesKey(PointOfInterestTypes.MEETING),
                pos -> true,
                playerPos,
                48,
                PointOfInterestStorage.OccupationStatus.ANY
        );

        BlockPos targetPos = meetingPoint.orElse(playerPos);
        BlockPos spawnPos = getNearbySpawnPos(world, targetPos, 48);
        if (spawnPos == null || !doesNotSuffocateAt(world, spawnPos)) {
            cir.setReturnValue(false);
            return;
        }

        if (world.getBiome(spawnPos).isIn(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) {
            cir.setReturnValue(false);
            return;
        }

        WanderingTraderEntity wanderingTrader = EntityType.WANDERING_TRADER.spawn(world, spawnPos, SpawnReason.EVENT);
        if (wanderingTrader == null) {
            cir.setReturnValue(false);
            return;
        }

        for (int i = 0; i < 2; i++) {
            spawnLlama(world, wanderingTrader, 4);
        }

        properties.setWanderingTraderId(wanderingTrader.getUuid());
        wanderingTrader.setDespawnDelay(48000);
        wanderingTrader.setWanderTarget(targetPos);
        wanderingTrader.setPositionTarget(targetPos, 16);
        cir.setReturnValue(true);
    }

    @ModifyConstant(method = "spawn", constant = @Constant(intValue = 75))
    private int raiseMaximumSpawnChance(int constant) {
        return 100;
    }
}
