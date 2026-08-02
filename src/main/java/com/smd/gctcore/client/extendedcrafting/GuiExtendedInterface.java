package com.smd.gctcore.client.extendedcrafting;

import com.smd.gctcore.common.integration.extendedcrafting.ContainerExtendedInterface;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;

public class GuiExtendedInterface extends GuiContainer {
    private final ContainerExtendedInterface container;

    public GuiExtendedInterface(ContainerExtendedInterface container) {
        super(container);
        this.container = container;
        xSize = 176;
        ySize = 186;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFFC6C6C6);
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + 1, 0xFFFFFFFF);
        drawRect(guiLeft, guiTop + ySize - 1, guiLeft + xSize, guiTop + ySize, 0xFF555555);
        for (Slot slot : inventorySlots.inventorySlots) {
            int x = guiLeft + slot.xPos - 1;
            int y = guiTop + slot.yPos - 1;
            drawRect(x, y, x + 18, y + 18, 0xFF8B8B8B);
            drawRect(x + 1, y + 1, x + 17, y + 17, 0xFFEEEEEE);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("container.gctcore.extended_interface",
                I18n.format("tier.gctcore." + container.tile().tier().id())), 8, 6, 0x404040);
    }
}
