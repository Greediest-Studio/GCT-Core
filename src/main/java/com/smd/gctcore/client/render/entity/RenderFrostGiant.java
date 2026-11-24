package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.client.model.entity.ModelFrostGiant;
import com.smd.gctcore.common.entity.EntityFrostGiant;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@SideOnly(Side.CLIENT)
public class RenderFrostGiant extends GeoEntityRenderer<EntityFrostGiant> {
    // The saved 130-unit model is five blocks tall in game.
    private static final float MODEL_SCALE = 80.0F / 130.0F;

    public RenderFrostGiant(RenderManager manager) {
        super(manager, new ModelFrostGiant());
        shadowSize = 1.3F;
    }

    @Override
    public void renderEarly(EntityFrostGiant entity, float partialTicks,
                            float red, float green, float blue, float alpha) {
        GlStateManager.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }
}
