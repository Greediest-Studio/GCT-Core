package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.client.model.entity.ModelCloudGiantSentinel;
import com.smd.gctcore.common.entity.EntityCloudGiantSentinel;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@SideOnly(Side.CLIENT)
public class RenderCloudGiantSentinel extends GeoEntityRenderer<EntityCloudGiantSentinel> {

    // Keep the same authored proportions and scale as the cloud giant priest.
    private static final float MODEL_SCALE = 64.0F / 114.0F;

    public RenderCloudGiantSentinel(RenderManager renderManager) {
        super(renderManager, new ModelCloudGiantSentinel());
        this.shadowSize = 0.8F;
    }

    @Override
    public void renderEarly(EntityCloudGiantSentinel entity, float partialTicks,
                            float red, float green, float blue, float alpha) {
        GlStateManager.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }
}
