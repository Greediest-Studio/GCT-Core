package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;

public class BlockNilfheimStairs extends BlockStairs {
    public BlockNilfheimStairs(String name, IBlockState modelState, SoundType soundType, String tool, int harvestLevel) {
        super(modelState);
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(modelState.getBlock().getBlockHardness(modelState, null, null));
        setResistance(modelState.getBlock().getExplosionResistance(null));
        setSoundType(soundType);
        setHarvestLevel(tool, harvestLevel);
        this.useNeighborBrightness = true;
    }
}
