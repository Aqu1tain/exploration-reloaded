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
    private static final int ARMOR_SLOT = 1;
    @Unique
    private static final String LOCATE_TAG = "locate";
    @Unique
    private static final double LOCATOR_TRANSMIT_RANGE = 100;
    @Unique
    private static final int LOCATOR_TIMEOUT_TICKS = 20 * 60 * 5;
    @Unique
    private static final double RIDDEN_WALK_SPEED_CAP = 0.1;

    @Unique
    private static final Map<Item, Float> RAGE_CHANCE = new Object2FloatArrayMap<>();
    @Unique
    private static final Map<Holder<MobEffect>, String> EFFECT_ATTRIBUTES = new Object2ObjectArrayMap<>();
    @Unique
    private static final Map<String, Double> ATTRIBUTE_BONUSES = Map.of(
            "max_health", 2.0,
            "jump_strength", 0.08,
            "movement_speed", 0.03
    );

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

        EFFECT_ATTRIBUTES.put(MobEffects.SPEED, "movement_speed");
        EFFECT_ATTRIBUTES.put(MobEffects.JUMP_BOOST, "jump_strength");
        EFFECT_ATTRIBUTES.put(MobEffects.REGENERATION, "max_health");
    }

    @Inject(method = "standIfPossible", at = @At("HEAD"), cancellable = true)
    private void rejectAngryWhenArmored(CallbackInfo ci) {
        ItemStack armor = this.inventory.getItem(ARMOR_SLOT);
        float chance = RAGE_CHANCE.getOrDefault(armor.getItem(), 0F);
        boolean staysCalm = (chance > 0 && chance < 1) || Math.random() <= chance;
        if (staysCalm) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "setOffspringAttribute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/equine/AbstractHorse;createOffspringAttribute(DDDDLnet/minecraft/util/RandomSource;)D"), index = 0)
    private double modifyFirstParentAttribute(double original, @Local(argsOnly = true) Holder<Attribute> attribute) {
        AgeableMob parent = (AgeableMob) (Object) this;
        return applyParentEffectBonus(original, attribute.value(), parent.getActiveEffects());
    }

    @ModifyArg(method = "setOffspringAttribute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/equine/AbstractHorse;createOffspringAttribute(DDDDLnet/minecraft/util/RandomSource;)D"), index = 1)
    private double modifySecondParentAttribute(double original, @Local(argsOnly = true) Holder<Attribute> attribute, @Local(argsOnly = true) AgeableMob otherParent) {
        return applyParentEffectBonus(original, attribute.value(), otherParent.getActiveEffects());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/equine/AbstractHorse;isStanding()Z"))
    private void onTick(CallbackInfo ci) {
        AbstractHorse horse = (AbstractHorse) (Object) this;
        limitWalkingSpeed(horse);
        horse.refreshDimensions();
        tickLocatorSession(horse);
    }

    @Inject(method = "getDismountLocationForPassenger", at = @At("HEAD"))
    private void startLocatorOnDismount(LivingEntity passenger, CallbackInfoReturnable<Vec3> cir) {
        if (!(passenger instanceof ServerPlayer)) {
            return;
        }

        AbstractHorse horse = (AbstractHorse) (Object) this;
        if (!horse.isTamed()) {
            return;
        }

        horse.tickCount = 0;
        horse.addTag(LOCATE_TAG);
        setWaypointTransmitRange(horse, LOCATOR_TRANSMIT_RANGE);
        applyHorseWaypointStyle(horse);
    }

    @Inject(method = "doPlayerRide", at = @At("HEAD"))
    private void endLocatorOnRide(Player player, CallbackInfo ci) {
        endLocatorSession((AbstractHorse) (Object) this);
    }

    @Unique
    private static void limitWalkingSpeed(AbstractHorse horse) {
        if (!horse.hasControllingPassenger() || !horse.onGround() || horse.getControllingPassenger().isSprinting()) {
            return;
        }

        Vec3 velocity = horse.getDeltaMovement();
        double horizontalSpeed = velocity.horizontalDistance();
        if (horizontalSpeed <= 0.01) {
            return;
        }

        double cappedSpeed = Math.min(RIDDEN_WALK_SPEED_CAP, horizontalSpeed);
        horse.setDeltaMovement(cappedSpeed * velocity.x / horizontalSpeed, velocity.y, cappedSpeed * velocity.z / horizontalSpeed);
    }

    @Unique
    private static void tickLocatorSession(AbstractHorse horse) {
        if (!horse.entityTags().contains(LOCATE_TAG)) {
            return;
        }

        if (horse.tickCount > LOCATOR_TIMEOUT_TICKS) {
            endLocatorSession(horse);
            return;
        }

        boolean hidden = horse.isLeashed() || horse.hasControllingPassenger();
        setWaypointTransmitRange(horse, hidden ? 0 : LOCATOR_TRANSMIT_RANGE);
    }

    @Unique
    private static void endLocatorSession(AbstractHorse horse) {
        setWaypointTransmitRange(horse, 0);
        horse.removeTag(LOCATE_TAG);
    }

    @Unique
    private static void setWaypointTransmitRange(AbstractHorse horse, double range) {
        horse.getAttributes().getInstance(Attributes.WAYPOINT_TRANSMIT_RANGE).setBaseValue(range);
    }

    @Unique
    private static void applyHorseWaypointStyle(AbstractHorse horse) {
        String command = "/waypoint modify " + horse.getStringUUID() + " style set horse";
        horse.level().getServer().getCommands().performPrefixedCommand(createCommandSource((ServerLevel) horse.level(), horse.blockPosition()), command);
    }

    @Unique
    private static double applyParentEffectBonus(double original, Attribute attribute, Collection<MobEffectInstance> effects) {
        MobEffectInstance effect = strongestRelevantEffect(effects);
        if (effect == null) {
            return original;
        }

        String attributeName = EFFECT_ATTRIBUTES.get(effect.getEffect());
        if (!attribute.getDescriptionId().contains(attributeName)) {
            return original;
        }

        int potency = effect.getAmplifier() + 1 + (effect.isAmbient() ? 0 : 1);
        return original + ATTRIBUTE_BONUSES.get(attributeName) * potency;
    }

    @Unique
    @Nullable
    private static MobEffectInstance strongestRelevantEffect(Collection<MobEffectInstance> effects) {
        MobEffectInstance chosenEffect = null;
        int longestDuration = -1;
        int highestLevel = -1;

        for (MobEffectInstance effect : effects) {
            if (!EFFECT_ATTRIBUTES.containsKey(effect.getEffect())) {
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
