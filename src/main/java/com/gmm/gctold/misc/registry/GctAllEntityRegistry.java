package com.gmm.gctold.misc.registry;

import com.smd.gctcore.Tags;
import com.gmm.gctold.common.entity.EntityAncientShoggoth;
import com.gmm.gctold.common.entity.EntityApocalypseCube;
import com.gmm.gctold.common.entity.EntityApocalypseHolder;
import com.gmm.gctold.common.entity.EntityApocalypseKnight;
import com.gmm.gctold.common.entity.EntityBligtz;
import com.gmm.gctold.common.entity.EntityBloodyShoggoth;
import com.gmm.gctold.common.entity.EntityBlueFlameBeholder;
import com.gmm.gctold.common.entity.EntityBnatuz;
import com.gmm.gctold.common.entity.EntityBninz;
import com.gmm.gctold.common.entity.EntityBthdz;
import com.gmm.gctold.common.entity.EntityDarkerLesserShoggoth;
import com.gmm.gctold.common.entity.EntityElf;
import com.gmm.gctold.common.entity.EntityMixtureShoggoth;
import com.gmm.gctold.common.entity.EntityRemnantWandering;
import com.gmm.gctold.common.entity.EntityReversedElf;
import com.gmm.gctold.common.entity.EntityRottened;
import com.gmm.gctold.common.entity.EntityShadowBase;
import com.gmm.gctold.common.entity.EntityWeatherEyevil;
import com.gmm.gctold.common.entity.EntityWeatherWaterRod;
import com.gmm.gctold.common.entity.EntityZethur;
import com.gmm.gctold.common.entity.EntityZjarugoth;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

public final class GctAllEntityRegistry {
    private GctAllEntityRegistry() {
    }

    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        register(event, EntityDarkerLesserShoggoth.DarkerLesserShoggothEntity.class,
                "darkerlessershoggoth", 1, 128, 3, true, -16777216, -1);
        registerProjectile(event, EntityDarkerLesserShoggoth.DarkerLesserShoggothProjectileEntity.class,
                "entitybulletdarkerlessershoggoth", 2, 64, 1, true);
        register(event, EntityAncientShoggoth.AncientShoggothEntity.class,
                "ancientshoggoth", 3, 256, 3, true, -16777216, -16777216);
        register(event, EntityShadowBase.ShadowBaseEntity.class,
                "shadow_base", 5, 64, 3, true, -16777216, -6750208);
        register(event, EntityRemnantWandering.RemnantWanderingEntity.class,
                "remnant_wandering", 7, 256, 3, true, -13421773, -1);

        register(event, EntityApocalypseCube.ApocalypseCubeEntity.class,
                "apocalypse_cube", 9, 64, 3, true, -6750208, -3407872);
        register(event, EntityBlueFlameBeholder.BlueFlameBeholderEntity.class,
                "blue_flame_beholder", 9, 64, 3, true, -16777216, -16750951);
        register(event, EntityBligtz.BligtzEntity.class,
                "bligtz", 11, 64, 3, true, -3355648, -205);
        registerProjectile(event, EntityBligtz.BligtzProjectileEntity.class,
                "entitybulletbligtz", 12, 64, 1, true);
        register(event, EntityZjarugoth.ZjarugothEntity.class,
                "zjarugoth", 11, 512, 3, true, -16777216, -16711681);

        register(event, EntityBninz.BninzEntity.class,
                "bninz", 13, 64, 3, true, -16777216, -10066330);
        registerProjectile(event, EntityBninz.BninzProjectileEntity.class,
                "entitybulletbninz", 14, 64, 1, true);
        register(event, EntityMixtureShoggoth.MixtureShoggothEntity.class,
                "mixture_shoggoth", 13, 64, 3, true, -16777216, -16711681);
        register(event, EntityBthdz.BthdzEntity.class,
                "bthdz", 15, 64, 3, true, -16776961, -13395457);
        registerProjectile(event, EntityBthdz.BthdzProjectileEntity.class,
                "entitybulletbthdz", 16, 64, 1, true);
        register(event, EntityBloodyShoggoth.BloodyShoggothEntity.class,
                "bloody_shoggoth", 15, 64, 3, true, -16777216, -16711681);
        register(event, EntityBnatuz.BnatuzEntity.class,
                "bnatuz", 17, 64, 3, true, -16738048, -16751002);
        registerProjectile(event, EntityBnatuz.BnatuzProjectileEntity.class,
                "entitybulletbnatuz", 18, 64, 1, true);

        register(event, EntityApocalypseKnight.ApocalypseKnightEntity.class,
                "apocalypse_knight", 23, 128, 3, true, -16777216, -3407770);
        register(event, EntityRottened.RottenedEntity.class,
                "rottened", 25, 64, 3, true, -6750004, -13434727);
        register(event, EntityZethur.ZethurEntity.class,
                "zethur", 29, 256, 3, true, -16777216, -10066330);
        register(event, EntityWeatherEyevil.WeatherEyevilEntity.class,
                "weather_eyevil", 31, 256, 3, true, -16763905, -16711681);
        register(event, EntityWeatherWaterRod.WeatherWaterRodEntity.class,
                "weather_water_rod", 35, 64, 3, true, -16776961, -13369345);
        register(event, EntityApocalypseHolder.ApocalypseHolderEntity.class,
                "apocalypse_holder", 37, 64, 3, true, -1, -1);
        register(event, EntityElf.ElfEntity.class,
                "elf", 39, 64, 3, true, -1, -1);
        register(event, EntityReversedElf.ReversedElfEntity.class,
                "reversed_elf", 41, 64, 3, true, -11534229, -5273345);
    }

    private static void register(RegistryEvent.Register<EntityEntry> event, Class<? extends Entity> entityClass,
            String name, int id, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates,
            int eggPrimary, int eggSecondary) {
        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(entityClass)
                .id(id(name), id)
                .name(name)
                .tracker(trackingRange, updateFrequency, sendsVelocityUpdates)
                .egg(eggPrimary, eggSecondary)
                .build());
    }

    private static void registerProjectile(RegistryEvent.Register<EntityEntry> event, Class<? extends Entity> entityClass,
            String name, int id, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(entityClass)
                .id(id(name), id)
                .name(name)
                .tracker(trackingRange, updateFrequency, sendsVelocityUpdates)
                .build());
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Tags.MOD_ID, name);
    }
}
