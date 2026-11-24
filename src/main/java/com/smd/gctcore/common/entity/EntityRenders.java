package com.smd.gctcore.common.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.client.render.entity.RenderAlfSpark;
import com.smd.gctcore.client.render.entity.RenderGaiaSpark;
import com.smd.gctcore.client.render.entity.RenderJoetunheimSpark;
import com.smd.gctcore.client.render.entity.RenderNidavellirSpark;
import com.smd.gctcore.client.render.entity.RenderVanaheimSpark;
import com.smd.gctcore.client.render.entity.RenderCloudGiantPriest;
import com.smd.gctcore.client.render.entity.RenderCloudGiantCrystal;
import com.smd.gctcore.client.render.entity.RenderCloudGiantSentinel;
import com.smd.gctcore.client.render.entity.RenderCloudGiantCrossbowman;
import com.smd.gctcore.client.render.entity.RenderCloudGiantBolt;
import com.smd.gctcore.client.render.entity.RenderHillGiant;
import com.smd.gctcore.client.render.entity.RenderFrostGiant;
import com.smd.gctcore.client.render.entity.RenderFireGiant;
import com.smd.gctcore.client.render.entity.RenderFireGiantChain;
import com.smd.gctcore.client.render.entity.RenderStoneGiant;
import com.smd.gctcore.client.render.entity.RenderStoneGiantBoulder;
import com.smd.gctcore.client.render.entity.RenderFomorianNoble;
import com.smd.gctcore.client.render.entity.RenderFomorianWarlock;
import com.smd.gctcore.client.render.entity.RenderGiantSpell;
import com.smd.gctcore.common.entity.botania.EntityAlfSpark;
import com.smd.gctcore.common.entity.botania.EntityGaiaSpark;
import com.smd.gctcore.common.entity.botania.EntityJoetunheimSpark;
import com.smd.gctcore.common.entity.botania.EntityNidavellirSpark;
import com.smd.gctcore.common.entity.botania.EntityVanaheimSpark;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class EntityRenders {
    
    public static void registerEntityRenders() {
        RenderingRegistry.registerEntityRenderingHandler(
                EntityCloudGiantPriest.class,
                RenderCloudGiantPriest::new
        );
        RenderingRegistry.registerEntityRenderingHandler(
                EntityCloudGiantCrystal.class,
                RenderCloudGiantCrystal::new
        );
        RenderingRegistry.registerEntityRenderingHandler(
                EntityCloudGiantSentinel.class,
                RenderCloudGiantSentinel::new
        );
        RenderingRegistry.registerEntityRenderingHandler(
                EntityCloudGiantCrossbowman.class,
                RenderCloudGiantCrossbowman::new
        );
        RenderingRegistry.registerEntityRenderingHandler(
                EntityCloudGiantBolt.class,
                RenderCloudGiantBolt::new
        );
        RenderingRegistry.registerEntityRenderingHandler(
                EntityHillGiant.class,
                RenderHillGiant::new
        );
        RenderingRegistry.registerEntityRenderingHandler(EntityStoneGiant.class, RenderStoneGiant::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFrostGiant.class, RenderFrostGiant::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFireGiant.class, RenderFireGiant::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFireGiantChain.class, RenderFireGiantChain::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityStoneGiantBoulder.class, RenderStoneGiantBoulder::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFomorianNoble.class, RenderFomorianNoble::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFomorianWarlock.class, RenderFomorianWarlock::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGiantSpell.class, RenderGiantSpell::new);

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

        RenderingRegistry.registerEntityRenderingHandler(
                EntityJoetunheimSpark.class,
                RenderJoetunheimSpark::new
        );

        RenderingRegistry.registerEntityRenderingHandler(
                EntityNidavellirSpark.class,
                RenderNidavellirSpark::new
        );

        RenderingRegistry.registerEntityRenderingHandler(
                EntityVanaheimSpark.class,
                RenderVanaheimSpark::new
        );
    }
}
