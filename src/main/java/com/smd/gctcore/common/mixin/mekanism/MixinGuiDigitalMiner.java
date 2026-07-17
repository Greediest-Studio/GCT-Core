package com.smd.gctcore.common.mixin.mekanism;

import com.smd.gctcore.common.config.GCTCompatConfig;
import com.smd.gctcore.common.integration.mekanism.DigitalMinerHarvestAccess;
import mekanism.client.SpecialColors;
import mekanism.client.gui.machine.GuiDigitalMiner;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws Digital Miner mining level. Status panel text/layout is left vanilla.
 */
@Mixin(value = GuiDigitalMiner.class, remap = false)
public abstract class MixinGuiDigitalMiner {

    private static final int LEVEL_TEXT_X = 13;
    private static final int LEVEL_TEXT_Y = 50;
    private static final float LEVEL_TEXT_SCALE = 0.8F;

    @Inject(method = "drawForegroundText", at = @At("RETURN"))
    private void gct$drawMiningLevel(int mouseX, int mouseY, CallbackInfo ci) {
        if (!GCTCompatConfig.mekanismIntegration.enableDigitalMinerHarvestLimit) {
            return;
        }
        GuiDigitalMiner gui = (GuiDigitalMiner) (Object) this;
        int level = 0;
        TileEntityDigitalMiner tile = gui.getTileEntity();
        if (tile instanceof DigitalMinerHarvestAccess) {
            level = ((DigitalMinerHarvestAccess) tile).gct$getMiningLevel();
        }
        String text = "Level:" + level;
        int color = SpecialColors.TEXT_SCREEN.argb();
        GlStateManager.pushMatrix();
        GlStateManager.translate(LEVEL_TEXT_X, LEVEL_TEXT_Y, 0);
        GlStateManager.scale(LEVEL_TEXT_SCALE, LEVEL_TEXT_SCALE, LEVEL_TEXT_SCALE);
        gui.getFont().drawString(text, 0, 0, color);
        GlStateManager.popMatrix();
    }
}
