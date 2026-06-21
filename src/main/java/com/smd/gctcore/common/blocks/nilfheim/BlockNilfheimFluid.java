package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockNilfheimFluid extends BlockFluidClassic {
    public BlockNilfheimFluid(Fluid fluid, String name) {
        super(fluid, Material.WATER);
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setQuantaPerBlock(4);
        setTickRate(6);
        setLightOpacity(3);
    }
}
