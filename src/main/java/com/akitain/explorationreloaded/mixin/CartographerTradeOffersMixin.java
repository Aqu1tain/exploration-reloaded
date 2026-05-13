package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.registry.ExplorationRegistries;
import com.akitain.explorationreloaded.registry.ExplorationTags;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.village.TradeOffers.PROFESSION_TO_LEVELED_TRADE;

@Mixin(TradeOffers.class)
public class CartographerTradeOffersMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addRuinedPortalMapTrade(CallbackInfo ci) {
        PROFESSION_TO_LEVELED_TRADE.get(VillagerProfession.CARTOGRAPHER).replace(3, new TradeOffers.Factory[]{
                new TradeOffers.BuyItemFactory(Items.COMPASS, 1, 12, 20, 1),
                new TradeOffers.SellMapFactory(13, StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.MONUMENT, 12, 10),
                new TradeOffers.SellMapFactory(12, StructureTags.ON_TRIAL_CHAMBERS_MAPS, "filled_map.trial_chambers", MapDecorationTypes.TRIAL_CHAMBERS, 12, 10),
                new TradeOffers.SellMapFactory(12, ExplorationTags.ON_RUINED_PORTAL_MAPS, "filled_map.ruined_portal", ExplorationRegistries.RUINED_PORTAL, 12, 10)
        });
    }
}
