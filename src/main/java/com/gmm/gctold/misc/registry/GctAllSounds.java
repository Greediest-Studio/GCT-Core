package com.gmm.gctold.misc.registry;

import com.smd.gctcore.Tags;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;

public final class GctAllSounds {
    private static final Map<ResourceLocation, SoundEvent> SOUNDS = new HashMap<>();

    static {
        registerSound("remnant_ambient");
        registerSound("zjarugoth_roar");
        registerSound("shoggoth_ambient");
        registerSound("chant_abyss");
        registerSound("shoggoth_death");
        registerSound("shoggoth_hurt");
        registerSound("bligtz_br");
        registerSound("bninz_br");
        registerSound("bninz_ht");
        registerSound("bninz_de");
        registerSound("bligtz_ht");
        registerSound("bligtz_de");
        registerSound("bnatuz_br");
        registerSound("bnatuz_ht");
        registerSound("bnatuz_de");
        registerSound("bthdz_br");
        registerSound("bthdz_ht");
        registerSound("bthdz_de");
        registerSound("reversedlands");
        registerSound("deepslate_break");
        registerSound("deepslate_step");
        registerSound("deepslate_place");
    }

    private GctAllSounds() {
    }

    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        SOUNDS.forEach((id, sound) -> event.getRegistry().register(sound.setRegistryName(id)));
    }

    public static Map<ResourceLocation, SoundEvent> getSounds() {
        return Collections.unmodifiableMap(SOUNDS);
    }

    private static void registerSound(String name) {
        ResourceLocation id = new ResourceLocation(Tags.MOD_ID, name);
        SOUNDS.put(id, new SoundEvent(id));
    }
}
