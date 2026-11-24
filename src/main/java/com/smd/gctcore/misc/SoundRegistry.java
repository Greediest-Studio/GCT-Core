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
    public static final SoundEvent HILL_GIANT_AMBIENT = sound("hill_giant_ambient");
    public static final SoundEvent HILL_GIANT_HURT = sound("hill_giant_hurt");
    public static final SoundEvent HILL_GIANT_DEATH = sound("hill_giant_death");
    public static final SoundEvent STONE_GIANT_AMBIENT = sound("stone_giant_ambient");
    public static final SoundEvent STONE_GIANT_HURT = sound("stone_giant_hurt");
    public static final SoundEvent STONE_GIANT_DEATH = sound("stone_giant_death");
    public static final SoundEvent FROST_GIANT_AMBIENT = sound("frost_giant_ambient");
    public static final SoundEvent FROST_GIANT_HURT = sound("frost_giant_hurt");
    public static final SoundEvent FROST_GIANT_DEATH = sound("frost_giant_death");
    public static final SoundEvent FIRE_GIANT_AMBIENT = sound("fire_giant_ambient");
    public static final SoundEvent FIRE_GIANT_HURT = sound("fire_giant_hurt");
    public static final SoundEvent FIRE_GIANT_DEATH = sound("fire_giant_death");
    public static final SoundEvent FOMORIAN_NOBLE_AMBIENT = sound("fomorian_noble_ambient");
    public static final SoundEvent FOMORIAN_NOBLE_HURT = sound("fomorian_noble_hurt");
    public static final SoundEvent FOMORIAN_NOBLE_DEATH = sound("fomorian_noble_death");
    public static final SoundEvent FOMORIAN_WARLOCK_AMBIENT = sound("fomorian_warlock_ambient");
    public static final SoundEvent FOMORIAN_WARLOCK_HURT = sound("fomorian_warlock_hurt");
    public static final SoundEvent FOMORIAN_WARLOCK_DEATH = sound("fomorian_warlock_death");

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
                CLOUD_GIANT_CRYSTAL_ACTIVATED,
                HILL_GIANT_AMBIENT,
                HILL_GIANT_HURT,
                HILL_GIANT_DEATH,
                STONE_GIANT_AMBIENT,
                STONE_GIANT_HURT,
                STONE_GIANT_DEATH,
                FROST_GIANT_AMBIENT,
                FROST_GIANT_HURT,
                FROST_GIANT_DEATH,
                FIRE_GIANT_AMBIENT,
                FIRE_GIANT_HURT,
                FIRE_GIANT_DEATH,
                FOMORIAN_NOBLE_AMBIENT,
                FOMORIAN_NOBLE_HURT,
                FOMORIAN_NOBLE_DEATH,
                FOMORIAN_WARLOCK_AMBIENT,
                FOMORIAN_WARLOCK_HURT,
                FOMORIAN_WARLOCK_DEATH
        );
    }
}
