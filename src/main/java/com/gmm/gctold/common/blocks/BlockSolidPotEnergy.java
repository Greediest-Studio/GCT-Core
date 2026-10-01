package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

  public class BlockSolidPotEnergy extends Block {
  public static final Block block = new BlockSolidPotEnergy();

  public BlockSolidPotEnergy() {
    super(Material.IRON);
    setTranslationKey("solid_pot_energy");
    setSoundType(SoundType.METAL);
    setHarvestLevel("pickaxe", 1);
    setHardness(2.0F);
    setResistance(10.0F);
    setLightLevel(1.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
