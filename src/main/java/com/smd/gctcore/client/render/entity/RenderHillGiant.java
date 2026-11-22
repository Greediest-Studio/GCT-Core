package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.client.model.entity.ModelHillGiant;
import com.smd.gctcore.common.entity.EntityHillGiant;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@SideOnly(Side.CLIENT)
public class RenderHillGiant extends GeoEntityRenderer<EntityHillGiant> {

    // The authored 88 model units span five blocks after scaling.
    private static final float MODEL_SCALE = 80.0F / 88.0F;

    public RenderHillGiant(RenderManager renderManager) {
        super(renderManager, new ModelHillGiant());
        this.shadowSize = 1.3F;
    }

    @Override
    public void renderEarly(EntityHillGiant entity, float partialTicks,
                            float red, float green, float blue, float alpha) {
        GlStateManager.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }
}
