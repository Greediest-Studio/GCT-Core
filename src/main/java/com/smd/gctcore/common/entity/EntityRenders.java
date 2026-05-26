package com.smd.gctcore.common.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.client.render.entity.RenderAlfSpark;
import com.smd.gctcore.client.render.entity.RenderGaiaSpark;
import com.smd.gctcore.common.entity.botania.EntityAlfSpark;
import com.smd.gctcore.common.entity.botania.EntityGaiaSpark;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class EntityRenders {
    
    public static void registerEntityRenders() {
        RenderingRegistry.registerEntityRenderingHandler(
                EntityReversedAlfMaster.class,
                renderManager -> new RenderLiving<EntityReversedAlfMaster>(
                        renderManager,
                        new EntityReversedAlfMaster.ModelAlfMaster(),
                        0.5F
                ) {
                    @Override
                    protected ResourceLocation getEntityTexture(EntityReversedAlfMaster entity) {
                        return new ResourceLocation(Tags.MOD_ID, "textures/entity/reversed_alf_master.png");
                    }
                }
        );

        RenderingRegistry.registerEntityRenderingHandler(
                EntityAlfSpark.class,
                RenderAlfSpark::new
        );

        RenderingRegistry.registerEntityRenderingHandler(
                EntityGaiaSpark.class,
                RenderGaiaSpark::new
        );
    }
}
