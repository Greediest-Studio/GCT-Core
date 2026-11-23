package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.client.model.entity.ModelFomorianNoble;
import com.smd.gctcore.common.entity.EntityFomorianNoble;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@SideOnly(Side.CLIENT)
public class RenderFomorianNoble extends GeoEntityRenderer<EntityFomorianNoble> {

    private static final float MODEL_SCALE = 80.0F / 125.0F;

    public RenderFomorianNoble(RenderManager renderManager) {
        super(renderManager, new ModelFomorianNoble());
        shadowSize = 1.1F;
    }

    @Override
    public void renderEarly(EntityFomorianNoble entity, float partialTicks,
                            float red, float green, float blue, float alpha) {
        GlStateManager.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }
}
