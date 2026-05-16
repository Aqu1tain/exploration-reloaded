package com.akitain.explorationreloaded.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(CauldronInteraction.class)
public interface CauldronBehaviorMixin {
    @Shadow
    CauldronInteraction.InteractionMap WATER = null;

    @Inject(method = "bootStrap", at = @At("TAIL"))
    private static void explorationReloaded$registerHarnessCleaning(CallbackInfo ci) {
        Map<Item, CauldronInteraction> map = WATER.map();
        Item[] coloredHarnesses = {
            Items.LIGHT_GRAY_HARNESS,
            Items.GRAY_HARNESS,
            Items.BLACK_HARNESS,
            Items.BROWN_HARNESS,
            Items.RED_HARNESS,
            Items.ORANGE_HARNESS,
            Items.YELLOW_HARNESS,
            Items.LIME_HARNESS,
            Items.GREEN_HARNESS,
            Items.CYAN_HARNESS,
            Items.LIGHT_BLUE_HARNESS,
            Items.BLUE_HARNESS,
            Items.PURPLE_HARNESS,
            Items.MAGENTA_HARNESS,
            Items.PINK_HARNESS
        };

        for (Item harness : coloredHarnesses) {
            map.put(harness, CauldronBehaviorMixin::cleanHarness);
        }
    }

    @Unique
    private static InteractionResult cleanHarness(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        return cleanSimple(state, world, pos, player, hand, stack, ItemTags.HARNESSES, Items.WHITE_HARNESS);
    }

    @Unique
    private static InteractionResult cleanSimple(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, ItemStack stack, TagKey<Item> itemTag, Item into) {
        if (!stack.is(itemTag)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!world.isClientSide()) {
            ItemStack itemStack = stack.transmuteCopy(into, stack.getCount());
            player.setItemInHand(hand, itemStack);
            LayeredCauldronBlock.lowerFillLevel(state, world, pos);
        }

        return InteractionResult.SUCCESS;
    }
}
