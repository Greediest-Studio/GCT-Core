package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockManaPlank extends Block {
  public static final Block block = new BlockManaPlank();

  public BlockManaPlank() {
    super(Material.WOOD);
    setTranslationKey("mana_plank");
    setSoundType(SoundType.WOOD);
    setHarvestLevel("axe", 0);
    setHardness(2.0F);
    setResistance(2.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
