package com.akitain.explorationreloaded.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin extends ThrownItemEntity {
    @Unique
    @Nullable
    private LivingEntity vehicle = null;

    public EnderPearlEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V", at = @At("TAIL"))
    private void saveThrownVehicle(World world, LivingEntity owner, ItemStack stack, CallbackInfo ci) {
        if (!owner.hasVehicle()) {
            return;
        }

        LivingEntity root = rootVehicle(owner);
        if (root != null && owner == root.getControllingPassenger()) {
            this.vehicle = root;
        }
    }

    @Redirect(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;teleportTo(Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/server/network/ServerPlayerEntity;"))
    private ServerPlayerEntity teleportWithVehicle(ServerPlayerEntity player, TeleportTarget target, @Share("passed") LocalBooleanRef ref) {
        Criteria.CONSUME_ITEM.trigger(player, Items.ENDER_PEARL.getDefaultStack());
        if (player.hasVehicle()) {
            LivingEntity currentVehicle = rootVehicle(player);
            if (currentVehicle != null && currentVehicle.equals(this.vehicle)) {
                return teleportMatchingVehicle(player, currentVehicle, ref);
            }
        }

        return player.teleportTo(new TeleportTarget(
                (ServerWorld) this.getEntityWorld(),
                this.getLastRenderPos(),
                Vec3d.ZERO,
                0.0F,
                0.0F,
                PositionFlag.combine(PositionFlag.ROT, PositionFlag.DELTA),
                TeleportTarget.NO_OP
        ));
    }

    @Unique
    private ServerPlayerEntity teleportMatchingVehicle(ServerPlayerEntity player, LivingEntity currentVehicle, LocalBooleanRef ref) {
        currentVehicle.teleportTo(new TeleportTarget(
                (ServerWorld) this.getEntityWorld(),
                this.getLastRenderPos(),
                Vec3d.ZERO,
                0.0F,
                0.0F,
                PositionFlag.combine(PositionFlag.ROT, PositionFlag.DELTA),
                TeleportTarget.NO_OP
        ));
        currentVehicle.addCommandTag("tp");

        if (currentVehicle instanceof PathAwareEntity pathAwareEntity) {
            pathAwareEntity.getNavigation().stop();
        }

        currentVehicle.onLanding();
        EnderPearlEntity pearl = (EnderPearlEntity) (Object) this;
        PlayerEntity owner = (PlayerEntity) Objects.requireNonNull(pearl.getOwner());
        if (!owner.getAbilities().creativeMode) {
            currentVehicle.damage((ServerWorld) this.getEntityWorld(), this.getDamageSources().fall(), 5.0F);
        }
        ref.set(true);

        ServerPlayerEntity teleportedPlayer = player.teleportTo(new TeleportTarget(
                (ServerWorld) this.getEntityWorld(),
                this.getLastRenderPos(),
                Vec3d.ZERO,
                0.0F,
                0.0F,
                PositionFlag.combine(PositionFlag.ROT, PositionFlag.DELTA),
                TeleportTarget.NO_OP
        ));
        assert teleportedPlayer != null;
        teleportedPlayer.startRiding(currentVehicle);
        return teleportedPlayer;
    }

    @Unique
    @Nullable
    private LivingEntity rootVehicle(Entity entity) {
        if (!entity.hasVehicle() || !(entity.getVehicle() instanceof LivingEntity vehicleEntity)) {
            return null;
        }

        LivingEntity parentVehicle = rootVehicle(vehicleEntity);
        return parentVehicle == null ? vehicleEntity : parentVehicle;
    }
}
