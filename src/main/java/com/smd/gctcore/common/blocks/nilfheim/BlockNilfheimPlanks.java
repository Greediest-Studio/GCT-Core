package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockNilfheimPlanks extends Block {
    public BlockNilfheimPlanks(String name) {
        super(Material.WOOD);
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(2.0F);
        setResistance(5.0F);
        setSoundType(SoundType.WOOD);
        setHarvestLevel("axe", 0);
    }
}
