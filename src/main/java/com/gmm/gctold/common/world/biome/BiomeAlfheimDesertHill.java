package com.gmm.gctold.common.world.biome;

import java.util.Random;
import com.gmm.gctold.common.blocks.BlockAlfSand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeAlfheimDesertHill extends Biome {
  public static final BiomeAlfheimDesertHill biome = new BiomeAlfheimDesertHill();
  public BiomeAlfheimDesertHill() {
    super((new Biome.BiomeProperties("Alfheim Desert Hill")).setRainfall(0.0F).setBaseHeight(0.3F).setWaterColor(-256).setHeightVariation(0.8F)
        .setTemperature(2.0F));
    setRegistryName("alfheim_desert_hill");
    this.topBlock = BlockAlfSand.block.getDefaultState();
    this.fillerBlock = BlockAlfSand.block.getDefaultState();
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
    return -256;
  }

  @SideOnly(Side.CLIENT)
  public int getFoliageColorAtPos(BlockPos pos) {
    return -256;
  }

  @SideOnly(Side.CLIENT)
  public int getSkyColorByTemp(float currentTemperature) {
    return -6684928;
  }

  public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
    return super.getRandomTreeFeature(rand);
  }
}
