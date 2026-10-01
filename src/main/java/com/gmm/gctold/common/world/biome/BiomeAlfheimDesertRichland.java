package com.gmm.gctold.common.world.biome;

import com.gmm.gctold.common.blocks.BlockAlfDirt;
import com.gmm.gctold.common.blocks.BlockAlfGrass;
import com.gmm.gctold.common.blocks.BlockDreamwoodLeaves;
import com.gmm.gctold.common.blocks.BlockDreamwoodLog;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeAlfheimDesertRichland extends Biome {
  public static final BiomeAlfheimDesertRichland biome = new BiomeAlfheimDesertRichland();

  public BiomeAlfheimDesertRichland() {
    super((new Biome.BiomeProperties("Alfheim Desert Richland")).setRainfall(0.0F).setBaseHeight(1.0F)
        .setWaterColor(-256).setHeightVariation(0.1F).setTemperature(2.0F));
    setRegistryName("alfheim_desert_richland");
    this.topBlock = BlockAlfGrass.block.getDefaultState();
    this.fillerBlock = BlockAlfDirt.block.getDefaultState();
    this.decorator.treesPerChunk = 1;
    this.decorator.flowersPerChunk = 4;
    this.decorator.grassPerChunk = 6;
    this.decorator.mushroomsPerChunk = 0;
    this.decorator.bigMushroomsPerChunk = 0;
    this.decorator.reedsPerChunk = 0;
    this.decorator.cactiPerChunk = 0;
    this.decorator.sandPatchesPerChunk = 0;
    this.decorator.gravelPatchesPerChunk = 0;
    this.spawnableMonsterList.clear();
    this.spawnableCreatureList.clear();
    this.spawnableWaterCreatureList.clear();
    this.spawnableCaveCreatureList.clear();
  }

  @Override
  @SideOnly(Side.CLIENT)
  public int getGrassColorAtPos(BlockPos pos) {
    return -256;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public int getFoliageColorAtPos(BlockPos pos) {
    return -256;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public int getSkyColorByTemp(float currentTemperature) {
    return -6684928;
  }

  @Override
  public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
    return new WorldGenAlfheimTree(7, BlockDreamwoodLog.block.getDefaultState(),
        BlockDreamwoodLeaves.block.getDefaultState(), BlockAlfGrass.block, BlockAlfDirt.block);
  }
}
