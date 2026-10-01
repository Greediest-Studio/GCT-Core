package com.gmm.gctold.common.world.biome;

import java.util.Random;
import com.gmm.gctold.common.blocks.BlockLunarGrass;
import com.gmm.gctold.common.blocks.BlockLunarSoil;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BiomeCapricornsCliffs extends Biome {
  public static final BiomeCapricornsCliffs biome = new BiomeCapricornsCliffs();
  public BiomeCapricornsCliffs() {
    super((new Biome.BiomeProperties("Capricorns Cliffs")).setRainfall(0.5F).setBaseHeight(0.8F).setHeightVariation(2.0F).setTemperature(0.5F));
    setRegistryName("capricorns_cliffs");
    this.topBlock = BlockLunarGrass.block.getDefaultState();
    this.fillerBlock = BlockLunarSoil.block.getDefaultState();
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
