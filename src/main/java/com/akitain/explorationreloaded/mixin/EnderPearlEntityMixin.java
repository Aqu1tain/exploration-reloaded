package com.akitain.explorationreloaded.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

@Mixin(ThrownEnderpearl.class)
public abstract class EnderPearlEntityMixin extends ThrowableItemProjectile {
    @Unique
    @Nullable
    private LivingEntity vehicle = null;

    public EnderPearlEntityMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private void saveThrownVehicle(Level world, LivingEntity owner, ItemStack stack, CallbackInfo ci) {
        if (!owner.isPassenger()) {
            return;
        }

        LivingEntity root = rootVehicle(owner);
        if (root != null && owner == root.getControllingPassenger()) {
            this.vehicle = root;
        }
    }

    @Redirect(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;"))
    private ServerPlayer teleportWithVehicle(ServerPlayer player, TeleportTransition target, @Share("passed") LocalBooleanRef ref) {
        CriteriaTriggers.CONSUME_ITEM.trigger(player, Items.ENDER_PEARL.getDefaultInstance());
        if (player.isPassenger()) {
            LivingEntity currentVehicle = rootVehicle(player);
            if (currentVehicle != null && currentVehicle.equals(this.vehicle)) {
                return teleportMatchingVehicle(player, currentVehicle, ref);
            }
        }

        return player.teleport(new TeleportTransition(
                (ServerLevel) this.level(),
                this.oldPosition(),
                Vec3.ZERO,
                0.0F,
                0.0F,
                Relative.union(Relative.ROTATION, Relative.DELTA),
                TeleportTransition.DO_NOTHING
        ));
    }

    @Unique
    private ServerPlayer teleportMatchingVehicle(ServerPlayer player, LivingEntity currentVehicle, LocalBooleanRef ref) {
        currentVehicle.teleport(new TeleportTransition(
                (ServerLevel) this.level(),
                this.oldPosition(),
                Vec3.ZERO,
                0.0F,
                0.0F,
                Relative.union(Relative.ROTATION, Relative.DELTA),
                TeleportTransition.DO_NOTHING
        ));
        currentVehicle.addTag("tp");

        if (currentVehicle instanceof PathfinderMob pathAwareEntity) {
            pathAwareEntity.getNavigation().stop();
        }

        currentVehicle.resetFallDistance();
        ThrownEnderpearl pearl = (ThrownEnderpearl) (Object) this;
        Player owner = (Player) Objects.requireNonNull(pearl.getOwner());
        if (!owner.getAbilities().instabuild) {
            currentVehicle.hurtServer((ServerLevel) this.level(), this.damageSources().fall(), 5.0F);
        }
        ref.set(true);

        ServerPlayer teleportedPlayer = player.teleport(new TeleportTransition(
                (ServerLevel) this.level(),
                this.oldPosition(),
                Vec3.ZERO,
                0.0F,
                0.0F,
                Relative.union(Relative.ROTATION, Relative.DELTA),
                TeleportTransition.DO_NOTHING
        ));
        assert teleportedPlayer != null;
        teleportedPlayer.startRiding(currentVehicle);
        return teleportedPlayer;
    }

    @Unique
    @Nullable
    private LivingEntity rootVehicle(Entity entity) {
        if (!entity.isPassenger() || !(entity.getVehicle() instanceof LivingEntity vehicleEntity)) {
            return null;
        }

        LivingEntity parentVehicle = rootVehicle(vehicleEntity);
        return parentVehicle == null ? vehicleEntity : parentVehicle;
    }
}
