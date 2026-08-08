package com.smd.gctcore.client.extendedcrafting;

import com.smd.gctcore.common.integration.extendedcrafting.ContainerExtendedPatternTerminal;
import com.smd.gctcore.common.integration.extendedcrafting.ExtendedCraftingTier;
import com.smd.gctcore.common.network.GctNetworkHandler;
import com.smd.gctcore.common.network.PacketExtendedPatternTerminal;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiExtendedPatternTerminal extends GuiContainer {
    private static final int ENCODE = 1;
    private static final int CLEAR = 2;
    private final ContainerExtendedPatternTerminal container;
    private final ResourceLocation texture;

    public GuiExtendedPatternTerminal(ContainerExtendedPatternTerminal container) {
        super(container);
        this.container = container;
        ExtendedCraftingTier tier = container.terminal().tier();
        texture = new ResourceLocation("gctcore", "textures/gui/extended_crafting/"
                + tier.id() + "_pattern_terminal.png");
        xSize = ContainerExtendedPatternTerminal.layoutWidth(tier);
        ySize = ContainerExtendedPatternTerminal.layoutHeight(tier);
    }

    @Override
    public void initGui() {
        super.initGui();
        ExtendedCraftingTier tier = container.terminal().tier();
        int sideX = ContainerExtendedPatternTerminal.layoutSideX(tier);
        buttonList.add(new GuiExtendedCraftingButton(ENCODE, guiLeft + sideX + 20,
                guiTop + ContainerExtendedPatternTerminal.ENCODED_TOP, 20, 18, ">", tier));
        buttonList.add(new GuiExtendedCraftingButton(CLEAR, guiLeft + sideX + 20,
                guiTop + ContainerExtendedPatternTerminal.BLANK_TOP, 20, 18, "C", tier));
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
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(texture);
        drawScaledCustomSizeModalRect(guiLeft, guiTop, 0, 0, xSize, ySize,
                xSize, ySize, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("container.gctcore.extended_pattern_terminal",
                I18n.format("tier.gctcore." + container.terminal().tier().id())), 8, 6,
                ExtendedGuiTheme.text(container.terminal().tier()));
        fontRenderer.drawString(I18n.format("container.inventory"),
                ContainerExtendedPatternTerminal.layoutPlayerLeft(container.terminal().tier()),
                ContainerExtendedPatternTerminal.layoutPlayerTop(container.terminal().tier()) - 11,
                0xFFAAB2BA);
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
