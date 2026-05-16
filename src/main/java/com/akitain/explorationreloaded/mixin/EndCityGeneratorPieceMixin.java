package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.ExplorationReloaded;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.structures.EndCityPieces;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EndCityPieces.EndCityPiece.class)
public class EndCityGeneratorPieceMixin {
    private static final ResourceKey<LootTable> END_SHIP_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, ExplorationReloaded.id("end_ship_loot"));

    @Shadow
    @Final
    protected String templateName;

    @ModifyArg(method = "handleDataMarker", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/RandomizableContainer;setBlockEntityLootTable(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/resources/ResourceKey;)V"), index = 3)
    private ResourceKey<LootTable> useEndShipLootTable(ResourceKey<LootTable> lootTable) {
        if (!"ship".equals(this.templateName)) {
            return lootTable;
        }

        return END_SHIP_LOOT_TABLE;
    }
}
