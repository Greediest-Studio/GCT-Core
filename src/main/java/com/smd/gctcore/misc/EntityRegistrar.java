package com.smd.gctcore.misc;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityReversedAlfMaster;
import com.smd.gctcore.common.entity.EntityCloudGiantPriest;
import com.smd.gctcore.common.entity.EntityCloudGiantCrystal;
import com.smd.gctcore.common.entity.EntityCloudGiantSentinel;
import com.smd.gctcore.common.entity.EntityCloudGiantCrossbowman;
import com.smd.gctcore.common.entity.EntityCloudGiantBolt;
import com.smd.gctcore.common.entity.EntityHillGiant;
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
    public static EntityEntry CLOUD_GIANT_PRIEST;
    public static EntityEntry CLOUD_GIANT_CRYSTAL;
    public static EntityEntry CLOUD_GIANT_SENTINEL;
    public static EntityEntry CLOUD_GIANT_CROSSBOWMAN;
    public static EntityEntry CLOUD_GIANT_BOLT;
    public static EntityEntry HILL_GIANT;

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

        CLOUD_GIANT_PRIEST = EntityEntryBuilder.create()
                .entity(EntityCloudGiantPriest.class)
                .id(new ResourceLocation(Tags.MOD_ID, "cloud_giant_priest"), 7)
                .name("cloud_giant_priest")
                .tracker(128, 1, true)
                .egg(0xC9E0EA, 0x70778F)
                .build();

        CLOUD_GIANT_CRYSTAL = EntityEntryBuilder.create()
                .entity(EntityCloudGiantCrystal.class)
                .id(new ResourceLocation(Tags.MOD_ID, "cloud_giant_crystal"), 8)
                .name("cloud_giant_crystal")
                .tracker(128, 1, true)
                .build();

        CLOUD_GIANT_SENTINEL = EntityEntryBuilder.create()
                .entity(EntityCloudGiantSentinel.class)
                .id(new ResourceLocation(Tags.MOD_ID, "cloud_giant_sentinel"), 9)
                .name("cloud_giant_sentinel")
                .tracker(128, 1, true)
                .egg(0xDBDFD4, 0x758EAA)
                .build();

        CLOUD_GIANT_CROSSBOWMAN = EntityEntryBuilder.create()
                .entity(EntityCloudGiantCrossbowman.class)
                .id(new ResourceLocation(Tags.MOD_ID, "cloud_giant_crossbowman"), 10)
                .name("cloud_giant_crossbowman")
                .tracker(128, 1, true)
                .egg(0x39536F, 0xDBDFD4)
                .build();

        CLOUD_GIANT_BOLT = EntityEntryBuilder.create()
                .entity(EntityCloudGiantBolt.class)
                .id(new ResourceLocation(Tags.MOD_ID, "cloud_giant_bolt"), 11)
                .name("cloud_giant_bolt")
                .tracker(128, 1, true)
                .build();

        HILL_GIANT = EntityEntryBuilder.create()
                .entity(EntityHillGiant.class)
                .id(new ResourceLocation(Tags.MOD_ID, "hill_giant"), 12)
                .name("hill_giant")
                .tracker(128, 1, true)
                .egg(0xB7925C, 0x4B3320)
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
                VANAHEIM_SPARK,
                CLOUD_GIANT_PRIEST,
                CLOUD_GIANT_CRYSTAL,
                CLOUD_GIANT_SENTINEL,
                CLOUD_GIANT_CROSSBOWMAN,
                CLOUD_GIANT_BOLT,
                HILL_GIANT
        );
    }
}
