package com.akitain.explorationreloaded.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayer player;

    @WrapOperation(method = "handleMoveVehicle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private void skipClientMoveAfterVehicleTeleport(Entity entity, MoverType movementType, Vec3 movement, Operation<Void> original) {
        if (!entity.entityTags().contains("tp")) {
            original.call(entity, movementType, movement);
        }
    }

    @Inject(method = "handleMoveVehicle", at = @At("TAIL"))
    private void removeTeleportTag(ServerboundMoveVehiclePacket packet, CallbackInfo ci) {
        Entity rootVehicle = this.player.getRootVehicle();
        rootVehicle.removeTag("tp");
    }
}
