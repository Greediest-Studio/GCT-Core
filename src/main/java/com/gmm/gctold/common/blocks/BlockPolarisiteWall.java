package com.gmm.gctold.common.blocks;

import net.minecraft.creativetab.CreativeTabs;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

  public class BlockPolarisiteWall extends BlockWall {
  public static final Block block = new BlockPolarisiteWall();

  public BlockPolarisiteWall() {
    super(new Block(Material.ROCK));
    setTranslationKey("polarisite_wall");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 8);
    setHardness(15.0F);
    setResistance(4000.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
    items.add(new ItemStack((Block)this));
  }

  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
  }

  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }
}
