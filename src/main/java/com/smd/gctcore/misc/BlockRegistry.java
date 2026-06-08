package com.smd.gctcore.misc;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.blocks.arcanearchives.RadiantResonatorBlock;
import com.smd.gctcore.common.blocks.arcanearchives.RawQuartzClusterBlock;
import com.smd.gctcore.common.blocks.arcanearchives.StorageRawQuartzBlock;
import com.smd.gctcore.common.blocks.arcanearchives.StorageShapedQuartzBlock;
import com.smd.gctcore.common.tile.RadiantResonatorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockRegistry {
    public static RadiantResonatorBlock RADIANT_RESONATOR;
    public static RawQuartzClusterBlock RAW_QUARTZ_CLUSTER;
    public static StorageRawQuartzBlock STORAGE_RAW_QUARTZ;
    public static StorageShapedQuartzBlock STORAGE_SHAPED_QUARTZ;

    public static void init() {
        RADIANT_RESONATOR = new RadiantResonatorBlock();
        RAW_QUARTZ_CLUSTER = new RawQuartzClusterBlock();
        STORAGE_RAW_QUARTZ = new StorageRawQuartzBlock();
        STORAGE_SHAPED_QUARTZ = new StorageShapedQuartzBlock();
    }

    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(RadiantResonatorTileEntity.class, new ResourceLocation(Tags.MOD_ID, "radiant_resonator"));
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                RADIANT_RESONATOR,
                RAW_QUARTZ_CLUSTER,
                STORAGE_RAW_QUARTZ,
                STORAGE_SHAPED_QUARTZ
        );
    }

    @SubscribeEvent
    public void registerItemBlocks(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                new ItemBlock(RADIANT_RESONATOR).setRegistryName(RADIANT_RESONATOR.getRegistryName()),
                new ItemBlock(RAW_QUARTZ_CLUSTER).setRegistryName(RAW_QUARTZ_CLUSTER.getRegistryName()),
                new ItemBlock(STORAGE_RAW_QUARTZ).setRegistryName(STORAGE_RAW_QUARTZ.getRegistryName()),
                new ItemBlock(STORAGE_SHAPED_QUARTZ).setRegistryName(STORAGE_SHAPED_QUARTZ.getRegistryName())
        );
    }
}
