package com.akitain.explorationreloaded.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(CauldronBehavior.class)
public interface CauldronBehaviorMixin {
    @Shadow
    CauldronBehavior.CauldronBehaviorMap WATER_CAULDRON_BEHAVIOR = null;

    @Inject(method = "registerBehavior", at = @At("TAIL"))
    private static void explorationReloaded$registerHarnessCleaning(CallbackInfo ci) {
        Map<Item, CauldronBehavior> map = WATER_CAULDRON_BEHAVIOR.map();
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
    private static ActionResult cleanHarness(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        return cleanSimple(state, world, pos, player, hand, stack, ItemTags.HARNESSES, Items.WHITE_HARNESS);
    }

    @Unique
    private static ActionResult cleanSimple(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, TagKey<Item> itemTag, Item into) {
        if (!stack.isIn(itemTag)) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }

        if (!world.isClient()) {
            ItemStack itemStack = stack.copyComponentsToNewStack(into, stack.getCount());
            player.setStackInHand(hand, itemStack);
            LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
        }

        return ActionResult.SUCCESS;
    }
}
