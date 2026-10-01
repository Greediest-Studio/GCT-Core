package com.gmm.gctold.common.world.biome;

import com.gmm.gctold.common.blocks.BlockAlfDirt;
import com.gmm.gctold.common.blocks.BlockAlfGrass;
import com.gmm.gctold.common.blocks.BlockDreamwoodLeaves;
import com.gmm.gctold.common.blocks.BlockDreamwoodLog;
import java.util.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BiomeAlfheimForest extends Biome {
  public static final BiomeAlfheimForest biome = new BiomeAlfheimForest();

  public BiomeAlfheimForest() {
    super((new Biome.BiomeProperties("Alfheim Forest")).setRainfall(0.5F).setBaseHeight(0.1F)
        .setHeightVariation(0.3F).setTemperature(0.5F));
    setRegistryName("alfheim_forest");
    this.topBlock = BlockAlfGrass.block.getDefaultState();
    this.fillerBlock = BlockAlfDirt.block.getDefaultState();
    this.decorator.treesPerChunk = 0;
    this.decorator.flowersPerChunk = 4;
    this.decorator.grassPerChunk = 4;
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
  public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
    return new WorldGenAlfheimTree(9, BlockDreamwoodLog.block.getDefaultState(),
        BlockDreamwoodLeaves.block.getDefaultState(), BlockAlfGrass.block, BlockAlfDirt.block);
  }
}
