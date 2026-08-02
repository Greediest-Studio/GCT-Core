package com.smd.gctcore.client.extendedcrafting;

import com.smd.gctcore.common.integration.extendedcrafting.ContainerExtendedPatternTerminal;
import com.smd.gctcore.common.integration.extendedcrafting.ExtendedCraftingTier;
import com.smd.gctcore.common.network.GctNetworkHandler;
import com.smd.gctcore.common.network.PacketExtendedPatternTerminal;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;

import java.io.IOException;

public class GuiExtendedPatternTerminal extends GuiContainer {
    private static final int ENCODE = 1;
    private static final int CLEAR = 2;
    private final ContainerExtendedPatternTerminal container;

    public GuiExtendedPatternTerminal(ContainerExtendedPatternTerminal container) {
        super(container);
        this.container = container;
        ExtendedCraftingTier tier = container.terminal().tier();
        xSize = ContainerExtendedPatternTerminal.layoutWidth(tier);
        ySize = ContainerExtendedPatternTerminal.layoutHeight(tier);
    }

    @Override
    public void initGui() {
        super.initGui();
        Slot blank = inventorySlots.inventorySlots.get(container.blankContainerSlot());
        buttonList.add(new GuiButton(ENCODE, guiLeft + blank.xPos - 3, guiTop + 126, 22, 20, ">"));
        buttonList.add(new GuiButton(CLEAR, guiLeft + xSize - 24, guiTop + 4, 20, 20, "C"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        container.refreshPreview();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFFC6C6C6);
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + 1, 0xFFFFFFFF);
        drawRect(guiLeft, guiTop + ySize - 1, guiLeft + xSize, guiTop + ySize, 0xFF555555);
        for (Slot slot : inventorySlots.inventorySlots) drawSlotBackground(slot);
    }

    private void drawSlotBackground(Slot slot) {
        int x = guiLeft + slot.xPos - 1;
        int y = guiTop + slot.yPos - 1;
        drawRect(x, y, x + 18, y + 18, 0xFF8B8B8B);
        drawRect(x + 1, y + 1, x + 17, y + 17, 0xFFEEEEEE);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("container.gctcore.extended_pattern_terminal",
                I18n.format("tier.gctcore." + container.terminal().tier().id())), 8, 6, 0x404040);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == ENCODE) {
            GctNetworkHandler.CHANNEL.sendToServer(new PacketExtendedPatternTerminal(PacketExtendedPatternTerminal.ENCODE));
        } else if (button.id == CLEAR) {
            GctNetworkHandler.CHANNEL.sendToServer(new PacketExtendedPatternTerminal(PacketExtendedPatternTerminal.CLEAR));
        } else super.actionPerformed(button);
    }
}
