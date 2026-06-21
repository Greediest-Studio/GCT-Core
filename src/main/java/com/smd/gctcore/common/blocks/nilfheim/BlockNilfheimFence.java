package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.BlockFence;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockNilfheimFence extends BlockFence {
    public BlockNilfheimFence(String name) {
        super(Material.WOOD, MapColor.GRAY);
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(2.0F);
        setResistance(5.0F);
        setSoundType(SoundType.WOOD);
        setHarvestLevel("axe", 0);
    }
}
