package com.gmm.gctold.common.world.biome;

import java.util.Random;
import com.gmm.gctold.common.blocks.BlockBloodydirt;
import com.gmm.gctold.common.blocks.BlockBloodygrass;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeBloodyplain extends Biome {
  public static final BiomeBloodyplain biome = new BiomeBloodyplain();
  public BiomeBloodyplain() {
    super((new Biome.BiomeProperties("Bloodyplain")).setRainfall(0.5F).setBaseHeight(0.1F).setWaterColor(-6737152).setHeightVariation(0.4F)
        .setTemperature(0.5F));
    setRegistryName("bloodyplain");
    this.topBlock = BlockBloodygrass.block.getDefaultState();
    this.fillerBlock = BlockBloodydirt.block.getDefaultState();
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
    return -6737152;
  }

  @SideOnly(Side.CLIENT)
  public int getFoliageColorAtPos(BlockPos pos) {
    return -6737152;
  }

  @SideOnly(Side.CLIENT)
  public int getSkyColorByTemp(float currentTemperature) {
    return -6737152;
  }

  public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
    return super.getRandomTreeFeature(rand);
  }
}
