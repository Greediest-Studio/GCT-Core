package com.smd.gctcore.proxy;

import WayofTime.bloodmagic.client.IMeshProvider;
import com.google.common.collect.Sets;
import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityRenders;
import com.smd.gctcore.common.events.MiningSpeedHandler;
import com.smd.gctcore.common.misc.ItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Objects;
import java.util.Set;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        // 注册事件监听器
        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(new MiningSpeedHandler());
        ClientRegistry.registerKeyBinding(MiningSpeedHandler.KEY_MINING_SPEED_ADD);
        ClientRegistry.registerKeyBinding(MiningSpeedHandler.KEY_MINING_SPEED_MINUS);

        // 注册实体渲染器
        EntityRenders.registerEntityRenders();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MOD_ID)
    public static class ModelRegistration {

        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event) {
            registerItemModel(ItemRegistry.CHAOTIC_FLUX_CAPACITOR);
            registerItemModel(ItemRegistry.ORDERED_FLUX_CAPACITOR);
            registerItemModel(ItemRegistry.FROSTBURN_FLUX_CAPACITOR);
            registerItemModel(ItemRegistry.CRIMSON_ANCHOR);
            registerSoulGemMeshModel();
        }

        private static void registerBlockModel(Block block) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(Objects.requireNonNull(block.getRegistryName()), "inventory"));
        }

        private static void registerItemModel(Item item) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory"));
        }
        
        private static void registerSoulGemMeshModel() {
            Item soulGem = ItemRegistry.ITEM_SOUL_GEM;
            if (!(soulGem instanceof IMeshProvider)) {
                return;
            }
            
            IMeshProvider mesh = (IMeshProvider) soulGem;
            ResourceLocation loc = mesh.getCustomLocation();
            if (loc == null) {
                loc = soulGem.getRegistryName();
            }
            Set<String> variants = Sets.newHashSet();
            mesh.gatherVariants(variants::add);
            for (String variant : variants) {
                ModelLoader.registerItemVariants(soulGem, new ModelResourceLocation(loc, variant));
            }
            ModelLoader.setCustomMeshDefinition(soulGem, mesh.getMeshDefinition());
        }

    }
}
