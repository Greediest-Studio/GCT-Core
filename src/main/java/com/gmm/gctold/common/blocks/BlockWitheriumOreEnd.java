package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.registry.GctAllItems;

import com.gmm.gctold.misc.GctAllCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockWitheriumOreEnd extends Block {
    public static final Block block = new BlockWitheriumOreEnd();

    public BlockWitheriumOreEnd() {
        super(Material.ROCK);
        setTranslationKey("witherium_ore_end");
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 8);
        setHardness(5.0F);
        setResistance(20.0F);
        setLightLevel(0.06666667F);
        setLightOpacity(15);
        setCreativeTab(GctAllCreativeTab.TAB);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.add(new ItemStack(GctAllItems.WITHERIUM_DUST, 3));
    }
}
