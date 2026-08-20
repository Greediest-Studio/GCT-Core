package com.smd.gctcore.misc;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityReversedAlfMaster;
import com.smd.gctcore.common.entity.botania.EntityAlfSpark;
import com.smd.gctcore.common.entity.botania.EntityGaiaSpark;
import com.smd.gctcore.common.entity.botania.EntityJoetunheimSpark;
import com.smd.gctcore.common.entity.botania.EntityNidavellirSpark;
import com.smd.gctcore.common.entity.botania.EntityVanaheimSpark;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

public class EntityRegistrar {

    public static EntityEntry REVERSED_ALF_MASTER;
    public static EntityEntry ALF_SPARK;
    public static EntityEntry GAIA_SPARK;
    public static EntityEntry JOETUNHEIM_SPARK;
    public static EntityEntry NIDAVELLIR_SPARK;
    public static EntityEntry VANAHEIM_SPARK;

    public static void init() {
        REVERSED_ALF_MASTER = EntityEntryBuilder.create()
                .entity(EntityReversedAlfMaster.class)
                .id(new ResourceLocation(Tags.MOD_ID, "reversed_alf_master"), 1)
                .name("reversed_alf_master")
                .tracker(64, 3, true)
                .egg(-11534229, -5273345)
                .build();

        ALF_SPARK = EntityEntryBuilder.create()
                .entity(EntityAlfSpark.class)
                .id(new ResourceLocation(Tags.MOD_ID, "alf_spark"), 2)
                .name("alf_spark")
                .tracker(64, 3, true)
                .build();

        GAIA_SPARK = EntityEntryBuilder.create()
                .entity(EntityGaiaSpark.class)
                .id(new ResourceLocation(Tags.MOD_ID, "gaia_spark"), 3)
                .name("gaia_spark")
                .tracker(64, 3, true)
                .build();

        JOETUNHEIM_SPARK = EntityEntryBuilder.create()
                .entity(EntityJoetunheimSpark.class)
                .id(new ResourceLocation(Tags.MOD_ID, "joetunheim_spark"), 4)
                .name("joetunheim_spark")
                .tracker(64, 3, true)
                .build();

        NIDAVELLIR_SPARK = EntityEntryBuilder.create()
                .entity(EntityNidavellirSpark.class)
                .id(new ResourceLocation(Tags.MOD_ID, "nidavellir_spark"), 5)
                .name("nidavellir_spark")
                .tracker(64, 3, true)
                .build();

        VANAHEIM_SPARK = EntityEntryBuilder.create()
                .entity(EntityVanaheimSpark.class)
                .id(new ResourceLocation(Tags.MOD_ID, "vanaheim_spark"), 6)
                .name("vanaheim_spark")
                .tracker(64, 3, true)
                .build();
    }

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().registerAll(
                REVERSED_ALF_MASTER,
                ALF_SPARK,
                GAIA_SPARK,
                JOETUNHEIM_SPARK,
                NIDAVELLIR_SPARK,
                VANAHEIM_SPARK
        );
    }
}
