package com.gmm.gctold.common.blocks;

import net.minecraft.creativetab.CreativeTabs;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

  public class BlockPrimordialStonebrickWall extends BlockWall {
  public static final Block block = new BlockPrimordialStonebrickWall();

  public BlockPrimordialStonebrickWall() {
    super(new Block(Material.ROCK));
    setTranslationKey("primordial_stonebrick_wall");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 4);
    setHardness(2.0F);
    setResistance(10.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
    items.add(new ItemStack((Block)this));
  }
}
