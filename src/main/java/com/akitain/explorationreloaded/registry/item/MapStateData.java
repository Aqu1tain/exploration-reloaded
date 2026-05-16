package com.akitain.explorationreloaded.registry.item;

import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapStateData {
    public MapId id;
    public MapItemSavedData mapState;
    public MapStateData(MapId id, MapItemSavedData mapState){
        this.id = id;
        this.mapState = mapState;
    }
}
