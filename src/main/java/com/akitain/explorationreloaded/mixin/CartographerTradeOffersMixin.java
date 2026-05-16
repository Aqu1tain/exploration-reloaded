package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.registry.ExplorationRegistries;
import com.akitain.explorationreloaded.registry.ExplorationTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.world.entity.npc.villager.VillagerTrades.TRADES;

@Mixin(VillagerTrades.class)
public class CartographerTradeOffersMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addRuinedPortalMapTrade(CallbackInfo ci) {
        TRADES.get(VillagerProfession.CARTOGRAPHER).replace(3, new VillagerTrades.ItemListing[]{
                new VillagerTrades.EmeraldForItems(Items.COMPASS, 1, 12, 20, 1),
                new VillagerTrades.TreasureMapForEmeralds(13, StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.OCEAN_MONUMENT, 12, 10),
                new VillagerTrades.TreasureMapForEmeralds(12, StructureTags.ON_TRIAL_CHAMBERS_MAPS, "filled_map.trial_chambers", MapDecorationTypes.TRIAL_CHAMBERS, 12, 10),
                new VillagerTrades.TreasureMapForEmeralds(12, ExplorationTags.ON_RUINED_PORTAL_MAPS, "filled_map.ruined_portal", ExplorationRegistries.RUINED_PORTAL, 12, 10)
        });
    }
}
