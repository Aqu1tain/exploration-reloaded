package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.ExplorationReloaded;
import com.akitain.explorationreloaded.registry.ExplorationComponents;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FishingHook.class)
public class FishingBobberEntityMixin {
    @Shadow
    @Final
    private int luck;

    @ModifyArg(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootParams$Builder;withLuck(F)Lnet/minecraft/world/level/storage/loot/LootParams$Builder;"))
    private float clearVanillaFishingLuck(float luck) {
        return 0;
    }

    @ModifyArg(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"), index = 4)
    private ItemStack replaceFishingLoot(ItemStack loot, @Local(ordinal = 0, argsOnly = true) ItemStack rod) {
        FishingHook bobber = (FishingHook) (Object) this;
        Player player = bobber.getPlayerOwner();
        if (player == null) {
            return loot;
        }

        ItemStack bait = getBait(player);
        int baitPower = getBaitPower(bait, player);
        int luck = getFishingLuck(bobber, player);
        int lootPool = getLootPool(player, luck, baitPower);
        ItemStack fixedLoot = getFixedFishingLoot(bobber, rod, player, lootPool);

        if (!player.getAbilities().instabuild && !bait.isEmpty()) {
            bait.shrink(1);
        }
        return fixedLoot;
    }

    @Unique
    private int getFishingLuck(FishingHook bobber, Player player) {
        int luck = this.luck;
        Level world = bobber.level();
        if (player.hasEffect(MobEffects.CONDUIT_POWER)) {
            luck += 2;
        }
        if (world.getBrightness(LightLayer.SKY, bobber.blockPosition()) > 10 && world.isRaining()) {
            luck += 2;
        }
        return luck;
    }

    @Unique
    private int getBaitPower(ItemStack bait, Player player) {
        int baitPower = 0;
        if (!bait.isEmpty()) {
            baitPower = bait.get(ExplorationComponents.BAIT_POWER).level();
        }
        if (player.hasEffect(MobEffects.LUCK)) {
            baitPower += player.getEffect(MobEffects.LUCK).getAmplifier() + 1;
        }

        Level world = player.level();
        MoonPhase moonPhase = world.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, player.blockPosition());
        if (world.isDarkOutside() && moonPhase.index() == 0 && world.getBrightness(LightLayer.SKY, player.blockPosition()) > 10) {
            baitPower++;
        }
        return baitPower;
    }

    @Unique
    private int getLootPool(Player player, int luck, int baitPower) {
        int chanceGood = Math.min(luck * baitPower + 3 * baitPower, 100);
        int chanceFish = Math.max(40 - chanceGood, 0);
        int chanceBad = Math.max(40 - chanceGood * 2, 0);
        int chanceMid = Math.max(100 - chanceGood - chanceFish - chanceBad, 0);

        int roll = player.level().getRandom().nextInt(100);
        int lootPool = 0;
        if (roll > chanceFish) {
            lootPool = 1;
        }
        if (roll > chanceFish + chanceBad) {
            lootPool = 2;
        }
        if (roll > chanceFish + chanceBad + chanceMid) {
            lootPool = 3;
        }
        if (player.hasEffect(MobEffects.CONDUIT_POWER) && lootPool == 1) {
            return 0;
        }
        return lootPool;
    }

    @Unique
    private ItemStack getFixedFishingLoot(FishingHook bobber, ItemStack rod, Player player, int lootPool) {
        String[] tables = {"fish", "junk", "mid", "treasure"};
        Identifier lootTableId = ExplorationReloaded.id("gameplay/fixed_fishing/" + tables[lootPool]);
        LootTable lootTable = bobber.level().getServer()
                .reloadableRegistries()
                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTableId));
        LootParams lootContext = new LootParams.Builder((ServerLevel) bobber.level())
                .withParameter(LootContextParams.ORIGIN, bobber.position())
                .withParameter(LootContextParams.TOOL, rod)
                .withParameter(LootContextParams.THIS_ENTITY, bobber)
                .withLuck(player.getLuck())
                .create(LootContextParamSets.FISHING);

        ObjectArrayList<ItemStack> loots = lootTable.getRandomItems(lootContext);
        if (loots.isEmpty()) {
            return Items.DIRT.getDefaultInstance();
        }
        return loots.get(0);
    }

    @Unique
    private ItemStack getBait(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.has(ExplorationComponents.BAIT_POWER)) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.has(ExplorationComponents.BAIT_POWER)) {
            return offHand;
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item.has(ExplorationComponents.BAIT_POWER)) {
                return item;
            }
        }
        return ItemStack.EMPTY;
    }
}
