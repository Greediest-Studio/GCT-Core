package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityFireGiantChain;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Alternating solid cuboid links retain a Minecraft silhouette at every camera angle. */
@SideOnly(Side.CLIENT)
public class RenderFireGiantChain extends Render<EntityFireGiantChain> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Tags.MOD_ID, "textures/entity/fire_giant_chain.png");
    private final LinkModel model = new LinkModel();

    public RenderFireGiantChain(RenderManager manager) {
        super(manager);
        shadowSize = 0.0F;
    }

    @Override
    public void doRender(EntityFireGiantChain chain, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        if (chain.getOwner() == null) { return; }
        Vec3d start = chain.getCastingHandPosition(partialTicks);
        Vec3d end = chain.getEndpoint(partialTicks);
        Vec3d direction = end.subtract(start);
        double length = direction.length();
        if (length < 0.02D) { return; }
        double originX = chain.prevPosX + (chain.posX - chain.prevPosX) * partialTicks;
        double originY = chain.prevPosY + (chain.posY - chain.prevPosY) * partialTicks;
        double originZ = chain.prevPosZ + (chain.posZ - chain.prevPosZ) * partialTicks;
        float oldLightX = OpenGlHelper.lastBrightnessX;
        float oldLightY = OpenGlHelper.lastBrightnessY;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + start.x - originX, y + start.y - originY, z + start.z - originZ);
        GlStateManager.rotate((float) Math.toDegrees(Math.atan2(direction.x, direction.z)), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) -Math.toDegrees(Math.atan2(direction.y,
                Math.sqrt(direction.x * direction.x + direction.z * direction.z))), 1.0F, 0.0F, 0.0F);
        bindTexture(TEXTURE);
        GlStateManager.disableLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int count = Math.min(48, Math.max(1, (int) Math.ceil(length / 0.28D)));
        for (int i = 0; i < count; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, 0.0D, (i + 0.5D) * length / count);
            GlStateManager.rotate((i & 1) == 0 ? 0.0F : 90.0F, 0.0F, 0.0F, 1.0F);
            model.link.render(0.0625F);
            GlStateManager.popMatrix();
        }
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, oldLightX, oldLightY);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFireGiantChain entity) { return TEXTURE; }

    private static final class LinkModel extends ModelBase {
        final ModelRenderer link;

        LinkModel() {
            textureWidth = 32;
            textureHeight = 16;
            link = new ModelRenderer(this, 0, 0);
            link.addBox(-2.0F, -0.5F, -3.0F, 4, 1, 1);
            link.addBox(-2.0F, -0.5F, 2.0F, 4, 1, 1);
            link.setTextureOffset(0, 4);
            link.addBox(-2.0F, -0.5F, -2.0F, 1, 1, 4);
            link.addBox(1.0F, -0.5F, -2.0F, 1, 1, 4);
        }
    }
}
