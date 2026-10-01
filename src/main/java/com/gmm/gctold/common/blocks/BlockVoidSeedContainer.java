package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

  public class BlockVoidSeedContainer extends Block {
  public static final Block block = new BlockVoidSeedContainer();

  public BlockVoidSeedContainer() {
    super(Material.ROCK);
    setTranslationKey("void_seed_container");
    setSoundType(SoundType.STONE);
    setHardness(1.0F);
    setResistance(10.0F);
    setLightLevel(0.0F);
    setLightOpacity(0);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
  }

  public boolean isFullCube(IBlockState state) {
    return false;
  }

  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
    return (new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)).union(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D));
  }

  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }
}
