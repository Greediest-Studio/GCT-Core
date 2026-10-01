package com.gmm.gctold.common.world.biome;

import java.util.Random;
import com.gmm.gctold.common.blocks.BlockStarGrass;
import com.gmm.gctold.common.blocks.BlockStarSoil;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BiomeTaurusPlataeu extends Biome {
  public static final BiomeTaurusPlataeu biome = new BiomeTaurusPlataeu();
  public BiomeTaurusPlataeu() {
    super((new Biome.BiomeProperties("Taurus Plataeu")).setRainfall(0.5F).setBaseHeight(1.2F).setHeightVariation(0.1F).setTemperature(0.5F));
    setRegistryName("taurus_plataeu");
    this.topBlock = BlockStarGrass.block.getDefaultState();
    this.fillerBlock = BlockStarSoil.block.getDefaultState();
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
