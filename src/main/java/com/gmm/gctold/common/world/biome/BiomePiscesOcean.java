package com.gmm.gctold.common.world.biome;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BiomePiscesOcean extends Biome {
  public static final BiomePiscesOcean biome = new BiomePiscesOcean();
  public BiomePiscesOcean() {
    super((new Biome.BiomeProperties("Pisces Ocean")).setRainfall(0.5F).setBaseHeight(0.0F).setHeightVariation(0.0F).setTemperature(0.5F));
    setRegistryName("pisces_ocean");
    this.topBlock = Blocks.FLOWING_WATER.getDefaultState();
    this.fillerBlock = Blocks.FLOWING_WATER.getDefaultState();
    this.decorator.treesPerChunk = 0;
    this.decorator.flowersPerChunk = 4;
    this.decorator.grassPerChunk = 8;
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

  public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
    return super.getRandomTreeFeature(rand);
  }
}
