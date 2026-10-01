package com.gmm.gctold.common.world.biome;

import java.util.Random;
import com.gmm.gctold.common.blocks.BlockWarprack;
import com.gmm.gctold.common.entity.EntityRemnantWandering;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeWarped extends Biome {
  public static final BiomeWarped biome = new BiomeWarped();
  public BiomeWarped() {
    super((new Biome.BiomeProperties("Warped")).setRainfall(0.0F).setBaseHeight(3.0F).setWaterColor(-16751002).setHeightVariation(0.1F)
        .setTemperature(1.5F));
    setRegistryName("warped");
    this.topBlock = BlockWarprack.block.getDefaultState();
    this.fillerBlock = BlockWarprack.block.getDefaultState();
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
    this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityRemnantWandering.RemnantWanderingEntity.class, 20, 4, 4));
  }

  @SideOnly(Side.CLIENT)
  public int getGrassColorAtPos(BlockPos pos) {
    return -16751002;
  }

  @SideOnly(Side.CLIENT)
  public int getFoliageColorAtPos(BlockPos pos) {
    return -16751002;
  }

  @SideOnly(Side.CLIENT)
  public int getSkyColorByTemp(float currentTemperature) {
    return -16751002;
  }

  public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
    return super.getRandomTreeFeature(rand);
  }
}
