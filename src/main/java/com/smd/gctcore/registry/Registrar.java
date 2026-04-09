package com.smd.gctcore.registry;

import com.smd.gctcore.Tags;
import com.smd.gctcore.entity.EntityReversedAlfMaster;
import com.smd.gctcore.init.GctItems;
import WayofTime.bloodmagic.client.IMeshProvider;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.google.common.collect.Sets;

import java.util.Set;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class Registrar {

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().register(
                EntityEntryBuilder.create()
                        .entity(EntityReversedAlfMaster.class)
                        .id(new ResourceLocation(Tags.MOD_ID, "reversed_alf_master"), 1)
                        .name("reversed_alf_master")
                        .tracker(64, 3, true)
                        .egg(-11534229, -5273345)
                        .build()
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(GctItems.CHAOTIC_FLUX_CAPACITOR);
        event.getRegistry().register(GctItems.ORDERED_FLUX_CAPACITOR);
        event.getRegistry().register(GctItems.FROSTBURN_FLUX_CAPACITOR);
        event.getRegistry().register(GctItems.ITEM_SOUL_GEM);
        event.getRegistry().register(GctItems.CRIMSON_ANCHOR);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelRegistry(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(GctItems.CHAOTIC_FLUX_CAPACITOR, 0,
                new ModelResourceLocation(GctItems.CHAOTIC_FLUX_CAPACITOR.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(GctItems.ORDERED_FLUX_CAPACITOR, 0,
                new ModelResourceLocation(GctItems.ORDERED_FLUX_CAPACITOR.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(GctItems.FROSTBURN_FLUX_CAPACITOR, 0,
                new ModelResourceLocation(GctItems.FROSTBURN_FLUX_CAPACITOR.getRegistryName(), "inventory"));
        
        // 注册 Soul Gem (使用 IMeshProvider 接口)
        if (GctItems.ITEM_SOUL_GEM instanceof IMeshProvider) {
            IMeshProvider mesh = (IMeshProvider) GctItems.ITEM_SOUL_GEM;
            ResourceLocation loc = mesh.getCustomLocation();
            if (loc == null) {
                loc = GctItems.ITEM_SOUL_GEM.getRegistryName();
            }

            Set<String> variants = Sets.newHashSet();
            mesh.gatherVariants(variants::add);
            for (String variant : variants) {
                ModelLoader.registerItemVariants(GctItems.ITEM_SOUL_GEM, new ModelResourceLocation(loc, variant));
            }

            ModelLoader.setCustomMeshDefinition(GctItems.ITEM_SOUL_GEM, mesh.getMeshDefinition());
        
            // Crimson Anchor
            ModelLoader.setCustomModelResourceLocation(GctItems.CRIMSON_ANCHOR, 0,
                new ModelResourceLocation(GctItems.CRIMSON_ANCHOR.getRegistryName(), "inventory"));
        }
    }
}
