package com.akitain.explorationreloaded.mixin.map_book;

import com.llamalad7.mixinextras.sugar.Local;
import com.akitain.explorationreloaded.registry.item.MapBookIdCountsState;
import com.akitain.explorationreloaded.registry.item.MapBookState;
import com.akitain.explorationreloaded.registry.item.MapBookStateManager;
import com.akitain.explorationreloaded.registry.ExplorationItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BiConsumer;

@Mixin(CartographyTableMenu.class)
public class CartographyTableScreenHandlerMixin {

    @Shadow
    @Final
    private ResultContainer resultContainer;

    @ModifyArg(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/CartographyTableMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;", ordinal = 1), index = 0)
    private Slot bookInSecondSlot(Slot par1){
        CartographyTableMenu CTSH = (CartographyTableMenu)(Object)this;
        return new Slot(CTSH.container, 1, 15, 52) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.PAPER) || stack.is(Items.MAP) || stack.is(Items.GLASS_PANE) || stack.is(Items.BOOK);
            }
        };
    }

    @ModifyArg(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/CartographyTableMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;", ordinal = 2), index = 0)
    private Slot MapBookCraft(Slot par1, @Local(argsOnly = true) ContainerLevelAccess context){
        CartographyTableMenu CTSH = (CartographyTableMenu)(Object)this;
        return new Slot(this.resultContainer, 2, 145, 39) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                if (player instanceof ServerPlayer) {
                    if (CTSH.slots.get(0).getItem().is(Items.FILLED_MAP)) {
                        if (CTSH.slots.get(1).getItem().is(Items.BOOK)) {
                            if (stack.is(ExplorationItems.MAP_BOOK)) {
                                int i = createMapBookState(stack, player.level().getServer());
                                MapBookState state = MapBookStateManager.INSTANCE.getMapBookState(player.level().getServer(), i);
                                if (state != null) {
                                    state.addMapID(CTSH.slots.get(0).getItem().get(DataComponents.MAP_ID).id());
                                }
                            }
                        }
                    }
                }

                CTSH.slots.get(0).remove(1);
                CTSH.slots.get(1).remove(1);
                stack.getItem().onCraftedBy(stack, player);
                context.execute( (world, pos) -> {
                    long l = world.getGameTime();
                    if (CTSH.lastSoundTime != l) {
                        world.playSound(null, pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        CTSH.lastSoundTime = l;
                    }
                });
                super.onTake(player, stack);
            }
        };
    }

    @Redirect(method = "setupResultSlot", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V"
    ))
    private void cartogrophyTableMapBook(ContainerLevelAccess instance, BiConsumer<Level, BlockPos> function, @Local(argsOnly = true, ordinal = 0) ItemStack map,  @Local(argsOnly = true, ordinal = 1) ItemStack item,  @Local(argsOnly = true, ordinal = 2) ItemStack oldResult) {
        CartographyTableMenu CTSH = (CartographyTableMenu)(Object)this;
        instance.execute((world, pos) -> {

            if (map.is(ExplorationItems.MAP_BOOK)) {
                if (item.is(Items.BOOK)) {
                    this.resultContainer.setItem(2, map.copyWithCount(2));
                    CTSH.broadcastChanges();
                }
            } else {
                MapItemSavedData mapState = MapItem.getSavedData(map, world);
                if (mapState != null) {
                    ItemStack itemStack4;
                    if (item.is(Items.PAPER) && !mapState.locked && mapState.scale < 4) {
                        itemStack4 = map.copyWithCount(1);
                        itemStack4.set(DataComponents.MAP_POST_PROCESSING, MapPostProcessing.SCALE);
                        CTSH.broadcastChanges();
                    }
                    else if (item.is(Items.GLASS_PANE) && !mapState.locked) {
                        itemStack4 = map.copyWithCount(1);
                        itemStack4.set(DataComponents.MAP_POST_PROCESSING, MapPostProcessing.LOCK);
                        CTSH.broadcastChanges();
                    }
                    else if (item.is(Items.BOOK) && !mapState.locked) {
                        itemStack4 = ExplorationItems.MAP_BOOK.getDefaultInstance();
                        CTSH.broadcastChanges();
                    }
                    else {
                        if (!item.is(Items.MAP)) {
                            this.resultContainer.removeItemNoUpdate(2);
                            CTSH.broadcastChanges();
                            return;
                        }

                        itemStack4 = map.copyWithCount(2);
                        CTSH.broadcastChanges();
                    }

                    if (!ItemStack.matches(itemStack4, oldResult)) {
                        this.resultContainer.setItem(2, itemStack4);
                        CTSH.broadcastChanges();
                    }
                }
            }
        });
    }

    @Redirect(method = "quickMoveStack",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean bookQuickMove(ItemStack instance, Object item){
        return instance.is(Items.PAPER) || instance.is(Items.BOOK);
    }

    @Redirect(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack quickMapBookCraft(Slot instance, @Local(argsOnly = true) Player player) {
        ItemStack stack = instance.getItem();
        CartographyTableMenu CTSH = (CartographyTableMenu)(Object)this;
        if (player instanceof ServerPlayer) {
            if (CTSH.slots.get(0).getItem().is(Items.FILLED_MAP)) {
                if (CTSH.slots.get(1).getItem().is(Items.BOOK)) {
                    if (stack.is(ExplorationItems.MAP_BOOK)) {
                        if (Math.min(CTSH.slots.get(0).getItem().getCount(), CTSH.slots.get(1).getItem().getCount())==1) {
                            int i = createMapBookState(stack, player.level().getServer());
                            MapBookState state = MapBookStateManager.INSTANCE.getMapBookState(player.level().getServer(), i);
                            if (state != null) {
                                state.addMapID(CTSH.slots.get(0).getItem().get(DataComponents.MAP_ID).id());
                            }
                        }else{
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
        }
        return stack;
    }

    @Unique
    private int allocateMapBookId(MinecraftServer server) {

        MapBookIdCountsState counts = server.overworld().getDataStorage().computeIfAbsent(
                MapBookIdCountsState.persistentStateType
        );
        int i = counts.get();
        MapBookStateManager.INSTANCE.putMapBookState(server, i, new MapBookState());
        return i;
    }

    @Unique
    private int createMapBookState(ItemStack stack, MinecraftServer server) {
        int i = this.allocateMapBookId(server);
        stack.set(DataComponents.MAP_ID, new MapId(i));
        return i;
    }
}
