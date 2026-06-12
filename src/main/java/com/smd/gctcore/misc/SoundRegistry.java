package com.smd.gctcore.misc;

import com.smd.gctcore.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SoundRegistry {

    public static final SoundEvent BIRD = new SoundEvent(new ResourceLocation(Tags.MOD_ID, "bird"))
            .setRegistryName(Tags.MOD_ID, "bird");

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(BIRD);
    }
}
