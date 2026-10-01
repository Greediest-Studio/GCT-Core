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

public class BlockOrderLiquid extends BlockFluidClassic {
    private static final Fluid FLUID = registerFluid();

    public static final Block block = new BlockOrderLiquid();
    public static final Item item = new ItemBlock(block);

    public BlockOrderLiquid() {
        super(FLUID, Material.WATER);
        setTranslationKey("order_liquid");
    }

    public static void preInit(FMLPreInitializationEvent event) {
        FluidRegistry.addBucketForFluid(FLUID);
    }

    private static Fluid registerFluid() {
        Fluid fluid = new Fluid("order_liquid",
                new ResourceLocation("gctold:blocks/order_liquid"),
                new ResourceLocation("gctold:blocks/order_liquid"))
                .setLuminosity(0)
                .setDensity(1000)
                .setViscosity(1000)
                .setGaseous(false);
        return FluidRegistry.registerFluid(fluid) ? fluid : FluidRegistry.getFluid(fluid.getName());
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        GctAllFluidModels.register(block, item, "order_liquid");
    }
}
