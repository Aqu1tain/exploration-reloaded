package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.ExplorationCustomData;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderEntityTradeMixin {
    @Inject(method = "updateTrades", at = @At("TAIL"))
    private void replaceTrades(ServerLevel serverWorld, CallbackInfo ci) {
        WanderingTrader trader = (WanderingTrader) (Object) this;
        MerchantOffers offers = trader.getOffers();
        offers.clear();

        addOffers(trader, offers, buyingTrades(), 3 + randomInt(trader, 2));
        addOffers(trader, offers, uncommonTrades(trader), 2 + randomInt(trader, 2));
        addOffers(trader, offers, commonTrades(), 2 + randomInt(trader, 2));
        offers.add(sellingStack(createSpecialItem(trader), 10, 1, 1));
    }

    @Unique
    private void addOffers(WanderingTrader trader, MerchantOffers offers, List<Supplier<MerchantOffer>> tradeFactories, int count) {
        List<Supplier<MerchantOffer>> remainingTradeFactories = new ArrayList<>(tradeFactories);
        int offerCount = Math.min(count, remainingTradeFactories.size());

        for (int i = 0; i < offerCount; i++) {
            int index = randomInt(trader, remainingTradeFactories.size());
            offers.add(remainingTradeFactories.remove(index).get());
        }
    }

    @Unique
    private List<Supplier<MerchantOffer>> buyingTrades() {
        return List.of(
                () -> buying(createWaterPotionTradeItem(), 1, 1),
                () -> buying(Items.WATER_BUCKET, 1, 1, 2),
                () -> buying(Items.MILK_BUCKET, 1, 1, 2),
                () -> buying(Items.FERMENTED_SPIDER_EYE, 1, 1, 3),
                () -> buying(Items.BAKED_POTATO, 4, 1, 1),
                () -> buying(Items.HAY_BLOCK, 2, 1, 1),
                () -> buying(Items.GOLDEN_CARROT, 2, 1, 1),
                () -> buying(Items.PUMPKIN_PIE, 2, 1, 1),
                () -> buying(Items.BEETROOT_SOUP, 1, 1, 1),
                () -> buying(Items.COMPASS, 1, 1, 1),
                () -> buying(Items.LEAD, 2, 1, 1),
                () -> buying(Items.COOKIE, 2, 1, 1)
        );
    }

    @Unique
    private List<Supplier<MerchantOffer>> uncommonTrades(WanderingTrader trader) {
        return List.of(
                () -> selling(Items.PACKED_ICE, 1, 1, 6, 1),
                () -> selling(Items.BLUE_ICE, 6, 1, 6, 1),
                () -> selling(Items.GUNPOWDER, 1, 4, 2, 1),
                () -> selling(Items.PODZOL, 3, 3, 6, 1),
                () -> selling(Blocks.ACACIA_LOG, 1, 8, 4, 1),
                () -> selling(Blocks.BIRCH_LOG, 1, 8, 4, 1),
                () -> selling(Blocks.DARK_OAK_LOG, 1, 8, 4, 1),
                () -> selling(Blocks.JUNGLE_LOG, 1, 8, 4, 1),
                () -> selling(Blocks.OAK_LOG, 1, 8, 4, 1),
                () -> selling(Blocks.SPRUCE_LOG, 1, 8, 4, 1),
                () -> selling(Blocks.CHERRY_LOG, 1, 8, 4, 1),
                () -> sellingStack(createEnchantedPickaxeStack(trader), 1, 1, 1),
                () -> sellingStack(createInvisibilityPotionStack(), 5, 1, 1),
                () -> selling(Items.NAUTILUS_SHELL, 5, 1, 5, 1)
        );
    }

    @Unique
    private List<Supplier<MerchantOffer>> commonTrades() {
        return List.of(
                () -> selling(Items.TROPICAL_FISH_BUCKET, 3, 1, 4, 1),
                () -> selling(Items.PUFFERFISH_BUCKET, 3, 1, 4, 1),
                () -> selling(Items.SEA_PICKLE, 2, 1, 5, 1),
                () -> selling(Items.SLIME_BALL, 4, 1, 5, 1),
                () -> selling(Items.GLOWSTONE, 2, 1, 5, 1),
                () -> selling(Items.FERN, 1, 1, 12, 1),
                () -> selling(Items.SUGAR_CANE, 1, 1, 8, 1),
                () -> selling(Items.PUMPKIN, 1, 1, 4, 1),
                () -> selling(Items.KELP, 3, 1, 12, 1),
                () -> selling(Items.CACTUS, 3, 1, 8, 1),
                () -> selling(Items.DANDELION, 1, 1, 12, 1),
                () -> selling(Items.POPPY, 1, 1, 12, 1),
                () -> selling(Items.BLUE_ORCHID, 1, 1, 8, 1),
                () -> selling(Items.ALLIUM, 1, 1, 12, 1),
                () -> selling(Items.AZURE_BLUET, 1, 1, 12, 1),
                () -> selling(Items.RED_TULIP, 1, 1, 12, 1),
                () -> selling(Items.ORANGE_TULIP, 1, 1, 12, 1),
                () -> selling(Items.WHITE_TULIP, 1, 1, 12, 1),
                () -> selling(Items.PINK_TULIP, 1, 1, 12, 1),
                () -> selling(Items.OXEYE_DAISY, 1, 1, 12, 1),
                () -> selling(Items.CORNFLOWER, 1, 1, 12, 1),
                () -> selling(Items.LILY_OF_THE_VALLEY, 1, 1, 7, 1),
                () -> selling(Items.WHEAT_SEEDS, 1, 1, 12, 1),
                () -> selling(Items.BEETROOT_SEEDS, 1, 1, 12, 1),
                () -> selling(Items.PUMPKIN_SEEDS, 1, 1, 12, 1),
                () -> selling(Items.MELON_SEEDS, 1, 1, 12, 1),
                () -> selling(Items.ACACIA_SAPLING, 5, 1, 8, 1),
                () -> selling(Items.BIRCH_SAPLING, 5, 1, 8, 1),
                () -> selling(Items.DARK_OAK_SAPLING, 5, 1, 8, 1),
                () -> selling(Items.JUNGLE_SAPLING, 5, 1, 8, 1),
                () -> selling(Items.OAK_SAPLING, 5, 1, 8, 1),
                () -> selling(Items.SPRUCE_SAPLING, 5, 1, 8, 1),
                () -> selling(Items.CHERRY_SAPLING, 5, 1, 8, 1),
                () -> selling(Items.MANGROVE_PROPAGULE, 5, 1, 8, 1),
                () -> selling(Items.RED_DYE, 1, 3, 12, 1),
                () -> selling(Items.WHITE_DYE, 1, 3, 12, 1),
                () -> selling(Items.BLUE_DYE, 1, 3, 12, 1),
                () -> selling(Items.PINK_DYE, 1, 3, 12, 1),
                () -> selling(Items.BLACK_DYE, 1, 3, 12, 1),
                () -> selling(Items.GREEN_DYE, 1, 3, 12, 1),
                () -> selling(Items.LIGHT_GRAY_DYE, 1, 3, 12, 1),
                () -> selling(Items.MAGENTA_DYE, 1, 3, 12, 1),
                () -> selling(Items.YELLOW_DYE, 1, 3, 12, 1),
                () -> selling(Items.GRAY_DYE, 1, 3, 12, 1),
                () -> selling(Items.PURPLE_DYE, 1, 3, 12, 1),
                () -> selling(Items.LIGHT_BLUE_DYE, 1, 3, 12, 1),
                () -> selling(Items.LIME_DYE, 1, 3, 12, 1),
                () -> selling(Items.ORANGE_DYE, 1, 3, 12, 1),
                () -> selling(Items.BROWN_DYE, 1, 3, 12, 1),
                () -> selling(Items.CYAN_DYE, 1, 3, 12, 1),
                () -> selling(Items.BRAIN_CORAL_BLOCK, 3, 1, 8, 1),
                () -> selling(Items.BUBBLE_CORAL_BLOCK, 3, 1, 8, 1),
                () -> selling(Items.FIRE_CORAL_BLOCK, 3, 1, 8, 1),
                () -> selling(Items.HORN_CORAL_BLOCK, 3, 1, 8, 1),
                () -> selling(Items.TUBE_CORAL_BLOCK, 3, 1, 8, 1),
                () -> selling(Items.VINE, 1, 3, 4, 1),
                () -> selling(Items.BROWN_MUSHROOM, 1, 3, 4, 1),
                () -> selling(Items.RED_MUSHROOM, 1, 3, 4, 1),
                () -> selling(Items.LILY_PAD, 1, 5, 2, 1),
                () -> selling(Items.SMALL_DRIPLEAF, 1, 2, 5, 1),
                () -> selling(Items.SAND, 1, 8, 8, 1),
                () -> selling(Items.RED_SAND, 1, 4, 6, 1),
                () -> selling(Items.POINTED_DRIPSTONE, 1, 2, 5, 1),
                () -> selling(Items.ROOTED_DIRT, 1, 2, 5, 1),
                () -> selling(Items.MOSS_BLOCK, 1, 2, 5, 1)
        );
    }

    @Unique
    private MerchantOffer buying(ItemLike item, int count, int maxUses, int xp) {
        return buying(new ItemCost(item, count), maxUses, xp);
    }

    @Unique
    private MerchantOffer buying(ItemCost cost, int maxUses, int xp) {
        return new MerchantOffer(cost, new ItemStack(Items.EMERALD), maxUses, xp, 0.05F);
    }

    @Unique
    private MerchantOffer selling(ItemLike item, int emeralds, int count, int maxUses, int xp) {
        return sellingStack(new ItemStack(item, count), emeralds, maxUses, xp);
    }

    @Unique
    private MerchantOffer sellingStack(ItemStack stack, int emeralds, int maxUses, int xp) {
        return new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), stack, maxUses, xp, 0.05F);
    }

    @Unique
    private ItemCost createWaterPotionTradeItem() {
        return new ItemCost(Items.POTION)
                .withComponents(builder -> builder.expect(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER)));
    }

    @Unique
    private ItemStack createInvisibilityPotionStack() {
        return PotionContents.createItemStack(Items.POTION, Potions.LONG_INVISIBILITY);
    }

    @Unique
    private ItemStack createEnchantedPickaxeStack(WanderingTrader trader) {
        ItemStack pickaxe = Items.IRON_PICKAXE.getDefaultInstance();
        return EnchantmentHelper.enchantItem(trader.getRandom(), pickaxe, 5 + randomInt(trader, 15), trader.registryAccess(), Optional.empty());
    }

    @Unique
    private ItemStack createSpecialItem(WanderingTrader trader) {
        return switch (randomInt(trader, 5)) {
            case 0 -> createMusicDiscStack(trader);
            case 1 -> createSherdStack(trader);
            case 2 -> createTrimStack(trader);
            case 3 -> createMobHeadStack(trader);
            default -> createBiomeMapStack(trader);
        };
    }

    @Unique
    private ItemStack createMusicDiscStack(WanderingTrader trader) {
        Item[] discs = {Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR,
                Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD,
                Items.MUSIC_DISC_11, Items.MUSIC_DISC_WAIT, Items.MUSIC_DISC_PIGSTEP, Items.MUSIC_DISC_OTHERSIDE, Items.MUSIC_DISC_5,
                Items.MUSIC_DISC_RELIC, Items.MUSIC_DISC_CREATOR, Items.MUSIC_DISC_CREATOR_MUSIC_BOX, Items.MUSIC_DISC_PRECIPICE};
        return discs[randomInt(trader, discs.length)].getDefaultInstance();
    }

    @Unique
    private ItemStack createSherdStack(WanderingTrader trader) {
        Item[] sherds = {Items.ANGLER_POTTERY_SHERD, Items.ARCHER_POTTERY_SHERD, Items.ARMS_UP_POTTERY_SHERD, Items.BLADE_POTTERY_SHERD,
                Items.BREWER_POTTERY_SHERD, Items.BURN_POTTERY_SHERD, Items.DANGER_POTTERY_SHERD, Items.EXPLORER_POTTERY_SHERD,
                Items.FLOW_POTTERY_SHERD, Items.FRIEND_POTTERY_SHERD, Items.GUSTER_POTTERY_SHERD, Items.HEART_POTTERY_SHERD,
                Items.HEARTBREAK_POTTERY_SHERD, Items.HOWL_POTTERY_SHERD, Items.MINER_POTTERY_SHERD, Items.MOURNER_POTTERY_SHERD,
                Items.PLENTY_POTTERY_SHERD, Items.PRIZE_POTTERY_SHERD, Items.SCRAPE_POTTERY_SHERD, Items.SHEAF_POTTERY_SHERD,
                Items.SHELTER_POTTERY_SHERD, Items.SKULL_POTTERY_SHERD, Items.SNORT_POTTERY_SHERD};
        return sherds[randomInt(trader, sherds.length)].getDefaultInstance();
    }

    @Unique
    private ItemStack createTrimStack(WanderingTrader trader) {
        Item[] trims = {Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE};
        return trims[randomInt(trader, trims.length)].getDefaultInstance();
    }

    @Unique
    private ItemStack createMobHeadStack(WanderingTrader trader) {
        Item[] heads = {Items.ZOMBIE_HEAD, Items.SKELETON_SKULL, Items.CREEPER_HEAD, Items.WITHER_SKELETON_SKULL, Items.PIGLIN_HEAD, Items.PLAYER_HEAD};
        ItemStack head = heads[randomInt(trader, heads.length)].getDefaultInstance();
        if (head.is(Items.PLAYER_HEAD)) {
            setProfile(head, trader);
        }
        return head;
    }

    @Unique
    private void setProfile(ItemStack head, WanderingTrader trader) {
        MinecraftServer server = trader.level().getServer();
        if (server == null) {
            return;
        }

        ProfileResolver resolver = server.services().profileResolver();
        String[] names = {"green_jab", "Rellati"};
        Optional<GameProfile> profile = resolver.fetchByName(names[randomInt(trader, names.length)]);
        if (profile.isPresent()) {
            head.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile.get()));
            return;
        }

        Player player = trader.level().getNearestPlayer(trader, 100);
        if (player != null) {
            head.set(DataComponents.PROFILE, ResolvableProfile.createResolved(player.getGameProfile()));
        }
    }

    @Unique
    private ItemStack createBiomeMapStack(WanderingTrader trader) {
        Level world = trader.level();
        if (!(world instanceof ServerLevel serverWorld)) {
            return Items.MAP.getDefaultInstance();
        }

        int map = randomInt(trader, 6);
        ExplorationCustomData.biomeSearch = biomeKey(map);

        List<ResourceKey<Biome>> biomes = List.of();
        Set<ResourceKey<Biome>> copiedBiomes = Set.copyOf(biomes);
        Objects.requireNonNull(copiedBiomes);
        Predicate<Holder<Biome>> predicate = copiedBiomes::contains;
        com.mojang.datafixers.util.Pair<BlockPos, Holder<Biome>> pair = serverWorld.findClosestBiome3d(predicate.negate(), trader.blockPosition(), 6400, 32, 64);
        if (pair == null) {
            return Items.MAP.getDefaultInstance();
        }

        BlockPos position = pair.getFirst();
        ItemStack mapStack = MapItem.create(serverWorld, position.getX(), position.getZ(), (byte) 2, true, true);
        MapItem.renderBiomePreviewMap(serverWorld, mapStack);

        String[] names = {"mushroom_fields", "cherry_grove", "ice_spikes", "badlands", "warm_ocean", "pale_garden"};
        int[] colors = {7412448, 16751570, 4639231, 16725801, 1938431, 10856879};
        mapStack.set(DataComponents.ITEM_NAME, Component.translatable("filled_map.explorer", Component.translatable("biome.minecraft." + names[map])));
        MapItemSavedData mapState = MapItem.getSavedData(mapStack, serverWorld);
        if (mapState != null) {
            mapState.toggleBanner(serverWorld, new BlockPos(position.getX(), -1000 - map, position.getZ()));
        }
        mapStack.set(DataComponents.MAP_COLOR, new MapItemColor(colors[map]));
        return mapStack;
    }

    @Unique
    private ResourceKey<Biome> biomeKey(int map) {
        return switch (map) {
            case 0 -> Biomes.MUSHROOM_FIELDS;
            case 1 -> Biomes.CHERRY_GROVE;
            case 2 -> Biomes.ICE_SPIKES;
            case 3 -> Biomes.BADLANDS;
            case 4 -> Biomes.WARM_OCEAN;
            case 5 -> Biomes.PALE_GARDEN;
            default -> Biomes.FOREST;
        };
    }

    @Unique
    private int randomInt(WanderingTrader trader, int bound) {
        return trader.getRandom().nextInt(bound);
    }
}
