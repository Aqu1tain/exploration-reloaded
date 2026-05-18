package com.akitain.explorationreloaded.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;

@Mixin(WanderingTraderSpawner.class)
public abstract class WanderingTraderManagerMixin {
    @Shadow
    @Final
    private RandomSource random;

    @Shadow
    @Nullable
    protected abstract BlockPos findSpawnPositionNear(LevelReader world, BlockPos pos, int range);

    @Shadow
    protected abstract boolean hasEnoughSpace(BlockGetter world, BlockPos pos);

    @Shadow
    protected abstract void tryToSpawnLlamaFor(ServerLevel world, WanderingTrader wanderingTrader, int range);

    @ModifyExpressionValue(method = "findSpawnPositionNear", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/SpawnPlacementType;isSpawnPositionOk(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntityType;)Z"))
    private boolean naturalSpawnsWanderingTrader(boolean original, @Local(argsOnly = true) LevelReader world, @Local(argsOnly = true) BlockPos pos) {
        return original && world.getBlockState(pos.below()).is(BlockTags.AZALEA_ROOT_REPLACEABLE);
    }

    @Inject(method = "spawn", at = @At("HEAD"), cancellable = true)
    private void spawnMoreOften(ServerLevel world, CallbackInfoReturnable<Boolean> cir) {
        Player player = world.getRandomPlayer();
        if (player == null) {
            cir.setReturnValue(true);
            return;
        }

        if (random.nextInt(2) == 0) {
            cir.setReturnValue(false);
            return;
        }

        BlockPos playerPos = player.blockPosition();
        Optional<BlockPos> meetingPoint = world.getPoiManager().find(
                poiType -> poiType.is(PoiTypes.MEETING),
                pos -> true,
                playerPos,
                48,
                PoiManager.Occupancy.ANY
        );

        BlockPos targetPos = meetingPoint.orElse(playerPos);
        BlockPos spawnPos = findSpawnPositionNear(world, targetPos, 48);
        if (spawnPos == null || !hasEnoughSpace(world, spawnPos)) {
            cir.setReturnValue(false);
            return;
        }

        if (world.getBiome(spawnPos).is(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) {
            cir.setReturnValue(false);
            return;
        }

        WanderingTrader wanderingTrader = EntityType.WANDERING_TRADER.spawn(world, spawnPos, EntitySpawnReason.EVENT);
        if (wanderingTrader == null) {
            cir.setReturnValue(false);
            return;
        }

        for (int i = 0; i < 2; i++) {
            tryToSpawnLlamaFor(world, wanderingTrader, 4);
        }

        wanderingTrader.setDespawnDelay(48000);
        wanderingTrader.setWanderTarget(targetPos);
        wanderingTrader.setHomeTo(targetPos, 16);
        cir.setReturnValue(true);
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 75))
    private int raiseMaximumSpawnChance(int constant) {
        return 100;
    }
}
