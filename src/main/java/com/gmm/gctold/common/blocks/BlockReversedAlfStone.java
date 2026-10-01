package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

  public class BlockReversedAlfStone extends Block {
  public static final Block block = new BlockReversedAlfStone();

  public BlockReversedAlfStone() {
    super(Material.ROCK);
    setTranslationKey("reversed_alf_stone");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 0);
    setHardness(1.5F);
    setResistance(6.0F);
    setLightLevel(0.33333334F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    drops.add(new ItemStack(BlockReversedAlfCobbleStone.block, 1));
  }
}
