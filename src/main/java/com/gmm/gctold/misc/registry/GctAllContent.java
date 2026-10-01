package com.gmm.gctold.misc.registry;

import com.gmm.gctold.common.blocks.GctAllBlocks;
import com.gmm.gctold.common.blocks.MachineBlock;
import com.gmm.gctold.client.GctAllModels;
import com.gmm.gctold.misc.GctAllCreativeTab;
import com.gmm.gctold.common.world.dimension.GctAllDimensions;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class GctAllContent {
    private static final Block ATOMIC_VIBRATOR = machine("model_atomic_viberator",
            "§7将中子注入矿物。");
    private static final Block ATOMIC_DECAYER = machine("model_atomic_decayer",
            "§7加速放射物的衰变。");
    private static final Block ATOMIC_ACIDOR = machine("model_atomic_acidor",
            "§7使用氟王水溶解金属。");
    private static final Block ENDER_FORGE = new MachineBlock("model_ender_forge", SoundType.GROUND, 1.0F, 10.0F,
            GctAllCreativeTab.TAB, null);

    private GctAllContent() {
    }

    public static void preInit(FMLPreInitializationEvent event) {
        GctAllBlocks.preInit(event);
    }

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        GctAllBlocks.registerBlocks(event);
        GctAllDimensions.registerBlocks(event);
        event.getRegistry().registerAll(
                ATOMIC_VIBRATOR,
                ATOMIC_DECAYER,
                ATOMIC_ACIDOR,
                ENDER_FORGE);
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        GctAllBlocks.registerItems(event);
        GctAllItems.registerItems(event);
        GctAllDimensions.registerItems(event);
        event.getRegistry().registerAll(
                itemBlock(ATOMIC_VIBRATOR),
                itemBlock(ATOMIC_DECAYER),
                itemBlock(ATOMIC_ACIDOR),
                itemBlock(ENDER_FORGE));
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        GctAllBlocks.registerModels(event);
        GctAllItems.registerModels();
        GctAllDimensions.registerModels(event);

        GctAllModels.block(ATOMIC_VIBRATOR);
        GctAllModels.block(ATOMIC_DECAYER);
        GctAllModels.block(ATOMIC_ACIDOR);
        GctAllModels.block(ENDER_FORGE);
    }

    private static ItemBlock itemBlock(Block block) {
        return (ItemBlock) new ItemBlock(block).setRegistryName(block.getRegistryName());
    }

    private static Block machine(String name, String tooltip) {
        Block block = new MachineBlock(name, SoundType.STONE, 5.0F, 10.0F, GctAllCreativeTab.TAB, tooltip);
        block.setHarvestLevel("pickaxe", 1);
        return block;
    }
}
