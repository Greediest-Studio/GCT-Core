package com.smd.gctcore.misc;

import com.smd.gctcore.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SoundRegistry {

    public static final SoundEvent BIRD = new SoundEvent(new ResourceLocation(Tags.MOD_ID, "bird"))
            .setRegistryName(Tags.MOD_ID, "bird");
    public static final SoundEvent CLOUD_GIANT_PRIEST_AMBIENT = sound("cloud_giant_priest_ambient");
    public static final SoundEvent CLOUD_GIANT_PRIEST_BLOCKED = sound("cloud_giant_priest_blocked");
    public static final SoundEvent CLOUD_GIANT_PRIEST_LIGHTNING = sound("cloud_giant_priest_lightning");
    public static final SoundEvent CLOUD_GIANT_PRIEST_HURT = sound("cloud_giant_priest_hurt");
    public static final SoundEvent CLOUD_GIANT_PRIEST_WHIRLWIND = sound("cloud_giant_priest_whirlwind");
    public static final SoundEvent CLOUD_GIANT_PRIEST_DEATH = sound("cloud_giant_priest_death");
    public static final SoundEvent CLOUD_GIANT_SENTINEL_HURT = sound("cloud_giant_sentinel_hurt");
    public static final SoundEvent CLOUD_GIANT_SENTINEL_DEATH = sound("cloud_giant_sentinel_death");
    public static final SoundEvent CLOUD_GIANT_SENTINEL_ATTACK = sound("cloud_giant_sentinel_attack");
    public static final SoundEvent CLOUD_GIANT_CROSSBOWMAN_HURT = sound("cloud_giant_crossbowman_hurt");
    public static final SoundEvent CLOUD_GIANT_CROSSBOWMAN_DEATH = sound("cloud_giant_crossbowman_death");
    public static final SoundEvent CLOUD_GIANT_CROSSBOWMAN_RELOAD = sound("cloud_giant_crossbowman_reload");
    public static final SoundEvent CLOUD_GIANT_CROSSBOWMAN_SHOOT = sound("cloud_giant_crossbowman_shoot");
    public static final SoundEvent CLOUD_GIANT_CRYSTAL_HURT = sound("cloud_giant_crystal_hurt");
    public static final SoundEvent CLOUD_GIANT_CRYSTAL_DEPLETED = sound("cloud_giant_crystal_depleted");
    public static final SoundEvent CLOUD_GIANT_CRYSTAL_ACTIVATED = sound("cloud_giant_crystal_activated");

    private static SoundEvent sound(String name) {
        return new SoundEvent(new ResourceLocation(Tags.MOD_ID, name)).setRegistryName(Tags.MOD_ID, name);
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(
                BIRD,
                CLOUD_GIANT_PRIEST_AMBIENT,
                CLOUD_GIANT_PRIEST_BLOCKED,
                CLOUD_GIANT_PRIEST_LIGHTNING,
                CLOUD_GIANT_PRIEST_HURT,
                CLOUD_GIANT_PRIEST_WHIRLWIND,
                CLOUD_GIANT_PRIEST_DEATH,
                CLOUD_GIANT_SENTINEL_HURT,
                CLOUD_GIANT_SENTINEL_DEATH,
                CLOUD_GIANT_SENTINEL_ATTACK,
                CLOUD_GIANT_CROSSBOWMAN_HURT,
                CLOUD_GIANT_CROSSBOWMAN_DEATH,
                CLOUD_GIANT_CROSSBOWMAN_RELOAD,
                CLOUD_GIANT_CROSSBOWMAN_SHOOT,
                CLOUD_GIANT_CRYSTAL_HURT,
                CLOUD_GIANT_CRYSTAL_DEPLETED,
                CLOUD_GIANT_CRYSTAL_ACTIVATED
        );
    }
}
