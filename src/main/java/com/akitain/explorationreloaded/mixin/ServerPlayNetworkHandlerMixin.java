package com.akitain.explorationreloaded.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @WrapOperation(method = "onVehicleMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"))
    private void skipClientMoveAfterVehicleTeleport(Entity entity, MovementType movementType, Vec3d movement, Operation<Void> original) {
        if (!entity.getCommandTags().contains("tp")) {
            original.call(entity, movementType, movement);
        }
    }

    @Inject(method = "onVehicleMove", at = @At("TAIL"))
    private void removeTeleportTag(VehicleMoveC2SPacket packet, CallbackInfo ci) {
        Entity rootVehicle = this.player.getRootVehicle();
        rootVehicle.removeCommandTag("tp");
    }
}
