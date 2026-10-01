package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockCrystalsand extends BlockFalling {
  public static final Block block = new BlockCrystalsand();

  public BlockCrystalsand() {
    super(Material.SAND);
    setTranslationKey("crystalsand");
    setSoundType(SoundType.SAND);
    setHarvestLevel("shovel", 0);
    setHardness(1.8F);
    setResistance(10.0F);
    setLightLevel(0.6666667F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
