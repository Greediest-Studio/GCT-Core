package com.gmm.gctold.common.world.biome;

import java.util.Random;
import com.gmm.gctold.common.blocks.BlockLunarGrass;
import com.gmm.gctold.common.blocks.BlockLunarSoil;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BiomeVirgoPlain extends Biome {
  public static final BiomeVirgoPlain biome = new BiomeVirgoPlain();
  public BiomeVirgoPlain() {
    super((new Biome.BiomeProperties("Vergo Plain")).setRainfall(0.5F).setBaseHeight(0.1F).setHeightVariation(0.1F).setTemperature(0.5F));
    setRegistryName("virgo_plain");
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
