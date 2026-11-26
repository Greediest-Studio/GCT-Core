package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.client.model.entity.ModelEttin;
import com.smd.gctcore.common.entity.EntityEttin;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@SideOnly(Side.CLIENT)
public class RenderEttin extends GeoEntityRenderer<EntityEttin> {
    private static final float MODEL_SCALE = 72.0F / 100.0F;

    public RenderEttin(RenderManager manager) {
        super(manager, new ModelEttin());
        shadowSize = 1.1F;
    }

    @Override
    public void renderEarly(EntityEttin entity, float partialTicks,
                            float red, float green, float blue, float alpha) {
        GlStateManager.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }

    @Override
    protected float getDeathMaxRotation(EntityEttin entity) { return 0.0F; }

    @Override
    protected boolean setDoRenderBrightness(EntityEttin entity, float partialTicks) {
        return entity.deathTime == 0 && super.setDoRenderBrightness(entity, partialTicks);
    }
}
