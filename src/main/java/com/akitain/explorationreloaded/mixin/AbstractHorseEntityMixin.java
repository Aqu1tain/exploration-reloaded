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
    private static final Map<Item, Float> RAGE_CHANCE = new Object2FloatArrayMap<>();
    @Unique
    private static final Map<RegistryEntry<StatusEffect>, String> EFFECT_MODIFIERS = new Object2ObjectArrayMap<>();

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

        EFFECT_MODIFIERS.put(StatusEffects.SPEED, "movement_speed");
        EFFECT_MODIFIERS.put(StatusEffects.JUMP_BOOST, "jump_strength");
        EFFECT_MODIFIERS.put(StatusEffects.REGENERATION, "max_health");
    }

    @Inject(method = "updateAnger", at = @At("HEAD"), cancellable = true)
    private void rejectAngryWhenArmored(CallbackInfo ci) {
        ItemStack armor = this.items.getStack(1);
        float chance = RAGE_CHANCE.getOrDefault(armor.getItem(), 0F);
        if (chance > 0 && chance < 1 || Math.random() <= chance) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "setChildAttribute", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AbstractHorseEntity;calculateAttributeBaseValue(DDDDLnet/minecraft/util/math/random/Random;)D"), index = 0)
    private double modifyFirstParentAttribute(double original, @Local(argsOnly = true) RegistryEntry<EntityAttribute> attribute) {
        PassiveEntity parent = (PassiveEntity) (Object) this;
        return modifyAttribute(original, attribute.value(), parent.getStatusEffects());
    }

    @ModifyArg(method = "setChildAttribute", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AbstractHorseEntity;calculateAttributeBaseValue(DDDDLnet/minecraft/util/math/random/Random;)D"), index = 1)
    private double modifySecondParentAttribute(double original, @Local(argsOnly = true) RegistryEntry<EntityAttribute> attribute, @Local(argsOnly = true) PassiveEntity otherParent) {
        return modifyAttribute(original, attribute.value(), otherParent.getStatusEffects());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AbstractHorseEntity;isAngry()Z"))
    private void limitWalkingSpeedAndHorseLocator(CallbackInfo ci) {
        AbstractHorseEntity horse = (AbstractHorseEntity) (Object) this;
        if (horse.hasControllingPassenger() && horse.isOnGround() && !horse.getControllingPassenger().isSprinting()) {
            Vec3d velocity = horse.getVelocity();
            double horizontalSpeed = velocity.horizontalLength();
            if (horizontalSpeed > 0.01) {
                double limitedSpeed = Math.min(0.1, horizontalSpeed);
                horse.setVelocity(limitedSpeed * velocity.x / horizontalSpeed, velocity.y, limitedSpeed * velocity.z / horizontalSpeed);
            }
        }

        horse.calculateDimensions();
        if (horse.getCommandTags().contains("locate") && horse.age > 20 * 60 * 5) {
            horse.getAttributes().getCustomInstance(EntityAttributes.WAYPOINT_TRANSMIT_RANGE).setBaseValue(0);
            horse.removeCommandTag("locate");
        }
    }

    @Inject(method = "updatePassengerForDismount", at = @At("HEAD"))
    private void addLocatorBarIcon(LivingEntity passenger, CallbackInfoReturnable<Vec3d> cir) {
        if (!(passenger instanceof ServerPlayerEntity)) {
            return;
        }

        AbstractHorseEntity horse = (AbstractHorseEntity) (Object) this;
        if (!horse.isTame()) {
            return;
        }

        horse.age = 0;
        horse.addCommandTag("locate");
        horse.getAttributes().getCustomInstance(EntityAttributes.WAYPOINT_TRANSMIT_RANGE).setBaseValue(100);
        String command = "/waypoint modify " + horse.getUuidAsString() + " style set horse";
        horse.getEntityWorld().getServer().getCommandManager().parseAndExecute(createCommandSource((ServerWorld) horse.getEntityWorld(), horse.getBlockPos()), command);
    }

    @Inject(method = "putPlayerOnBack", at = @At("HEAD"))
    private void removeLocatorBarIcon(PlayerEntity player, CallbackInfo ci) {
        AbstractHorseEntity horse = (AbstractHorseEntity) (Object) this;
        horse.getAttributes().getCustomInstance(EntityAttributes.WAYPOINT_TRANSMIT_RANGE).setBaseValue(0);
        horse.removeCommandTag("locate");
    }

    @Unique
    private double modifyAttribute(double original, EntityAttribute attribute, Collection<StatusEffectInstance> effects) {
        StatusEffectInstance chosenEffect = getStatusEffectInstance(effects);
        if (chosenEffect == null) {
            return original;
        }

        String attributeModifier = EFFECT_MODIFIERS.get(chosenEffect.getEffectType());
        if (!attribute.getTranslationKey().contains(attributeModifier)) {
            return original;
        }

        double bonus = 0;
        if (attribute.getTranslationKey().contains("max_health")) {
            bonus = 2;
        }
        if (attribute.getTranslationKey().contains("jump_strength")) {
            bonus = 0.08;
        }
        if (attribute.getTranslationKey().contains("movement_speed")) {
            bonus = 0.03;
        }
        bonus *= chosenEffect.getAmplifier() + 1 + (chosenEffect.isAmbient() ? 0 : 1);
        return original + bonus;
    }

    @Unique
    @Nullable
    private static StatusEffectInstance getStatusEffectInstance(Collection<StatusEffectInstance> effects) {
        StatusEffectInstance chosenEffect = null;
        int longestDuration = -1;
        int highestLevel = -1;

        for (StatusEffectInstance effect : effects) {
            if (!EFFECT_MODIFIERS.containsKey(effect.getEffectType())) {
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
