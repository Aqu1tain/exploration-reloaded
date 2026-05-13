package com.akitain.explorationreloaded;

import com.akitain.explorationreloaded.map_book.MapBookScreen;
import com.akitain.explorationreloaded.network.MapBookOpenPayload;
import com.akitain.explorationreloaded.network.MapBookSyncPayload;
import com.akitain.explorationreloaded.network.MapPositionPayload;
import com.akitain.explorationreloaded.registry.item.MapBookState;
import com.akitain.explorationreloaded.registry.item.MapBookStateManager;
import com.akitain.explorationreloaded.registry.item.MapStateAccessor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.map.MapState;

import java.util.ArrayList;

public final class ClientMapBookNetworking {
    private ClientMapBookNetworking() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(MapBookOpenPayload.PACKET_ID, ClientMapBookNetworking::mapBookOpen);
        ClientPlayNetworking.registerGlobalReceiver(MapBookSyncPayload.PACKET_ID, ClientMapBookNetworking::mapBookSync);
        ClientPlayNetworking.registerGlobalReceiver(MapPositionPayload.PACKET_ID, ClientMapBookNetworking::mapPosition);
    }

    private static void mapBookOpen(MapBookOpenPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> context.client().setScreen(new MapBookScreen(payload.itemStack())));
    }

    private static void mapBookSync(MapBookSyncPayload payload, ClientPlayNetworking.Context context) {
        if (payload.mapIDs().length == 0) return;

        context.client().execute(() -> {
            ArrayList<Integer> mapIds = new ArrayList<>();
            for (int mapId : payload.mapIDs()) {
                mapIds.add(mapId);
            }
            MapBookStateManager.INSTANCE.putClientMapBookState(
                    payload.bookID(),
                    new MapBookState(mapIds, payload.players(), payload.marker())
            );
        });
    }

    private static void mapPosition(MapPositionPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            ClientWorld world = context.client().world;
            if (world == null) return;

            MapState mapState = world.getMapState(payload.mapIdComponent());
            if (mapState == null) return;

            ((MapStateAccessor) mapState).explorationReloaded$setPosition(payload.centerX(), payload.centerZ());
        });
    }
}
