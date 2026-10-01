package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

  public class BlockBotanicalStone extends Block {
  public static final Block block = new BlockBotanicalStone();

  public BlockBotanicalStone() {
    super(Material.ROCK);
    setTranslationKey("botanical_stone");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 12);
    setHardness(200.0F);
    setResistance(2000000.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
    super.addInformation(itemstack, world, list, flag);
    list.add("§e蕴含着自然的庞大能量。");
  }

  public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    drops.add(new ItemStack(BlockAlfStone.block, 1));
  }
}
