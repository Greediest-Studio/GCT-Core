package com.smd.gctcore.client.integration.astralsorcery;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.integration.astralsorcery.EternalCollectorCrystalTile;
import hellfirepvp.astralsorcery.client.models.obj.OBJModelLibrary;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.TextureHelper;
import hellfirepvp.astralsorcery.client.util.item.IItemRenderer;
import hellfirepvp.astralsorcery.client.util.resource.BindableResource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

/** Uses the same OBJ crystal as Astral Sorcery with the GCT-Core eternal texture. */
public class EternalCollectorCrystalRenderer extends TileEntitySpecialRenderer<EternalCollectorCrystalTile>
        implements IItemRenderer {

    private static final BindableResource TEXTURE = new BindableResource(
            Tags.MOD_ID + ":textures/models/crystal_big_eternal.png");
    private static int displayList = -1;

    @Override
    public void render(EternalCollectorCrystalTile tile, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (tile.doesSeeSky()) {
            long seed = 1553015L ^ tile.getPos().getX() ^ tile.getPos().getY() ^ tile.getPos().getZ();
            Color color = tile.getConstellation() == null
                    ? EternalCrystalColor.COLOR
                    : tile.getConstellation().getConstellationColor();
            RenderingUtils.renderLightRayEffects(x + 0.5, y + 0.5, z + 0.5, color, seed,
                    Minecraft.getMinecraft().world.getTotalWorldTime(), 20,
                    tile.isEnhanced() ? 1.4F : 1.0F, tile.isEnhanced() ? 50 : 50, 25);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        renderCrystal(true);
        GlStateManager.popMatrix();
    }

    private static void renderCrystal(boolean bounce) {
        GlStateManager.pushMatrix();
        if (bounce && Minecraft.getMinecraft().world != null) {
            int t = (int) (Minecraft.getMinecraft().world.getTotalWorldTime() & 255);
            float perc = MathHelper.cos((float) (((256 - t) / 256F) * 2 * Math.PI));
            GlStateManager.translate(0, 0.03 * perc, 0);
        }
        TextureHelper.refreshTextureBindState();
        RenderHelper.disableStandardItemLighting();
        renderTile();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private static void renderTile() {
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.13F, 0.13F, 0.13F);
        TEXTURE.bind();
        if (displayList == -1) {
            displayList = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(displayList, GL11.GL_COMPILE);
            OBJModelLibrary.bigCrystal.renderAll(true);
            GlStateManager.glEndList();
        }
        GlStateManager.callList(displayList);
        GlStateManager.popMatrix();
    }

    @Override
    public void render(ItemStack stack) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5, 0, 0.5);
        RenderHelper.disableStandardItemLighting();
        renderTile();
        GlStateManager.popMatrix();
        RenderHelper.enableStandardItemLighting();
    }

    private static final class EternalCrystalColor {
        private static final Color COLOR = new Color(0x9DEBFF);
    }
}
