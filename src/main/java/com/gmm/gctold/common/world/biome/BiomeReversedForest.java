package com.gmm.gctold.common.world.biome;

import com.gmm.gctold.common.blocks.BlockReversedAlfDirt;
import com.gmm.gctold.common.blocks.BlockReversedAlfGrass;
import com.gmm.gctold.common.blocks.BlockReversedDreamwoodLeaves;
import com.gmm.gctold.common.blocks.BlockReversedDreamwoodLog;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeReversedForest extends Biome {
  public static final BiomeReversedForest biome = new BiomeReversedForest();

  public BiomeReversedForest() {
    super((new Biome.BiomeProperties("Alfheim Forest(Reversed)")).setRainfall(0.5F).setBaseHeight(0.2F)
        .setWaterColor(-16777216).setHeightVariation(0.4F).setTemperature(0.5F));
    setRegistryName("reversed_forest");
    this.topBlock = BlockReversedAlfGrass.block.getDefaultState();
    this.fillerBlock = BlockReversedAlfDirt.block.getDefaultState();
    this.decorator.treesPerChunk = 0;
    this.decorator.flowersPerChunk = 2;
    this.decorator.grassPerChunk = 16;
    this.decorator.mushroomsPerChunk = 0;
    this.decorator.bigMushroomsPerChunk = 2;
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
    return -8716033;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public int getFoliageColorAtPos(BlockPos pos) {
    return -8716033;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public int getSkyColorByTemp(float currentTemperature) {
    return -8716033;
  }

  @Override
  public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
    return new WorldGenAlfheimTree(7, BlockReversedDreamwoodLog.block.getDefaultState(),
        BlockReversedDreamwoodLeaves.block.getDefaultState(), BlockReversedAlfGrass.block, BlockReversedAlfDirt.block);
  }
}
