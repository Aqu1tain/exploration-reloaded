package com.akitain.explorationreloaded.mixin.util;

import com.akitain.explorationreloaded.registry.item.MapBookState;
import com.akitain.explorationreloaded.registry.item.MapBookStateManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "tickServer", at = @At("RETURN"))
    private void syncMapBooks(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer)(Object) this;
        for (int id : MapBookStateManager.INSTANCE.currentBooks) {
            MapBookState state = MapBookStateManager.INSTANCE.getMapBookState(server, id);
            if (state != null) {
                state.sendData(server, id);
            }
        }
        MapBookStateManager.INSTANCE.currentBooks = (new ArrayList<>());
    }
}
