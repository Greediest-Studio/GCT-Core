package com.gmm.gctold.misc.registry;

import com.smd.gctcore.Tags;
import com.gmm.gctold.common.potions.GctAllPotions;
import com.gmm.gctold.common.world.biome.GctAllBiomes;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class GctAllRegistry {
    private GctAllRegistry() {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        GctAllContent.registerBlocks(event);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        GctAllContent.registerItems(event);
    }

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        GctAllBiomes.registerBiomes(event);
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        GctAllEntityRegistry.registerEntities(event);
    }

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        GctAllPotions.registerPotions(event);
    }

    @SubscribeEvent
    public static void registerPotionTypes(RegistryEvent.Register<PotionType> event) {
        GctAllPotions.registerPotionTypes(event);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        GctAllSounds.registerSounds(event);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        GctAllContent.registerModels(event);
    }
}
