package com.akitain.fixedexploration.registry;

import com.akitain.fixedexploration.FixedExploration;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class ExplorationTags {
    public static final TagKey<Structure> LODESTONE_COMPASS = structure("lodestone_compass");
    public static final TagKey<Structure> ON_OUTPOST_MAPS = structure("on_outpost_maps");
    public static final TagKey<Structure> ON_RUINED_PORTAL_MAPS = structure("on_ruined_portal_maps");
    public static final TagKey<Structure> ON_TRAIL_RUINS_MAPS = structure("on_trail_ruins_maps");

    private ExplorationTags() {
    }

    private static TagKey<Structure> structure(String name) {
        return TagKey.create(Registries.STRUCTURE, FixedExploration.id(name));
    }
}
