package com.akitain.explorationreloaded.mixin.client.map;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import com.akitain.explorationreloaded.network.MapPositionRequestPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MapPositionRequestMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;overrideMapData(Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;)V"), method = "handleMapItemData")
    private void requestMapPosition(ClientboundMapItemDataPacket packet, CallbackInfo ci) {
        ClientPlayNetworking.send(new MapPositionRequestPayload(packet.mapId()));
    }
}
