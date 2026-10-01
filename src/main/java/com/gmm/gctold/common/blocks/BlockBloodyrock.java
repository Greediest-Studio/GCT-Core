package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockBloodyrock extends Block {
  public static final Block block = new BlockBloodyrock();

  public BlockBloodyrock() {
    super(Material.ROCK);
    setTranslationKey("bloodyrock");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 6);
    setHardness(5.0F);
    setResistance(20.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
