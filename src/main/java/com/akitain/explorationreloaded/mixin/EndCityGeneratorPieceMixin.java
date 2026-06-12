package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.ExplorationReloaded;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.EndCityGenerator;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EndCityGenerator.Piece.class)
public abstract class EndCityGeneratorPieceMixin {
    private static final Identifier END_SHIP_TEMPLATE = Identifier.ofVanilla("end_city/ship");
    private static final RegistryKey<LootTable> END_SHIP_LOOT_TABLE = RegistryKey.of(RegistryKeys.LOOT_TABLE, ExplorationReloaded.id("end_ship_loot"));

    @Shadow
    protected abstract Identifier getId();

    @ModifyArg(method = "handleMetadata", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/LootableInventory;setLootTable(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/registry/RegistryKey;)V"), index = 3)
    private RegistryKey<LootTable> useEndShipLootTable(RegistryKey<LootTable> lootTable) {
        if (!END_SHIP_TEMPLATE.equals(this.getId())) {
            return lootTable;
        }

        return END_SHIP_LOOT_TABLE;
    }
}
