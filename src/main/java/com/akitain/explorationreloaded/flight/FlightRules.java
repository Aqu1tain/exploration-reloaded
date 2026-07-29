package com.akitain.explorationreloaded.flight;

import com.akitain.explorationreloaded.ExplorationReloaded;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public final class FlightRules {

    /** Fireworks no longer push a glider; campfire updrafts replace them as the way to gain altitude. */
    public static GameRule<Boolean> FIREWORK_BOOSTS_FLIGHT;
    public static GameRule<Boolean> CAMPFIRE_UPDRAFTS;
    /** Ticks spent standing on a campfire before one Smokestack charge is granted. */
    public static GameRule<Integer> SMOKESTACK_CHARGE_TICKS;

    private FlightRules() {
    }

    public static void register() {
        FIREWORK_BOOSTS_FLIGHT = GameRuleBuilder.forBoolean(false)
                .category(GameRuleCategory.PLAYER)
                .buildAndRegister(ExplorationReloaded.id("firework_boosts_flight"));

        CAMPFIRE_UPDRAFTS = GameRuleBuilder.forBoolean(true)
                .category(GameRuleCategory.PLAYER)
                .buildAndRegister(ExplorationReloaded.id("campfire_updrafts"));

        SMOKESTACK_CHARGE_TICKS = GameRuleBuilder.forInteger(30)
                .category(GameRuleCategory.PLAYER)
                .buildAndRegister(ExplorationReloaded.id("smokestack_charge_ticks"));
    }
}
