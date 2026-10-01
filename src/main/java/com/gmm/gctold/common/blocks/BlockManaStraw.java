package com.gmm.gctold.common.blocks;

import net.minecraft.creativetab.CreativeTabs;

import com.gmm.gctold.misc.GctAllCreativeTab;

import java.util.Random;
import com.gmm.gctold.common.world.biome.BiomeManaForest;
import com.gmm.gctold.common.world.biome.BiomeManaForestHill;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

  public class BlockManaStraw extends BlockFlower {
  public static final Block block = new BlockManaStraw();public void generateWorld(Random random, int chunkX, int chunkZ, World world, int dimID, IChunkGenerator cg, IChunkProvider cp) {
    boolean dimensionCriteria = false;
    if (dimID == 0)
      dimensionCriteria = true;
    if (!dimensionCriteria)
      return;
    boolean biomeCriteria = false;
    Biome biome = world.getBiome(new BlockPos(chunkX, 128, chunkZ));
    if (biome == BiomeManaForest.biome)
      biomeCriteria = true;
    if (biome == BiomeManaForestHill.biome)
      biomeCriteria = true;
    if (!biomeCriteria)
      return;
    for (int i = 0; i < 20; i++) {
      int l6 = chunkX + random.nextInt(16);
      int i11 = random.nextInt(128);
      int l14 = chunkZ + random.nextInt(16);
      (new WorldGenFlowers((BlockFlower)block, BlockFlower.EnumFlowerType.DANDELION)).generate(world, random, new BlockPos(l6, i11, l14));
    }
  }

  public BlockManaStraw() {
    setSoundType(SoundType.PLANT);
    setCreativeTab(GctAllCreativeTab.TAB);
    setHardness(0.0F);
    setResistance(0.0F);
    setLightLevel(0.0F);
    setTranslationKey("mana_straw");
    setRegistryName("mana_straw");
  }

  public boolean isReplaceable(IBlockAccess blockAccess, BlockPos pos) {
    return true;
  }

  public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
    return 100;
  }

  public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    drops.add(new ItemStack(Blocks.AIR, 1));
  }

  public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
    return new ItemStack(Item.getItemFromBlock((Block)this), 1, damageDropped(state));
  }

  public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
    return EnumPlantType.Plains;
  }

  public BlockFlower.EnumFlowerColor getBlockType() {
    return BlockFlower.EnumFlowerColor.YELLOW;
  }

  @SideOnly(Side.CLIENT)
  public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
    for (BlockFlower.EnumFlowerType blockflower$enumflowertype : BlockFlower.EnumFlowerType.getTypes(getBlockType()))
      list.add(new ItemStack((Block)this, 1, blockflower$enumflowertype.getMeta()));
  }
}
