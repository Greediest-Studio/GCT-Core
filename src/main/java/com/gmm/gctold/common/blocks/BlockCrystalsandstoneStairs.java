package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

  public class BlockCrystalsandstoneStairs extends BlockStairs {
  public static final Block block = new BlockCrystalsandstoneStairs();

  public BlockCrystalsandstoneStairs() {
    super((new Block(Material.ROCK)).getDefaultState());
    setTranslationKey("crystalsandstone_stairs");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 0);
    setHardness(5.0F);
    setResistance(10.0F);
    setLightLevel(0.6666667F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
  }

  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }
}
