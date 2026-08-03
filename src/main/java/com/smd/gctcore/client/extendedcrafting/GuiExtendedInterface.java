package com.smd.gctcore.client.extendedcrafting;

import com.smd.gctcore.common.integration.extendedcrafting.ContainerExtendedInterface;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiExtendedInterface extends GuiContainer {
    private final ContainerExtendedInterface container;
    private final ResourceLocation texture;

    public GuiExtendedInterface(ContainerExtendedInterface container) {
        super(container);
        this.container = container;
        this.texture = new ResourceLocation("gctcore", "textures/gui/extended_crafting/"
                + container.tile().tier().id() + "_interface.png");
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
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(texture);
        drawScaledCustomSizeModalRect(guiLeft, guiTop, 0, 0, xSize, ySize,
                xSize, ySize, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("container.gctcore.extended_interface",
                I18n.format("tier.gctcore." + container.tile().tier().id())), 8, 6,
                ExtendedGuiTheme.text(container.tile().tier()));
        fontRenderer.drawString(I18n.format("container.inventory"), 8, 94, 0xFFAAB2BA);
    }
}
