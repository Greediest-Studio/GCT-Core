package com.smd.gctcore.common.misc;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityReversedAlfMaster;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

public class EntityRegistrar {

    public static EntityEntry REVERSED_ALF_MASTER;

    public static void init() {
        REVERSED_ALF_MASTER = EntityEntryBuilder.create()
                .entity(EntityReversedAlfMaster.class)
                .id(new ResourceLocation(Tags.MOD_ID, "reversed_alf_master"), 1)
                .name("reversed_alf_master")
                .tracker(64, 3, true)
                .egg(-11534229, -5273345)
                .build();
    }

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().registerAll(
                REVERSED_ALF_MASTER
        );
    }
}
