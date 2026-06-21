package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;

public class BlockNilfheimStone extends Block {
    public BlockNilfheimStone(String name, float hardness, float resistance) {
        super(Material.ROCK);
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(hardness);
        setResistance(resistance);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 13);
    }
}
