package com.akitain.explorationreloaded.mixin.client.flight;

import com.akitain.explorationreloaded.flight.FlightEnchantments;
import com.akitain.explorationreloaded.flight.FlightState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Flying without knowing how much thrust is left is guesswork, so the charges sit under the crosshair:
 * one pip per charge, dimmed once spent. Only drawn while gliding with Smokestack equipped.
 */
@Mixin(Gui.class)
public class SmokestackHudMixin {

    private static final int PIP_WIDTH = 7;
    private static final int PIP_HEIGHT = 3;
    private static final int PIP_SPACING = 9;
    private static final int FILLED = 0xFFFFC773;
    private static final int SPENT = 0x60FFFFFF;

    @Inject(method = "extractCrosshair", at = @At("TAIL"))
    private void erExtractSmokestackCharges(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isFallFlying()) {
            return;
        }

        int max = FlightEnchantments.smokestackLevel(player);
        if (max <= 0) {
            return;
        }
        int charges = FlightState.charges(player);

        int totalWidth = max * PIP_SPACING - (PIP_SPACING - PIP_WIDTH);
        int x = (graphics.guiWidth() - totalWidth) / 2;
        int y = graphics.guiHeight() / 2 + 16;

        for (int i = 0; i < max; i++) {
            int left = x + i * PIP_SPACING;
            graphics.fill(left, y, left + PIP_WIDTH, y + PIP_HEIGHT, i < charges ? FILLED : SPENT);
        }
    }
}
