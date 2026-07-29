package com.akitain.explorationreloaded.mixin.client.flight;

import com.akitain.explorationreloaded.flight.ElytraFlight;
import com.akitain.explorationreloaded.flight.FlightEnchantments;
import com.akitain.explorationreloaded.flight.FlightState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Flying without knowing how much thrust is left is guesswork, so the charges sit under the crosshair:
 * one pip per charge, dimmed once spent. Shown while gliding, and also while crouching on a campfire so
 * you can watch the tank fill instead of counting seconds.
 */
@Mixin(Gui.class)
public class SmokestackHudMixin {

    private static final Identifier CHARGE_SPRITE =
            Identifier.fromNamespaceAndPath("exploration-reloaded", "hud/smokestack_charge");
    private static final int PIP_SIZE = 8;
    private static final int PIP_SPACING = 10;
    private static final int FILLED_TINT = 0xFFFFFFFF;
    private static final int SPENT_TINT = 0x50000000;

    @Inject(method = "extractCrosshair", at = @At("TAIL"))
    private void erExtractSmokestackCharges(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        boolean charging = ElytraFlight.isRidingCampfireSmoke(player);
        if (!player.isFallFlying() && !charging) {
            return;
        }

        int max = FlightEnchantments.smokestackLevel(player);
        if (max <= 0) {
            return;
        }

        int charges = FlightState.charges(player);
        int totalWidth = max * PIP_SPACING - (PIP_SPACING - PIP_SIZE);
        int x = (graphics.guiWidth() - totalWidth) / 2;
        int y = graphics.guiHeight() / 2 + 16;

        for (int i = 0; i < max; i++) {
            int tint = SPENT_TINT;
            if (i < charges) {
                tint = FILLED_TINT;
            } else if (charging && i == charges) {
                // The pip being filled pulses, so the wait reads as progress rather than a dead HUD.
                tint = pulsingTint(player.tickCount);
            }
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CHARGE_SPRITE, x + i * PIP_SPACING, y,
                    PIP_SIZE, PIP_SIZE, tint);
        }
    }

    private static int pulsingTint(int tickCount) {
        float wave = (Mth.sin(tickCount * 0.25F) + 1.0F) * 0.5F;
        int alpha = 0x50 + (int) (wave * 0x90);
        return alpha << 24 | 0xFFC773;
    }
}
