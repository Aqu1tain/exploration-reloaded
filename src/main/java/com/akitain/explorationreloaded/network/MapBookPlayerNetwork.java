package com.akitain.explorationreloaded.network;

import java.util.ArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;

public class MapBookPlayerNetwork {
    public static final StreamCodec<FriendlyByteBuf, ArrayList<MapBookPlayer>> ARRAY_CODEC = new StreamCodec<>() {
        public ArrayList<MapBookPlayer> decode(FriendlyByteBuf byteBuf) {
            int length = VarInt.read(byteBuf);
            ArrayList<MapBookPlayer> array = new ArrayList<>();
            for(int j = 0; j < length; j++) {
                array.add(MapBookPlayer.fromPacket(byteBuf));
            }
            return array;
        }

        public void encode(FriendlyByteBuf byteBuf, ArrayList<MapBookPlayer> array) {
            ArrayList<MapBookPlayer> array2 = (ArrayList<MapBookPlayer>) array.clone();
            VarInt.write(byteBuf, array.size());
            for (int i = 0; i < array.size();i++) {
                array2.get(i).toPacket(byteBuf);
            }
        }
    };
    public static final StreamCodec<FriendlyByteBuf, MapBookPlayer> SINGLE = new StreamCodec<>() {
        public MapBookPlayer decode(FriendlyByteBuf byteBuf) {
            return MapBookPlayer.fromPacket(byteBuf);
        }

        public void encode(FriendlyByteBuf byteBuf, MapBookPlayer mapBookPlayer) {
            mapBookPlayer.toPacket(byteBuf);
        }
    };
}
