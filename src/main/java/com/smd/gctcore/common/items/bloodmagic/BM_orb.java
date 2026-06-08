package com.smd.gctcore.common.items.bloodmagic;

import WayofTime.bloodmagic.orb.BloodOrb;
import com.smd.gctcore.Tags;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class BM_orb {

    public static BloodOrb ELDRITCH = new BloodOrb("eldritch", 7, 80000000, 2800);
    public static BloodOrb MURDER = new BloodOrb("murder", 8, 200000000, 6400);
    public static BloodOrb ORIGIN = new BloodOrb("origin", 9, 400000000, 10000);
    public static BloodOrb TRUTH = new BloodOrb("truth", 10, 800000000, 12800);
    public static BloodOrb ENDLESS = new BloodOrb("endless", 11, 1600000000, 25600);
    public static BloodOrb THE_END = new BloodOrb("the_end", 12, Integer.MAX_VALUE, 51200);

    @SubscribeEvent
    public static void registerBloodOrbs(RegistryEvent.Register<BloodOrb> event) {

        IForgeRegistry<BloodOrb> registry = event.getRegistry();

        registry.register(ELDRITCH.withModel(new ModelResourceLocation(new ResourceLocation(
                        Tags.MOD_ID, "bloodmagic/eldritch_orb"), "inventory"))
                .setRegistryName("eldritch"));

        registry.register(MURDER.withModel(new ModelResourceLocation(new ResourceLocation(
                        Tags.MOD_ID, "bloodmagic/murder_orb"), "inventory"))
                .setRegistryName("murder"));

        registry.register(ORIGIN.withModel(new ModelResourceLocation(new ResourceLocation(
                        Tags.MOD_ID, "bloodmagic/origin_orb"), "inventory"))
                .setRegistryName("origin"));

        registry.register(TRUTH.withModel(new ModelResourceLocation(new ResourceLocation(
                        Tags.MOD_ID, "bloodmagic/truth_orb"), "inventory"))
                .setRegistryName("truth"));

        registry.register(ENDLESS.withModel(new ModelResourceLocation(new ResourceLocation(
                        Tags.MOD_ID, "bloodmagic/endless_orb"), "inventory"))
                .setRegistryName("endless"));

        registry.register(THE_END.withModel(new ModelResourceLocation(new ResourceLocation(
                        Tags.MOD_ID, "bloodmagic/the_end_orb"), "inventory"))
                .setRegistryName("the_end"));
    }
}
