package com.smd.gctcore.client.render.tile;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.tile.NilfheimPortalTileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderNilfheimPortal extends TileEntitySpecialRenderer<NilfheimPortalTileEntity> {
    private static final ResourceLocation SWIRL_TEXTURE = new ResourceLocation(Tags.MOD_ID, "blocks/nilfheim/nilfheim_portal_swirl");
    private static TextureAtlasSprite portalSprite;

    public static void registerTextures(TextureStitchEvent.Pre event) {
        portalSprite = event.getMap().registerSprite(SWIRL_TEXTURE);
    }

    @Override
    public void render(NilfheimPortalTileEntity tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!tile.isPortalActiveForRender() || tile.getWorld() == null || portalSprite == null) {
            return;
        }

        float pulse = 0.78F + 0.18F * (float) Math.sin((tile.getWorld().getTotalWorldTime() + partialTicks) * 0.08F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(-1.0F, 1.0F, 0.25F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 1 / 255F);
        GlStateManager.disableCull();
        GlStateManager.disableLighting();
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.color(0.58F, 0.84F, 1.0F, pulse);

        if (tile.getPortalAxisForRender() == 2) {
            GlStateManager.translate(1.25F, 0.0F, 1.75F);
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        }

        renderLayer(0.0F);
        renderLayer(0.5F);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void renderLayer(float zOffset) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        float minU = portalSprite.getMinU();
        float maxU = portalSprite.getMaxU();
        float minV = portalSprite.getMinV();
        float maxV = portalSprite.getMaxV();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
        addVertex(buffer, 0.0D, 0.0D, zOffset, minU, maxV);
        addVertex(buffer, 3.0D, 0.0D, zOffset, maxU, maxV);
        addVertex(buffer, 3.0D, 3.0D, zOffset, maxU, minV);
        addVertex(buffer, 0.0D, 3.0D, zOffset, minU, minV);
        tessellator.draw();
    }

    private static void addVertex(BufferBuilder buffer, double x, double y, double z, float u, float v) {
        buffer.pos(x, y, z).tex(u, v).lightmap(240, 240).color(255, 255, 255, 255).endVertex();
    }
}
