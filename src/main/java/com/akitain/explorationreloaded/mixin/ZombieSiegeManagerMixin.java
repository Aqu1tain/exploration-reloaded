package com.akitain.explorationreloaded.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.village.ZombieSiegeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieSiegeManager.class)
public abstract class ZombieSiegeManagerMixin {
    @Inject(method = "trySpawnZombie", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V", shift = At.Shift.AFTER))
    private void spawnZombieHorse(ServerWorld world, CallbackInfo ci, @Local ZombieEntity zombie) {
        ZombieHorseEntity zombieHorse = EntityType.ZOMBIE_HORSE.create(world, SpawnReason.EVENT);
        zombie.setStackInHand(Hand.MAIN_HAND, Items.STONE_HOE.getDefaultStack());
        if (zombieHorse != null && world.random.nextInt(10) == 0) {
            zombie.setStackInHand(Hand.MAIN_HAND, Items.IRON_SPEAR.getDefaultStack());
            zombie.equipStack(EquipmentSlot.HEAD, Items.IRON_HELMET.getDefaultStack());
            zombieHorse.refreshPositionAndAngles(zombie.getX(), zombie.getY(), zombie.getZ(), zombie.getYaw(), 0.0F);
            zombieHorse.initialize(world, world.getLocalDifficulty(zombie.getBlockPos()), SpawnReason.EVENT, null);
            zombie.startRiding(zombieHorse);
            world.spawnEntity(zombieHorse);
        }
    }
}
