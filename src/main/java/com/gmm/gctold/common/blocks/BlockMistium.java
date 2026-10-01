package com.gmm.gctold.common.blocks;

import com.gmm.gctold.client.GctAllFluidModels;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockMistium extends BlockFluidClassic {
    private static final Fluid FLUID = registerFluid();

    public static final Block block = new BlockMistium();
    public static final Item item = new ItemBlock(block);

    public BlockMistium() {
        super(FLUID, Material.LAVA);
        setTranslationKey("mistium");
    }

    public static void preInit(FMLPreInitializationEvent event) {
        FluidRegistry.addBucketForFluid(FLUID);
    }

    private static Fluid registerFluid() {
        Fluid fluid = new Fluid("mistium",
                new ResourceLocation("gctold:blocks/mistium_still"),
                new ResourceLocation("gctold:blocks/mistium_flow"))
                .setLuminosity(0)
                .setDensity(1000)
                .setViscosity(4000)
                .setGaseous(false);
        return FluidRegistry.registerFluid(fluid) ? fluid : FluidRegistry.getFluid(fluid.getName());
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        GctAllFluidModels.register(block, item, "mistium");
    }
}
