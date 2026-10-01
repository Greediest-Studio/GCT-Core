package com.gmm.gctold.common.world.biome;

import java.util.Random;
import com.gmm.gctold.common.blocks.BlockCrystalsand;
import com.gmm.gctold.common.blocks.BlockCrystalsandstone;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BiomeLibraIsland extends Biome {
  public static final BiomeLibraIsland biome = new BiomeLibraIsland();
  public BiomeLibraIsland() {
    super((new Biome.BiomeProperties("Libra Island")).setRainfall(0.5F).setBaseHeight(0.2F).setHeightVariation(0.1F).setTemperature(0.5F));
    setRegistryName("libra_island");
    this.topBlock = BlockCrystalsand.block.getDefaultState();
    this.fillerBlock = BlockCrystalsandstone.block.getDefaultState();
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
