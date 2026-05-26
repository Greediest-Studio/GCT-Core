package com.smd.gctcore.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.MiscellaneousIcons;
import vazkii.botania.client.core.proxy.ClientProxy;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Abstract spark renderer with configurable color tinting.
 */
@SideOnly(Side.CLIENT)
public abstract class RenderCustomSpark<T extends Entity> extends Render<T> {

    public RenderCustomSpark(RenderManager manager) {
        super(manager);
    }

    /** Red channel (0.0 – 1.0) */
    protected abstract float getR();

    /** Green channel (0.0 – 1.0) */
    protected abstract float getG();

    /** Blue channel (0.0 – 1.0) */
    protected abstract float getB();

    @Override
    public void doRender(@Nonnull T entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        TextureAtlasSprite icon = MiscellaneousIcons.INSTANCE.sparkWorldIcon;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.alphaFunc(516, 0.05F);

        double time = ClientTickHandler.ticksInGame + partialTicks;
        time += new Random(entity.getEntityId()).nextInt();

        float a = 0.1F + (entity.isInvisible() ? 0F : 1F) * 0.8F;
        float alpha = (0.7F + 0.3F * (float) (Math.sin(time / 5.0D) + 0.5D) * 2.0F) * a;

        GlStateManager.color(getR(), getG(), getB(), alpha);

        float scale = 0.75F + 0.1F * (float) Math.sin(time / 10.0D);
        GlStateManager.scale(scale, scale, scale);
        bindEntityTexture(entity);
        Tessellator tessellator = Tessellator.getInstance();

        GlStateManager.pushMatrix();
        float rot = 180.0F - this.renderManager.playerViewY;
        GlStateManager.rotate(rot, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        renderSparkIcon(tessellator, icon);
        GlStateManager.popMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    private void renderSparkIcon(Tessellator tess, TextureAtlasSprite icon) {
        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();
        float size = 1.0F;
        float halfW = 0.5F;
        float offsetY = 0.25F;

        tess.getBuffer().begin(7, ClientProxy.POSITION_TEX_LMAP_NORMAL);
        tess.getBuffer().pos(0.0F - halfW, 0.0F - offsetY, 0.0D).tex(minU, maxV).lightmap(240, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
        tess.getBuffer().pos(size - halfW, 0.0F - offsetY, 0.0D).tex(maxU, maxV).lightmap(240, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
        tess.getBuffer().pos(size - halfW, size - offsetY, 0.0D).tex(maxU, minV).lightmap(240, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
        tess.getBuffer().pos(0.0F - halfW, size - offsetY, 0.0D).tex(minU, minV).lightmap(240, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
        tess.draw();
    }

    @Nonnull
    @Override
    protected ResourceLocation getEntityTexture(@Nonnull T entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
