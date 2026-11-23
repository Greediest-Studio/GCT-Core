package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.client.model.entity.ModelStoneGiant;
import com.smd.gctcore.common.entity.EntityStoneGiant;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@SideOnly(Side.CLIENT)
public class RenderStoneGiant extends GeoEntityRenderer<EntityStoneGiant> {

    private static final float MODEL_SCALE = 88.0F / 127.0F;

    public RenderStoneGiant(RenderManager manager) {
        super(manager, new ModelStoneGiant());
        shadowSize = 1.2F;
    }

    @Override
    public void renderEarly(EntityStoneGiant entity, float partialTicks,
                            float red, float green, float blue, float alpha) {
        GlStateManager.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }
}
