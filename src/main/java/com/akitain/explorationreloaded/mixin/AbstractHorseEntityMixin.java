package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.registry.ExplorationItems;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
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

@Mixin(AbstractHorseEntity.class)
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
    private static final Map<RegistryEntry<StatusEffect>, String> EFFECT_ATTRIBUTES = new Object2ObjectArrayMap<>();
    @Unique
    private static final Map<String, Double> ATTRIBUTE_BONUSES = Map.of(
            "max_health", 2.0,
            "jump_strength", 0.08,
            "movement_speed", 0.03
    );

    @Shadow
    protected SimpleInventory items;

    static {
        RAGE_CHANCE.put(Items.NETHERITE_HORSE_ARMOR, 1F);
        RAGE_CHANCE.put(Items.DIAMOND_HORSE_ARMOR, 0.9F);
        RAGE_CHANCE.put(Items.IRON_HORSE_ARMOR, 0.75F);
        RAGE_CHANCE.put(Items.GOLDEN_HORSE_ARMOR, 0.6F);
        RAGE_CHANCE.put(ExplorationItems.CHAINMAIL_HORSE_ARMOR, 0.5F);
        RAGE_CHANCE.put(Items.COPPER_HORSE_ARMOR, 0.45F);
        RAGE_CHANCE.put(Items.LEATHER_HORSE_ARMOR, 0.3F);

        EFFECT_ATTRIBUTES.put(StatusEffects.SPEED, "movement_speed");
        EFFECT_ATTRIBUTES.put(StatusEffects.JUMP_BOOST, "jump_strength");
        EFFECT_ATTRIBUTES.put(StatusEffects.REGENERATION, "max_health");
    }

    @Inject(method = "updateAnger", at = @At("HEAD"), cancellable = true)
    private void rejectAngryWhenArmored(CallbackInfo ci) {
        ItemStack armor = this.items.getStack(ARMOR_SLOT);
        float chance = RAGE_CHANCE.getOrDefault(armor.getItem(), 0F);
        boolean staysCalm = (chance > 0 && chance < 1) || Math.random() <= chance;
        if (staysCalm) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "setChildAttribute", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AbstractHorseEntity;calculateAttributeBaseValue(DDDDLnet/minecraft/util/math/random/Random;)D"), index = 0)
    private double modifyFirstParentAttribute(double original, @Local(argsOnly = true) RegistryEntry<EntityAttribute> attribute) {
        PassiveEntity parent = (PassiveEntity) (Object) this;
        return applyParentEffectBonus(original, attribute.value(), parent.getStatusEffects());
    }

    @ModifyArg(method = "setChildAttribute", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AbstractHorseEntity;calculateAttributeBaseValue(DDDDLnet/minecraft/util/math/random/Random;)D"), index = 1)
    private double modifySecondParentAttribute(double original, @Local(argsOnly = true) RegistryEntry<EntityAttribute> attribute, @Local(argsOnly = true) PassiveEntity otherParent) {
        return applyParentEffectBonus(original, attribute.value(), otherParent.getStatusEffects());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AbstractHorseEntity;isAngry()Z"))
    private void onTick(CallbackInfo ci) {
        AbstractHorseEntity horse = (AbstractHorseEntity) (Object) this;
        limitWalkingSpeed(horse);
        horse.calculateDimensions();
        tickLocatorSession(horse);
    }

    @Inject(method = "updatePassengerForDismount", at = @At("HEAD"))
    private void startLocatorOnDismount(LivingEntity passenger, CallbackInfoReturnable<Vec3d> cir) {
        if (!(passenger instanceof ServerPlayerEntity)) {
            return;
        }

        AbstractHorseEntity horse = (AbstractHorseEntity) (Object) this;
        if (!horse.isTame()) {
            return;
        }

        horse.age = 0;
        horse.addCommandTag(LOCATE_TAG);
        setWaypointTransmitRange(horse, LOCATOR_TRANSMIT_RANGE);
        applyHorseWaypointStyle(horse);
    }

    @Inject(method = "putPlayerOnBack", at = @At("HEAD"))
    private void endLocatorOnRide(PlayerEntity player, CallbackInfo ci) {
        endLocatorSession((AbstractHorseEntity) (Object) this);
    }

    @Unique
    private static void limitWalkingSpeed(AbstractHorseEntity horse) {
        if (!horse.hasControllingPassenger() || !horse.isOnGround() || horse.getControllingPassenger().isSprinting()) {
            return;
        }

        Vec3d velocity = horse.getVelocity();
        double horizontalSpeed = velocity.horizontalLength();
        if (horizontalSpeed <= 0.01) {
            return;
        }

        double cappedSpeed = Math.min(RIDDEN_WALK_SPEED_CAP, horizontalSpeed);
        horse.setVelocity(cappedSpeed * velocity.x / horizontalSpeed, velocity.y, cappedSpeed * velocity.z / horizontalSpeed);
    }

    @Unique
    private static void tickLocatorSession(AbstractHorseEntity horse) {
        if (!horse.getCommandTags().contains(LOCATE_TAG)) {
            return;
        }

        if (horse.age > LOCATOR_TIMEOUT_TICKS) {
            endLocatorSession(horse);
            return;
        }

        boolean hidden = horse.isLeashed() || horse.hasControllingPassenger();
        setWaypointTransmitRange(horse, hidden ? 0 : LOCATOR_TRANSMIT_RANGE);
    }

    @Unique
    private static void endLocatorSession(AbstractHorseEntity horse) {
        setWaypointTransmitRange(horse, 0);
        horse.removeCommandTag(LOCATE_TAG);
    }

    @Unique
    private static void setWaypointTransmitRange(AbstractHorseEntity horse, double range) {
        horse.getAttributes().getCustomInstance(EntityAttributes.WAYPOINT_TRANSMIT_RANGE).setBaseValue(range);
    }

    @Unique
    private static void applyHorseWaypointStyle(AbstractHorseEntity horse) {
        String command = "/waypoint modify " + horse.getUuidAsString() + " style set horse";
        horse.getEntityWorld().getServer().getCommandManager().parseAndExecute(createCommandSource((ServerWorld) horse.getEntityWorld(), horse.getBlockPos()), command);
    }

    @Unique
    private static double applyParentEffectBonus(double original, EntityAttribute attribute, Collection<StatusEffectInstance> effects) {
        StatusEffectInstance effect = strongestRelevantEffect(effects);
        if (effect == null) {
            return original;
        }

        String attributeName = EFFECT_ATTRIBUTES.get(effect.getEffectType());
        if (!attribute.getTranslationKey().contains(attributeName)) {
            return original;
        }

        int potency = effect.getAmplifier() + 1 + (effect.isAmbient() ? 0 : 1);
        return original + ATTRIBUTE_BONUSES.get(attributeName) * potency;
    }

    @Unique
    @Nullable
    private static StatusEffectInstance strongestRelevantEffect(Collection<StatusEffectInstance> effects) {
        StatusEffectInstance chosenEffect = null;
        int longestDuration = -1;
        int highestLevel = -1;

        for (StatusEffectInstance effect : effects) {
            if (!EFFECT_ATTRIBUTES.containsKey(effect.getEffectType())) {
                continue;
            }

            int level = effect.getAmplifier() + (effect.isAmbient() ? 0 : 1);
            int duration = effect.isInfinite() ? 999999999 : effect.getDuration();
            if (level > highestLevel || duration > longestDuration) {
                highestLevel = level;
                longestDuration = duration;
                chosenEffect = effect;
            }
        }
        return chosenEffect;
    }

    @Unique
    private static ServerCommandSource createCommandSource(ServerWorld world, BlockPos pos) {
        return new ServerCommandSource(
                CommandOutput.DUMMY,
                Vec3d.ofCenter(pos),
                Vec2f.ZERO,
                world,
                LeveledPermissionPredicate.GAMEMASTERS,
                "Sign",
                Text.literal("Sign"),
                world.getServer(),
                null
        );
    }
}
