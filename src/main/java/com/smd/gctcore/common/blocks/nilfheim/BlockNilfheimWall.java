package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class BlockNilfheimWall extends BlockWall {
    public BlockNilfheimWall(String name, Block modelBlock) {
        super(modelBlock);
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHarvestLevel("pickaxe", 13);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }

    @Override
    public int damageDropped(net.minecraft.block.state.IBlockState state) {
        return 0;
    }
}
