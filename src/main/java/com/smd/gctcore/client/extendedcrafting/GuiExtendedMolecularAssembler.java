package com.smd.gctcore.client.extendedcrafting;

import com.smd.gctcore.common.integration.extendedcrafting.ContainerExtendedMolecularAssembler;
import com.smd.gctcore.common.integration.extendedcrafting.TileExtendedMolecularAssembler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;

public class GuiExtendedMolecularAssembler extends GuiContainer {
    private final ContainerExtendedMolecularAssembler container;

    public GuiExtendedMolecularAssembler(ContainerExtendedMolecularAssembler container) {
        super(container);
        this.container = container;
        xSize = ContainerExtendedMolecularAssembler.WIDTH;
        ySize = ContainerExtendedMolecularAssembler.HEIGHT;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFF20252B);
        drawRect(guiLeft + 3, guiTop + 3, guiLeft + xSize - 3, guiTop + ySize - 3,
                0xFF48515B);
        drawRect(guiLeft + 5, guiTop + 5, guiLeft + xSize - 5, guiTop + ySize - 5,
                0xFF303840);

        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 3; column++) {
                drawSlot(ContainerExtendedMolecularAssembler.CARD_LEFT + column * 18,
                        ContainerExtendedMolecularAssembler.CARD_TOP + row * 18);
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(ContainerExtendedMolecularAssembler.PLAYER_LEFT + column * 18,
                        ContainerExtendedMolecularAssembler.PLAYER_TOP + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(ContainerExtendedMolecularAssembler.PLAYER_LEFT + column * 18,
                    ContainerExtendedMolecularAssembler.PLAYER_TOP + 58);
        }

        TileExtendedMolecularAssembler tile = container.tile();
        int remaining = tile.getRemainingTicks();
        int duration = tile.getCraftDurationTicks();
        drawRect(guiLeft + 8, guiTop + 82, guiLeft + 168, guiTop + 88, 0xFF171B20);
        if (remaining > 0) {
            int width = (duration - Math.min(duration, remaining)) * 158 / duration;
            if (width > 0) {
                drawRect(guiLeft + 9, guiTop + 83, guiLeft + 9 + width, guiTop + 87,
                        0xFF5DCBFF);
            }
        }
    }

    private void drawSlot(int x, int y) {
        int left = guiLeft + x - 1;
        int top = guiTop + y - 1;
        drawRect(left, top, left + 18, top + 18, 0xFF15191D);
        drawRect(left + 1, top + 1, left + 17, top + 17, 0xFF8B969F);
        drawRect(left + 2, top + 2, left + 17, top + 17, 0xFF252B31);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        TileExtendedMolecularAssembler tile = container.tile();
        fontRenderer.drawString(I18n.format("container.gctcore.extended_assembler",
                I18n.format("tier.gctcore." + tile.tier().id())), 8, 6,
                ExtendedGuiTheme.text(tile.tier()));
        fontRenderer.drawString(I18n.format("gui.gctcore.extended_assembler.cards",
                tile.getAccelerationCardCount(), TileExtendedMolecularAssembler.ACCELERATION_SLOTS),
                8, 60, 0xFFAAB2BA);
        fontRenderer.drawString(I18n.format("gui.gctcore.extended_assembler.batch",
                tile.getBatchCrafts(), tile.getParallelCraftLimit()), 8, 71, 0xFFAAB2BA);
        fontRenderer.drawString(I18n.format("container.inventory"), 8, 91, 0xFFAAB2BA);
    }
}
