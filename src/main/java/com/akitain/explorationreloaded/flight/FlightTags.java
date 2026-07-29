package com.akitain.explorationreloaded.flight;

import com.akitain.explorationreloaded.ExplorationReloaded;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class FlightTags {

    /** Blocks whose smoke lifts a glider. Data-driven so a pack can add its own chimneys. */
    public static final TagKey<Block> CREATES_UPDRAFT =
            TagKey.create(Registries.BLOCK, ExplorationReloaded.id("creates_updraft"));

    private FlightTags() {
    }
}
