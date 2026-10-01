package com.gmm.gctold.common.world.biome;

import java.util.Random;
import com.gmm.gctold.common.blocks.BlockManaDirt;
import com.gmm.gctold.common.blocks.BlockManaGrass;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.common.BiomeManager;

public class BiomeManaForestHill extends Biome {
  public static final BiomeManaForestHill biome = new BiomeManaForestHill();
  public BiomeManaForestHill() {
    super((new Biome.BiomeProperties("Mana Forest Hill")).setRainfall(0.5F).setBaseHeight(0.4F).setWaterColor(-16750900).setHeightVariation(1.0F)
        .setTemperature(0.5F));
    setRegistryName("mana_forest_hill");
    this.topBlock = BlockManaGrass.block.getDefaultState();
    this.fillerBlock = BlockManaDirt.block.getDefaultState();
    this.decorator.treesPerChunk = 0;
    this.decorator.flowersPerChunk = 8;
    this.decorator.grassPerChunk = 10;
    this.decorator.mushroomsPerChunk = 2;
    this.decorator.bigMushroomsPerChunk = 1;
    this.decorator.reedsPerChunk = 0;
    this.decorator.cactiPerChunk = 0;
    this.decorator.sandPatchesPerChunk = 1;
    this.decorator.gravelPatchesPerChunk = 1;
    this.spawnableMonsterList.clear();
    this.spawnableCreatureList.clear();
    this.spawnableWaterCreatureList.clear();
    this.spawnableCaveCreatureList.clear();
  }

  public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
    return super.getRandomTreeFeature(rand);
  }
}
