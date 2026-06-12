package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.registry.ExplorationItems;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Map;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

@Mixin(AbstractHorse.class)
public class AbstractHorseEntityMixin {
    @Unique
    private static final Map<Item, Float> RAGE_CHANCE = new Object2FloatArrayMap<>();
    @Unique
    private static final Map<Holder<MobEffect>, String> EFFECT_MODIFIERS = new Object2ObjectArrayMap<>();

    @Shadow
    protected SimpleContainer inventory;

    static {
        RAGE_CHANCE.put(Items.NETHERITE_HORSE_ARMOR, 1F);
        RAGE_CHANCE.put(Items.DIAMOND_HORSE_ARMOR, 0.9F);
        RAGE_CHANCE.put(Items.IRON_HORSE_ARMOR, 0.75F);
        RAGE_CHANCE.put(Items.GOLDEN_HORSE_ARMOR, 0.6F);
        RAGE_CHANCE.put(ExplorationItems.CHAINMAIL_HORSE_ARMOR, 0.5F);
        RAGE_CHANCE.put(Items.COPPER_HORSE_ARMOR, 0.45F);
        RAGE_CHANCE.put(Items.LEATHER_HORSE_ARMOR, 0.3F);

        EFFECT_MODIFIERS.put(MobEffects.SPEED, "movement_speed");
        EFFECT_MODIFIERS.put(MobEffects.JUMP_BOOST, "jump_strength");
        EFFECT_MODIFIERS.put(MobEffects.REGENERATION, "max_health");
    }

    @Inject(method = "standIfPossible", at = @At("HEAD"), cancellable = true)
    private void rejectAngryWhenArmored(CallbackInfo ci) {
        ItemStack armor = this.inventory.getItem(1);
        float chance = RAGE_CHANCE.getOrDefault(armor.getItem(), 0F);
        if (chance > 0 && chance < 1 || Math.random() <= chance) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "setOffspringAttribute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/equine/AbstractHorse;createOffspringAttribute(DDDDLnet/minecraft/util/RandomSource;)D"), index = 0)
    private double modifyFirstParentAttribute(double original, @Local(argsOnly = true) Holder<Attribute> attribute) {
        AgeableMob parent = (AgeableMob) (Object) this;
        return modifyAttribute(original, attribute.value(), parent.getActiveEffects());
    }

    @ModifyArg(method = "setOffspringAttribute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/equine/AbstractHorse;createOffspringAttribute(DDDDLnet/minecraft/util/RandomSource;)D"), index = 1)
    private double modifySecondParentAttribute(double original, @Local(argsOnly = true) Holder<Attribute> attribute, @Local(argsOnly = true) AgeableMob otherParent) {
        return modifyAttribute(original, attribute.value(), otherParent.getActiveEffects());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/equine/AbstractHorse;isStanding()Z"))
    private void limitWalkingSpeedAndHorseLocator(CallbackInfo ci) {
        AbstractHorse horse = (AbstractHorse) (Object) this;
        if (horse.hasControllingPassenger() && horse.onGround() && !horse.getControllingPassenger().isSprinting()) {
            Vec3 velocity = horse.getDeltaMovement();
            double horizontalSpeed = velocity.horizontalDistance();
            if (horizontalSpeed > 0.01) {
                double limitedSpeed = Math.min(0.1, horizontalSpeed);
                horse.setDeltaMovement(limitedSpeed * velocity.x / horizontalSpeed, velocity.y, limitedSpeed * velocity.z / horizontalSpeed);
            }
        }

        horse.refreshDimensions();
        if (!horse.entityTags().contains("locate")) {
            return;
        }

        if (horse.tickCount > 20 * 60 * 5) {
            horse.getAttributes().getInstance(Attributes.WAYPOINT_TRANSMIT_RANGE).setBaseValue(0);
            horse.removeTag("locate");
            return;
        }

        boolean hidden = horse.isLeashed() || horse.hasControllingPassenger();
        horse.getAttributes().getInstance(Attributes.WAYPOINT_TRANSMIT_RANGE).setBaseValue(hidden ? 0 : 100);
    }

    @Inject(method = "getDismountLocationForPassenger", at = @At("HEAD"))
    private void addLocatorBarIcon(LivingEntity passenger, CallbackInfoReturnable<Vec3> cir) {
        if (!(passenger instanceof ServerPlayer)) {
            return;
        }

        AbstractHorse horse = (AbstractHorse) (Object) this;
        if (!horse.isTamed()) {
            return;
        }

        horse.tickCount = 0;
        horse.addTag("locate");
        horse.getAttributes().getInstance(Attributes.WAYPOINT_TRANSMIT_RANGE).setBaseValue(100);
        String command = "/waypoint modify " + horse.getStringUUID() + " style set horse";
        horse.level().getServer().getCommands().performPrefixedCommand(createCommandSource((ServerLevel) horse.level(), horse.blockPosition()), command);
    }

    @Inject(method = "doPlayerRide", at = @At("HEAD"))
    private void removeLocatorBarIcon(Player player, CallbackInfo ci) {
        AbstractHorse horse = (AbstractHorse) (Object) this;
        horse.getAttributes().getInstance(Attributes.WAYPOINT_TRANSMIT_RANGE).setBaseValue(0);
        horse.removeTag("locate");
    }

    @Unique
    private double modifyAttribute(double original, Attribute attribute, Collection<MobEffectInstance> effects) {
        MobEffectInstance chosenEffect = getStatusEffectInstance(effects);
        if (chosenEffect == null) {
            return original;
        }

        String attributeModifier = EFFECT_MODIFIERS.get(chosenEffect.getEffect());
        if (!attribute.getDescriptionId().contains(attributeModifier)) {
            return original;
        }

        double bonus = 0;
        if (attribute.getDescriptionId().contains("max_health")) {
            bonus = 2;
        }
        if (attribute.getDescriptionId().contains("jump_strength")) {
            bonus = 0.08;
        }
        if (attribute.getDescriptionId().contains("movement_speed")) {
            bonus = 0.03;
        }
        bonus *= chosenEffect.getAmplifier() + 1 + (chosenEffect.isAmbient() ? 0 : 1);
        return original + bonus;
    }

    @Unique
    @Nullable
    private static MobEffectInstance getStatusEffectInstance(Collection<MobEffectInstance> effects) {
        MobEffectInstance chosenEffect = null;
        int longestDuration = -1;
        int highestLevel = -1;

        for (MobEffectInstance effect : effects) {
            if (!EFFECT_MODIFIERS.containsKey(effect.getEffect())) {
                continue;
            }

            int level = effect.getAmplifier() + (effect.isAmbient() ? 0 : 1);
            int duration = effect.isInfiniteDuration() ? 999999999 : effect.getDuration();
            if (level > highestLevel || duration > longestDuration) {
                highestLevel = level;
                longestDuration = duration;
                chosenEffect = effect;
            }
        }
        return chosenEffect;
    }

    @Unique
    private static CommandSourceStack createCommandSource(ServerLevel world, BlockPos pos) {
        return new CommandSourceStack(
                CommandSource.NULL,
                Vec3.atCenterOf(pos),
                Vec2.ZERO,
                world,
                LevelBasedPermissionSet.GAMEMASTER,
                "Sign",
                Component.literal("Sign"),
                world.getServer(),
                null
        );
    }
}
