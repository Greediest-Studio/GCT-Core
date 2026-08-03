package com.smd.gctcore.client.extendedcrafting;

import com.smd.gctcore.common.integration.extendedcrafting.ExtendedCraftingTier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

final class GuiExtendedCraftingButton extends GuiButton {
    private final int accent;

    GuiExtendedCraftingButton(int id, int x, int y, int width, int height, String text,
                              ExtendedCraftingTier tier) {
        super(id, x, y, width, height, text);
        accent = ExtendedGuiTheme.accent(tier);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        int border = hovered ? accent : 0xFF46515C;
        int fill = hovered ? 0xFF26313A : 0xFF151B22;
        drawRect(x, y, x + width, y + height, 0xFF07090C);
        drawRect(x + 1, y + 1, x + width - 1, y + height - 1, border);
        drawRect(x + 2, y + 2, x + width - 2, y + height - 2, fill);
        mouseDragged(mc, mouseX, mouseY);
        int color = enabled ? (hovered ? 0xFFFFFFFF : accent) : 0xFF666666;
        drawCenteredString(mc.fontRenderer, displayString, x + width / 2,
                y + (height - 8) / 2, color);
    }
}
