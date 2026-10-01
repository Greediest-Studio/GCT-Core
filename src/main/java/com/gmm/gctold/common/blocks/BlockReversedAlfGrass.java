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

  public class BlockReversedAlfGrass extends Block {
  public static final Block block = new BlockReversedAlfGrass();

  public BlockReversedAlfGrass() {
    super(Material.GRASS);
    setTranslationKey("reversed_alf_grass");
    setSoundType(SoundType.GROUND);
    setHarvestLevel("shovel", 1);
    setHardness(0.5F);
    setResistance(0.5F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    drops.add(new ItemStack(BlockReversedAlfDirt.block, 1));
  }
}
