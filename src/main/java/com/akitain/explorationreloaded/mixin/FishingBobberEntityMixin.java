package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.ExplorationReloaded;
import com.akitain.explorationreloaded.registry.ExplorationComponents;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.LightType;
import net.minecraft.world.MoonPhase;
import net.minecraft.world.World;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin {
    @Shadow
    @Final
    private int luckBonus;

    @ModifyArg(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/context/LootWorldContext$Builder;luck(F)Lnet/minecraft/loot/context/LootWorldContext$Builder;"))
    private float clearVanillaFishingLuck(float luck) {
        return 0;
    }

    @ModifyArg(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V"), index = 4)
    private ItemStack replaceFishingLoot(ItemStack loot, @Local(ordinal = 0, argsOnly = true) ItemStack rod) {
        FishingBobberEntity bobber = (FishingBobberEntity) (Object) this;
        PlayerEntity player = bobber.getPlayerOwner();
        if (player == null) {
            return loot;
        }

        ItemStack bait = getBait(player);
        int baitPower = getBaitPower(bait, player);
        int luck = getFishingLuck(bobber, player);
        int lootPool = getLootPool(player, luck, baitPower);
        ItemStack fixedLoot = getFixedFishingLoot(bobber, rod, player, lootPool);

        if (!player.getAbilities().creativeMode && !bait.isEmpty()) {
            bait.decrement(1);
        }
        return fixedLoot;
    }

    @Unique
    private int getFishingLuck(FishingBobberEntity bobber, PlayerEntity player) {
        int luck = this.luckBonus;
        World world = bobber.getEntityWorld();
        if (player.hasStatusEffect(StatusEffects.CONDUIT_POWER)) {
            luck += 2;
        }
        if (world.getLightLevel(LightType.SKY, bobber.getBlockPos()) > 10 && world.isRaining()) {
            luck += 2;
        }
        return luck;
    }

    @Unique
    private int getBaitPower(ItemStack bait, PlayerEntity player) {
        int baitPower = 0;
        if (!bait.isEmpty()) {
            baitPower = bait.get(ExplorationComponents.BAIT_POWER).level();
        }
        if (player.hasStatusEffect(StatusEffects.LUCK)) {
            baitPower += player.getStatusEffect(StatusEffects.LUCK).getAmplifier() + 1;
        }

        World world = player.getEntityWorld();
        MoonPhase moonPhase = world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.MOON_PHASE_VISUAL, player.getBlockPos());
        if (world.isNight() && moonPhase.getIndex() == 0 && world.getLightLevel(LightType.SKY, player.getBlockPos()) > 10) {
            baitPower++;
        }
        return baitPower;
    }

    @Unique
    private int getLootPool(PlayerEntity player, int luck, int baitPower) {
        int chanceGood = Math.min(luck * baitPower + 3 * baitPower, 100);
        int chanceFish = Math.max(40 - chanceGood, 0);
        int chanceBad = Math.max(40 - chanceGood * 2, 0);
        int chanceMid = Math.max(100 - chanceGood - chanceFish - chanceBad, 0);

        int roll = player.getEntityWorld().random.nextInt(100);
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
        if (player.hasStatusEffect(StatusEffects.CONDUIT_POWER) && lootPool == 1) {
            return 0;
        }
        return lootPool;
    }

    @Unique
    private ItemStack getFixedFishingLoot(FishingBobberEntity bobber, ItemStack rod, PlayerEntity player, int lootPool) {
        String[] tables = {"fish", "junk", "mid", "treasure"};
        Identifier lootTableId = ExplorationReloaded.id("gameplay/fixed_fishing/" + tables[lootPool]);
        LootTable lootTable = bobber.getEntityWorld().getServer()
                .getReloadableRegistries()
                .getLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTableId));
        LootWorldContext lootContext = new LootWorldContext.Builder((ServerWorld) bobber.getEntityWorld())
                .add(LootContextParameters.ORIGIN, bobber.getEntityPos())
                .add(LootContextParameters.TOOL, rod)
                .add(LootContextParameters.THIS_ENTITY, bobber)
                .luck(player.getLuck())
                .build(LootContextTypes.FISHING);

        ObjectArrayList<ItemStack> loots = lootTable.generateLoot(lootContext);
        if (loots.isEmpty()) {
            return Items.DIRT.getDefaultStack();
        }
        return loots.get(0);
    }

    @Unique
    private ItemStack getBait(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        if (mainHand.contains(ExplorationComponents.BAIT_POWER)) {
            return mainHand;
        }

        ItemStack offHand = player.getOffHandStack();
        if (offHand.contains(ExplorationComponents.BAIT_POWER)) {
            return offHand;
        }

        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack item = player.getInventory().getStack(slot);
            if (item.contains(ExplorationComponents.BAIT_POWER)) {
                return item;
            }
        }
        return ItemStack.EMPTY;
    }
}
