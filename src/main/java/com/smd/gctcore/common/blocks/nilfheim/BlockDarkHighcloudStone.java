package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.creativetab.CreativeTabs;

public class BlockDarkHighcloudStone extends BlockHighcloudStone {
    public BlockDarkHighcloudStone(String name) {
        super(name, true, null);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHarvestLevel("pickaxe", 0);
    }
}
