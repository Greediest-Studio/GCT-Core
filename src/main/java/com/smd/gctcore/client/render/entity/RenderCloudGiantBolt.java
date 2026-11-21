package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.client.model.entity.ModelCloudGiantBolt;
import com.smd.gctcore.common.entity.EntityCloudGiantBolt;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCloudGiantBolt extends Render<EntityCloudGiantBolt> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Tags.MOD_ID, "textures/entity/cloud_giant_bolt.png");
    private static final float MODEL_SCALE = 0.5F;
    private final ModelCloudGiantBolt model = new ModelCloudGiantBolt();

    public RenderCloudGiantBolt(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.1F;
    }

    @Override
    public void doRender(EntityCloudGiantBolt entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        float yaw = entity.prevRotationYaw
                + MathHelper.wrapDegrees(entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        float pitch = entity.prevRotationPitch
                + MathHelper.wrapDegrees(entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

        this.bindEntityTexture(entity);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        // Arrow yaw/pitch describe +Z travel. The authored bolt instead points -Z.
        GlStateManager.rotate(yaw + 180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        GlStateManager.enableRescaleNormal();
        GlStateManager.disableCull();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        this.model.render(1.0F / 16.0F);

        if (this.renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.enableCull();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCloudGiantBolt entity) {
        return TEXTURE;
    }
}
