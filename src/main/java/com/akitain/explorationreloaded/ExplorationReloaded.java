package com.akitain.explorationreloaded;

import com.akitain.explorationreloaded.network.MapBookNetworking;
import com.akitain.explorationreloaded.registry.ExplorationRegistries;
import com.akitain.explorationreloaded.registry.ExplorationComponents;
import com.akitain.explorationreloaded.registry.ExplorationItems;
import com.akitain.explorationreloaded.registry.item.MapBookState;
import com.akitain.explorationreloaded.registry.item.MapBookStateManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExplorationReloaded implements ModInitializer {
    public static final String MOD_ID = "exploration-reloaded";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ExplorationComponents.register();
        ExplorationItems.register();
        ExplorationRegistries.register();
        MapBookNetworking.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("mapBookMarker")
                        .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                .then(CommandManager.argument("x", StringArgumentType.string())
                                        .then(CommandManager.argument("z", StringArgumentType.string())
                                                .then(CommandManager.argument("dim", StringArgumentType.string())
                                                        .executes(ExplorationReloaded::executeMapBookMarker)))))));
        LOGGER.info("Exploration Reloaded loaded");
    }

    private static int executeMapBookMarker(CommandContext<ServerCommandSource> context) {
        int id = IntegerArgumentType.getInteger(context, "id");
        double x = Double.parseDouble(StringArgumentType.getString(context, "x"));
        double z = Double.parseDouble(StringArgumentType.getString(context, "z"));
        String dimension = StringArgumentType.getString(context, "dim");
        MapBookState mapBookState = MapBookStateManager.INSTANCE.getMapBookState(context.getSource().getServer(), id);
        if (mapBookState != null) {
            mapBookState.setMarker(x, z, dimension);
        }
        return 1;
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
