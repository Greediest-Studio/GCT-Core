package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.client.model.entity.ModelFomorianWarlock;
import com.smd.gctcore.common.entity.EntityFomorianWarlock;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@SideOnly(Side.CLIENT)
public class RenderFomorianWarlock extends GeoEntityRenderer<EntityFomorianWarlock> {

    // Rest-pose height includes the crooked torso and counter-rotated head.
    private static final float MODEL_SCALE = 80.0F / 118.93239F;

    public RenderFomorianWarlock(RenderManager renderManager) {
        super(renderManager, new ModelFomorianWarlock());
        shadowSize = 1.2F;
    }

    @Override
    public void renderEarly(EntityFomorianWarlock entity, float partialTicks,
                            float red, float green, float blue, float alpha) {
        GlStateManager.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }
}
