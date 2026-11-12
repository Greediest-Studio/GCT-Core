package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.client.model.entity.ModelCloudGiantPriest;
import com.smd.gctcore.common.entity.EntityCloudGiantPriest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@SideOnly(Side.CLIENT)
public class RenderCloudGiantPriest extends GeoEntityRenderer<EntityCloudGiantPriest> {

    // Feet are at Y=0 and the hair reaches Y=114; the staff is deliberately taller.
    private static final float MODEL_SCALE = 64.0F / 114.0F;

    public RenderCloudGiantPriest(RenderManager renderManager) {
        super(renderManager, new ModelCloudGiantPriest());
        this.shadowSize = 0.8F;
    }

    @Override
    public void renderEarly(EntityCloudGiantPriest entity, float partialTicks,
                            float red, float green, float blue, float alpha) {
        GlStateManager.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }

    @Override
    protected float getDeathMaxRotation(EntityCloudGiantPriest entity) {
        // The authored death animation supplies the complete collapse and final pose.
        return 0.0F;
    }

    @Override
    protected boolean setDoRenderBrightness(EntityCloudGiantPriest entity, float partialTicks) {
        // Vanilla keeps the red death flash enabled for the entire extended corpse lifetime.
        return entity.deathTime == 0 && super.setDoRenderBrightness(entity, partialTicks);
    }
}
