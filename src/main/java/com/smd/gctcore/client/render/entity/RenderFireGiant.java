package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.client.model.entity.ModelFireGiant;
import com.smd.gctcore.common.entity.EntityFireGiant;
import com.smd.gctcore.Tags;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.geo.render.built.GeoBone;

@SideOnly(Side.CLIENT)
public class RenderFireGiant extends GeoEntityRenderer<EntityFireGiant> {
    private static final float MODEL_SCALE = 88.0F / 147.0F;
    private static final ResourceLocation GLOW = new ResourceLocation(
            Tags.MOD_ID, "textures/entity/fire_giant_glow.png");

    public RenderFireGiant(RenderManager manager) {
        super(manager, new ModelFireGiant());
        shadowSize = 1.4F;
    }

    @Override
    public void renderEarly(EntityFireGiant entity, float partialTicks,
                            float red, float green, float blue, float alpha) {
        GlStateManager.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }

    @Override
    public void renderAfter(EntityFireGiant entity, float partialTicks,
                            float red, float green, float blue, float alpha) {
        if (entity.isInvisible()) { return; }
        // Reuse the sampled bones and the already scaled matrix, without
        // applying renderEarly or the animation controller a second time.
        float lightX = OpenGlHelper.lastBrightnessX;
        float lightY = OpenGlHelper.lastBrightnessY;
        bindTexture(GLOW);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.disableLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        for (GeoBone bone : modelProvider.getModel(modelProvider.getModelLocation(entity)).topLevelBones) {
            renderRecursively(buffer, bone, 1.0F, 1.0F, 1.0F, alpha);
        }
        Tessellator.getInstance().draw();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX, lightY);
        GlStateManager.enableLighting();
        bindTexture(getEntityTexture(entity));
    }
}
