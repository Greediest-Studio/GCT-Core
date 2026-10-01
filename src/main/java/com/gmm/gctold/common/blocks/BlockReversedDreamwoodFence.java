package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

  public class BlockReversedDreamwoodFence extends BlockFence {
  public static final Block block = new BlockReversedDreamwoodFence();

  public BlockReversedDreamwoodFence() {
    super(Material.ROCK, Material.ROCK.getMaterialMapColor());
    setTranslationKey("reversed_dreamwood_fence");
    setSoundType(SoundType.WOOD);
    setHarvestLevel("axe", 1);
    setHardness(2.0F);
    setResistance(2.0F);
    setLightLevel(0.0F);
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
