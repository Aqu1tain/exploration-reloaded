package com.akitain.explorationreloaded.network;

public final class MapBookNetworking {
    private MapBookNetworking() {
    }

    public static void register() {
        MapBookOpenPayload.register();
        MapBookSyncPayload.register();
        MapPositionPayload.register();
        MapPositionRequestPayload.register();
    }
}
