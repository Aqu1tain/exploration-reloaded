package com.akitain.explorationreloaded.world.loot;

import com.akitain.explorationreloaded.registry.ExplorationRegistries;
import com.akitain.explorationreloaded.registry.ExplorationTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

public class ExplorationCompassFunction extends LootItemConditionalFunction {
    public static final int DEFAULT_COLOR = 32767;
    public static final int DEFAULT_SEARCH_RADIUS = 50;
    public static final boolean DEFAULT_SKIP_EXISTING_CHUNKS = true;
    public static final MapCodec<ExplorationCompassFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance)
                    .and(instance.group(
                            TagKey.hashedCodec(Registries.STRUCTURE)
                                    .optionalFieldOf("destination", ExplorationTags.LODESTONE_COMPASS)
                                    .forGetter(function -> function.destination),
                            Codec.INT.optionalFieldOf("color", DEFAULT_COLOR)
                                    .forGetter(function -> function.color),
                            Codec.INT.optionalFieldOf("search_radius", DEFAULT_SEARCH_RADIUS)
                                    .forGetter(function -> function.searchRadius),
                            Codec.BOOL.optionalFieldOf("skip_existing_chunks", DEFAULT_SKIP_EXISTING_CHUNKS)
                                    .forGetter(function -> function.skipExistingChunks)
                    ))
                    .apply(instance, ExplorationCompassFunction::new)
    );

    private final TagKey<Structure> destination;
    private final int color;
    private final int searchRadius;
    private final boolean skipExistingChunks;

    public ExplorationCompassFunction(
            List<LootItemCondition> conditions,
            TagKey<Structure> destination,
            int color,
            int searchRadius,
            boolean skipExistingChunks
    ) {
        super(conditions);
        this.destination = destination;
        this.color = color;
        this.searchRadius = searchRadius;
        this.skipExistingChunks = skipExistingChunks;
    }

    @Override
    public MapCodec<ExplorationCompassFunction> codec() {
        return ExplorationCompassFunction.CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (stack.getItem() != Items.COMPASS) {
            return stack;
        }

        Vec3 origin = context.getOptionalParameter(LootContextParams.ORIGIN);
        if (origin == null) {
            return stack;
        }

        ServerLevel level = context.getLevel();
        BlockPos structurePos = level.findNearestMapStructure(destination, BlockPos.containing(origin), searchRadius, skipExistingChunks);
        if (structurePos == null) {
            return stack;
        }

        ItemStack compass = Items.COMPASS.getDefaultInstance();
        compass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(), structurePos.atY(-49))), true));
        compass.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        return compass;
    }
}
