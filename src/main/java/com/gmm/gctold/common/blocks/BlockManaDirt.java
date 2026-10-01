package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IPlantable;

  public class BlockManaDirt extends Block {
  public static final Block block = new BlockManaDirt();

  public BlockManaDirt() {
    super(Material.GROUND);
    setTranslationKey("mana_dirt");
    setSoundType(SoundType.GROUND);
    setHarvestLevel("shovel", 1);
    setHardness(0.5F);
    setResistance(0.5F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
    return true;
  }
}
