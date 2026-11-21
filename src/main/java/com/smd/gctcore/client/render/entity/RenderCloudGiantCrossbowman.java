package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.client.model.entity.ModelCloudGiantCrossbowman;
import com.smd.gctcore.common.entity.EntityCloudGiantCrossbowman;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@SideOnly(Side.CLIENT)
public class RenderCloudGiantCrossbowman extends GeoEntityRenderer<EntityCloudGiantCrossbowman> {

    private static final float MODEL_SCALE = 64.0F / 114.0F;

    public RenderCloudGiantCrossbowman(RenderManager renderManager) {
        super(renderManager, new ModelCloudGiantCrossbowman());
        this.shadowSize = 0.8F;
    }

    @Override
    public void renderEarly(EntityCloudGiantCrossbowman entity, float partialTicks,
                            float red, float green, float blue, float alpha) {
        GlStateManager.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }
}
