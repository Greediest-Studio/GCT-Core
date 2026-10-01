package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import com.google.common.base.Predicate;
import java.util.Random;
import com.gmm.gctold.common.potions.PotionAbyssPlague;
import com.gmm.gctold.common.world.biome.BiomeWarped;
import com.gmm.gctold.common.world.dimension.WorldWarpedRuin;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;

  public class BlockAzathothiumOreComplex extends Block {
  public static final Block block = new BlockAzathothiumOreComplex();public void generateWorld(Random random, int chunkX, int chunkZ, World world, int dimID, IChunkGenerator cg, IChunkProvider cp) {
    boolean dimensionCriteria = false;
    if (dimID == WorldWarpedRuin.DIMID)
      dimensionCriteria = true;
    if (!dimensionCriteria)
      return;
    boolean biomeCriteria = false;
    Biome biome = world.getBiome(new BlockPos(chunkX, 128, chunkZ));
    if (biome == BiomeWarped.biome)
      biomeCriteria = true;
    if (!biomeCriteria)
      return;
    for (int i = 0; i < 3; i++) {
      int x = chunkX + random.nextInt(16);
      int y = random.nextInt(60) + 0;
      int z = chunkZ + random.nextInt(16);
      (new WorldGenMinable(block.getDefaultState(), 2, new Predicate<IBlockState>() {
            public boolean apply(IBlockState blockAt) {
              boolean blockCriteria = false;
              if (blockAt.getBlock() == BlockWarprack.block.getDefaultState().getBlock())
                blockCriteria = true;
              return blockCriteria;
            }
          })).generate(world, random, new BlockPos(x, y, z));
    }
  }

  public BlockAzathothiumOreComplex() {
    super(Material.ROCK);
    setTranslationKey("azathothium_ore_complex");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 11);
    setHardness(60.0F);
    setResistance(4000.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer entity, boolean willHarvest) {
    boolean retval = super.removedByPlayer(state, world, pos, entity, willHarvest);
    if (!world.isRemote && world.rand.nextDouble() < 0.6D && entity instanceof EntityLivingBase) {
      ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(PotionAbyssPlague.potion, 100, 0, false, false));
    }
    return retval;
  }
}
