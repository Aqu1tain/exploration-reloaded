package com.akitain.explorationreloaded.registry.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class MapBookIdCountsState extends SavedData {
    public static final Codec<MapBookIdCountsState> CODEC = RecordCodecBuilder.create(
             instance -> instance.group(
                     Codec.INT.optionalFieldOf("exploration-reloaded:map_book", -1).forGetter( state -> state.nextMapBookId))
                    .apply(instance, MapBookIdCountsState::new)
    );

    public static String IDCOUNTS_KEY = "exploration-reloaded_idcounts";

    int nextMapBookId;
    public MapBookIdCountsState() {
        nextMapBookId = -1;
    }

    public MapBookIdCountsState(int nextMapBookId) {
        this.nextMapBookId = nextMapBookId;
    }

    public int get() {
        nextMapBookId++;
        this.setDirty();
        return nextMapBookId;
    }
    public static final SavedDataType<MapBookIdCountsState> persistentStateType = new SavedDataType<>(
            IDCOUNTS_KEY, MapBookIdCountsState::new, CODEC, DataFixTypes.SAVED_DATA_MAP_INDEX
    );

}
