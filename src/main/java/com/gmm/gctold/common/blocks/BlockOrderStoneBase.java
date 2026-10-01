package com.gmm.gctold.common.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import com.gmm.gctold.misc.GctAllCreativeTab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
public class BlockOrderStoneBase extends Block {
    public static final Block block = new BlockOrderStoneBase();

    public BlockOrderStoneBase() {
    super(Material.ROCK);
    this.setTranslationKey("order_stone_base");
    this.setSoundType(SoundType.STONE);
    this.setHarvestLevel("pickaxe", 25);
    this.setHardness(50.0f);
    this.setResistance(4000.0f);
    this.setLightLevel(0.0f);
    this.setLightOpacity(15);
    this.setCreativeTab(GctAllCreativeTab.TAB);

    }



    public void getDrops(final NonNullList<ItemStack> drops, final IBlockAccess world, final BlockPos pos, final IBlockState state, final int fortune) {
    drops.add(new ItemStack(BlockOrderCobblestone.block, 1));
    }

}