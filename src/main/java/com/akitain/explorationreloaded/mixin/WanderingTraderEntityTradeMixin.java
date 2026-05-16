package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.ExplorationCustomData;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
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
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderEntityTradeMixin {
    @ModifyExpressionValue(method = "updateTrades", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/npc/villager/VillagerTrades;WANDERING_TRADER_TRADES:Ljava/util/List;"))
    private List<Pair<VillagerTrades.ItemListing[], Integer>> replaceTrades(List<Pair<VillagerTrades.ItemListing[], Integer>> original) {
        return List.of(
                Pair.of(new VillagerTrades.ItemListing[]{
                        new VillagerTrades.EmeraldForItems(createWaterPotionTradeItem(), 1, 1, 1),
                        new VillagerTrades.EmeraldForItems(Items.WATER_BUCKET, 1, 1, 1, 2),
                        new VillagerTrades.EmeraldForItems(Items.MILK_BUCKET, 1, 1, 1, 2),
                        new VillagerTrades.EmeraldForItems(Items.FERMENTED_SPIDER_EYE, 1, 1, 1, 3),
                        new VillagerTrades.EmeraldForItems(Items.BAKED_POTATO, 4, 1, 1, 1),
                        new VillagerTrades.EmeraldForItems(Items.HAY_BLOCK, 2, 1, 1, 1),
                        new VillagerTrades.EmeraldForItems(Items.GOLDEN_CARROT, 2, 1, 1, 1),
                        new VillagerTrades.EmeraldForItems(Items.PUMPKIN_PIE, 2, 1, 1, 1),
                        new VillagerTrades.EmeraldForItems(Items.BEETROOT_SOUP, 1, 1, 1, 1),
                        new VillagerTrades.EmeraldForItems(Items.COMPASS, 1, 1, 1, 1),
                        new VillagerTrades.EmeraldForItems(Items.LEAD, 2, 1, 1, 1),
                        new VillagerTrades.EmeraldForItems(Items.COOKIE, 2, 1, 1, 1)
                }, 3 + randomInt(2)),
                Pair.of(new VillagerTrades.ItemListing[]{
                        new VillagerTrades.ItemsForEmeralds(Items.PACKED_ICE, 1, 1, 6, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.BLUE_ICE, 6, 1, 6, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.GUNPOWDER, 1, 4, 2, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.PODZOL, 3, 3, 6, 1),
                        new VillagerTrades.ItemsForEmeralds(Blocks.ACACIA_LOG, 1, 8, 4, 1),
                        new VillagerTrades.ItemsForEmeralds(Blocks.BIRCH_LOG, 1, 8, 4, 1),
                        new VillagerTrades.ItemsForEmeralds(Blocks.DARK_OAK_LOG, 1, 8, 4, 1),
                        new VillagerTrades.ItemsForEmeralds(Blocks.JUNGLE_LOG, 1, 8, 4, 1),
                        new VillagerTrades.ItemsForEmeralds(Blocks.OAK_LOG, 1, 8, 4, 1),
                        new VillagerTrades.ItemsForEmeralds(Blocks.SPRUCE_LOG, 1, 8, 4, 1),
                        new VillagerTrades.ItemsForEmeralds(Blocks.CHERRY_LOG, 1, 8, 4, 1),
                        new VillagerTrades.EnchantedItemForEmeralds(Items.IRON_PICKAXE, 1, 1, 1, 0.2F),
                        new VillagerTrades.ItemsForEmeralds(createInvisibilityPotionStack(), 5, 1, 1, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.NAUTILUS_SHELL, 5, 1, 5, 1)
                }, 2 + randomInt(2)),
                Pair.of(new VillagerTrades.ItemListing[]{
                        new VillagerTrades.ItemsForEmeralds(Items.TROPICAL_FISH_BUCKET, 3, 1, 4, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.PUFFERFISH_BUCKET, 3, 1, 4, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.SEA_PICKLE, 2, 1, 5, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.SLIME_BALL, 4, 1, 5, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.GLOWSTONE, 2, 1, 5, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.FERN, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.SUGAR_CANE, 1, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.PUMPKIN, 1, 1, 4, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.KELP, 3, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.CACTUS, 3, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.DANDELION, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.POPPY, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.BLUE_ORCHID, 1, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.ALLIUM, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.AZURE_BLUET, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.RED_TULIP, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.ORANGE_TULIP, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.WHITE_TULIP, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.PINK_TULIP, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.OXEYE_DAISY, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.CORNFLOWER, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.LILY_OF_THE_VALLEY, 1, 1, 7, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.WHEAT_SEEDS, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.BEETROOT_SEEDS, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.PUMPKIN_SEEDS, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.MELON_SEEDS, 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.ACACIA_SAPLING, 5, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.BIRCH_SAPLING, 5, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.DARK_OAK_SAPLING, 5, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.JUNGLE_SAPLING, 5, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.OAK_SAPLING, 5, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.SPRUCE_SAPLING, 5, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.CHERRY_SAPLING, 5, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.MANGROVE_PROPAGULE, 5, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.RED_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.WHITE_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.BLUE_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.PINK_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.BLACK_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.GREEN_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.LIGHT_GRAY_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.MAGENTA_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.YELLOW_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.GRAY_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.PURPLE_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.LIGHT_BLUE_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.LIME_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.ORANGE_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.BROWN_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.CYAN_DYE, 1, 3, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.BRAIN_CORAL_BLOCK, 3, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.BUBBLE_CORAL_BLOCK, 3, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.FIRE_CORAL_BLOCK, 3, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.HORN_CORAL_BLOCK, 3, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.TUBE_CORAL_BLOCK, 3, 1, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.VINE, 1, 3, 4, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.BROWN_MUSHROOM, 1, 3, 4, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.RED_MUSHROOM, 1, 3, 4, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.LILY_PAD, 1, 5, 2, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.SMALL_DRIPLEAF, 1, 2, 5, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.SAND, 1, 8, 8, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.RED_SAND, 1, 4, 6, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.POINTED_DRIPSTONE, 1, 2, 5, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.ROOTED_DIRT, 1, 2, 5, 1),
                        new VillagerTrades.ItemsForEmeralds(Items.MOSS_BLOCK, 1, 2, 5, 1)
                }, 2 + randomInt(2)),
                Pair.of(new VillagerTrades.ItemListing[]{
                        new VillagerTrades.ItemsForEmeralds(createSpecialItem(), 10, 1, 1, 1)
                }, 1)
        );
    }

    @Unique
    private ItemStack createSpecialItem() {
        return switch (randomInt(5)) {
            case 0 -> createMusicDiscStack();
            case 1 -> createSherdStack();
            case 2 -> createTrimStack();
            case 3 -> createMobHeadStack();
            default -> createBiomeMapStack();
        };
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
    private ItemStack createMusicDiscStack() {
        Item[] discs = {Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR,
                Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD,
                Items.MUSIC_DISC_11, Items.MUSIC_DISC_WAIT, Items.MUSIC_DISC_PIGSTEP, Items.MUSIC_DISC_OTHERSIDE, Items.MUSIC_DISC_5,
                Items.MUSIC_DISC_RELIC, Items.MUSIC_DISC_CREATOR, Items.MUSIC_DISC_CREATOR_MUSIC_BOX, Items.MUSIC_DISC_PRECIPICE};
        return discs[randomInt(discs.length)].getDefaultInstance();
    }

    @Unique
    private ItemStack createSherdStack() {
        Item[] sherds = {Items.ANGLER_POTTERY_SHERD, Items.ARCHER_POTTERY_SHERD, Items.ARMS_UP_POTTERY_SHERD, Items.BLADE_POTTERY_SHERD,
                Items.BREWER_POTTERY_SHERD, Items.BURN_POTTERY_SHERD, Items.DANGER_POTTERY_SHERD, Items.EXPLORER_POTTERY_SHERD,
                Items.FLOW_POTTERY_SHERD, Items.FRIEND_POTTERY_SHERD, Items.GUSTER_POTTERY_SHERD, Items.HEART_POTTERY_SHERD,
                Items.HEARTBREAK_POTTERY_SHERD, Items.HOWL_POTTERY_SHERD, Items.MINER_POTTERY_SHERD, Items.MOURNER_POTTERY_SHERD,
                Items.PLENTY_POTTERY_SHERD, Items.PRIZE_POTTERY_SHERD, Items.SCRAPE_POTTERY_SHERD, Items.SHEAF_POTTERY_SHERD,
                Items.SHELTER_POTTERY_SHERD, Items.SKULL_POTTERY_SHERD, Items.SNORT_POTTERY_SHERD};
        return sherds[randomInt(sherds.length)].getDefaultInstance();
    }

    @Unique
    private ItemStack createTrimStack() {
        Item[] trims = {Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE};
        return trims[randomInt(trims.length)].getDefaultInstance();
    }

    @Unique
    private ItemStack createMobHeadStack() {
        Item[] heads = {Items.ZOMBIE_HEAD, Items.SKELETON_SKULL, Items.CREEPER_HEAD, Items.WITHER_SKELETON_SKULL, Items.PIGLIN_HEAD, Items.PLAYER_HEAD};
        ItemStack head = heads[randomInt(heads.length)].getDefaultInstance();
        if (head.is(Items.PLAYER_HEAD)) {
            setProfile(head);
        }
        return head;
    }

    @Unique
    private void setProfile(ItemStack head) {
        WanderingTrader trader = (WanderingTrader) (Object) this;
        MinecraftServer server = trader.level().getServer();
        if (server == null) {
            return;
        }

        ProfileResolver resolver = server.services().profileResolver();
        String[] names = {"green_jab", "Rellati"};
        Optional<GameProfile> profile = resolver.fetchByName(names[server.overworld().random.nextInt(names.length)]);
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
    private ItemStack createBiomeMapStack() {
        WanderingTrader trader = (WanderingTrader) (Object) this;
        Level world = trader.level();
        if (!(world instanceof ServerLevel serverWorld)) {
            return Items.MAP.getDefaultInstance();
        }

        int map = serverWorld.random.nextInt(6);
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
    private int randomInt(int bound) {
        return (int) (Math.random() * bound);
    }
}
