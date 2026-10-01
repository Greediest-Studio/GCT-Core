package com.gmm.gctold.common.world.biome;

import java.util.Random;
import com.gmm.gctold.common.blocks.BlockCorruptdirt;
import com.gmm.gctold.common.blocks.BlockCorruptgrass;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeCurruptplain extends Biome {
  public static final BiomeCurruptplain biome = new BiomeCurruptplain();
  public BiomeCurruptplain() {
    super((new Biome.BiomeProperties("Curruptplain")).setRainfall(0.5F).setBaseHeight(0.1F).setWaterColor(-10092442).setHeightVariation(0.4F)
        .setTemperature(0.5F));
    setRegistryName("curruptplain");
    this.topBlock = BlockCorruptgrass.block.getDefaultState();
    this.fillerBlock = BlockCorruptdirt.block.getDefaultState();
    this.decorator.treesPerChunk = 0;
    this.decorator.flowersPerChunk = 0;
    this.decorator.grassPerChunk = 0;
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

  @SideOnly(Side.CLIENT)
  public int getGrassColorAtPos(BlockPos pos) {
    return -10092442;
  }

  @SideOnly(Side.CLIENT)
  public int getFoliageColorAtPos(BlockPos pos) {
    return -10092442;
  }

  @SideOnly(Side.CLIENT)
  public int getSkyColorByTemp(float currentTemperature) {
    return -10092442;
  }

  public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
    return super.getRandomTreeFeature(rand);
  }
}
