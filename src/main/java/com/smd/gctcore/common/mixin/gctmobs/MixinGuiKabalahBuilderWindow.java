package com.smd.gctcore.common.mixin.gctmobs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.inventory.GuiContainer;

@Pseudo
@Mixin(targets = "net.mcreator.gctmobs.gui.GuiKabalahBuilder$GuiWindow", remap = false)
public abstract class MixinGuiKabalahBuilderWindow extends GuiContainer {

    // Required constructor bridge for mixin to extend GuiContainer
    public MixinGuiKabalahBuilderWindow() {
        super(null);
    }

    @Inject(method = "drawGuiContainerForegroundLayer", at = @At("HEAD"), cancellable = true)
    private void gctcore$cancelAndDrawChineseLabels(int mouseX, int mouseY, CallbackInfo ci) {
        // Cancel the original foreground drawing and draw Chinese labels with requested swaps.
        ci.cancel();
        int color = -12829636;

        // Title
        this.fontRenderer.drawString("     Kabalah Builder", 49, 4, color);

        // Draw fixed labels (English placeholders left as-is) and swapped Chinese names
        // Positions follow original GUI layout but we replace the four names as requested.
        this.fontRenderer.drawString(" Ain", 3, 78, color);
        this.fontRenderer.drawString(" Soph", 0, 53, color);
        this.fontRenderer.drawString(" Aur", 3, 28, color);
        this.fontRenderer.drawString(" Kether", 72, 16, color);

        // Swap 智慧(Chokmah) <-> 理解(Binah)
        this.fontRenderer.drawString(" 理解", 106, 28, color); // originally Chokmah at (106,28)
        this.fontRenderer.drawString(" 智慧", 38, 28, color); // originally Binah at (38,28)

        this.fontRenderer.drawString("  Tiphareth", 64, 65, color);
        this.fontRenderer.drawString(" Netzach", 34, 78, color);
        this.fontRenderer.drawString("Hod", 115, 78, color);
        this.fontRenderer.drawString("Yesod", 74, 90, color);
        this.fontRenderer.drawString(" Malkuth", 70, 115, color);

        // Swap 慈悲(Chesed) <-> 严格(Gevurah)
        this.fontRenderer.drawString(" 严格", 108, 53, color); // originally Chesed at (108,53)
        this.fontRenderer.drawString(" 慈悲", 34, 53, color); // originally Gevurah at (34,53)
    }
}
