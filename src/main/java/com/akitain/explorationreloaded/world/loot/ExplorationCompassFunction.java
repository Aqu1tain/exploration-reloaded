package com.akitain.explorationreloaded.world.loot;

import com.akitain.explorationreloaded.registry.ExplorationRegistries;
import com.akitain.explorationreloaded.registry.ExplorationTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.ExplorationMapLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.structure.Structure;

public class ExplorationCompassFunction extends ConditionalLootFunction {
    public static final int DEFAULT_COLOR = 32767;
    public static final int DEFAULT_SEARCH_RADIUS = 50;
    public static final boolean DEFAULT_SKIP_EXISTING_CHUNKS = true;
    public static final MapCodec<ExplorationCompassFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> addConditionsField(instance)
                    .and(instance.group(
                            TagKey.unprefixedCodec(RegistryKeys.STRUCTURE)
                                    .optionalFieldOf("destination", ExplorationTags.LODESTONE_COMPASS)
                                    .forGetter(function -> function.destination),
                            Codec.INT.optionalFieldOf("color", DEFAULT_COLOR).forGetter(function -> function.color),
                            Codec.INT.optionalFieldOf("search_radius", DEFAULT_SEARCH_RADIUS).forGetter(function -> function.searchRadius),
                            Codec.BOOL.optionalFieldOf("skip_existing_chunks", DEFAULT_SKIP_EXISTING_CHUNKS).forGetter(function -> function.skipExistingChunks)
                    ))
                    .apply(instance, ExplorationCompassFunction::new)
    );

    private final TagKey<Structure> destination;
    private final int color;
    private final int searchRadius;
    private final boolean skipExistingChunks;

    public ExplorationCompassFunction(
            List<LootCondition> conditions,
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
    public LootFunctionType<ExplorationCompassFunction> getType() {
        return ExplorationRegistries.EXPLORATION_COMPASS;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(LootContextParameters.ORIGIN);
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (!stack.isOf(Items.COMPASS)) {
            return stack;
        }

        Vec3d origin = context.get(LootContextParameters.ORIGIN);
        if (origin == null) {
            return stack;
        }

        ServerWorld world = context.getWorld();
        BlockPos structurePos = world.locateStructure(destination, BlockPos.ofFloored(origin), searchRadius, skipExistingChunks);
        if (structurePos == null) {
            return stack;
        }

        ItemStack compass = Items.COMPASS.getDefaultStack();
        compass.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.of(GlobalPos.create(world.getRegistryKey(), structurePos.withY(-49))), true));
        compass.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color));
        return compass;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ConditionalLootFunction.Builder<Builder> {
        private int color = DEFAULT_COLOR;
        private TagKey<Structure> destination = ExplorationMapLootFunction.DEFAULT_DESTINATION;
        private int searchRadius = DEFAULT_SEARCH_RADIUS;
        private boolean skipExistingChunks = DEFAULT_SKIP_EXISTING_CHUNKS;

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        public Builder withColor(int color) {
            this.color = color;
            return this;
        }

        public Builder withDestination(TagKey<Structure> destination) {
            this.destination = destination;
            return this;
        }

        public Builder searchRadius(int searchRadius) {
            this.searchRadius = searchRadius;
            return this;
        }

        public Builder withSkipExistingChunks(boolean skipExistingChunks) {
            this.skipExistingChunks = skipExistingChunks;
            return this;
        }

        @Override
        public LootFunction build() {
            return new ExplorationCompassFunction(getConditions(), destination, color, searchRadius, skipExistingChunks);
        }
    }
}
