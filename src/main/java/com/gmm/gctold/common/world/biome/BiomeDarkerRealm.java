package com.gmm.gctold.common.world.biome;

import java.util.Random;
import com.gmm.gctold.common.blocks.BlockDenseDarkstone;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeDarkerRealm extends Biome {
  public static final BiomeDarkerRealm biome = new BiomeDarkerRealm();
  public BiomeDarkerRealm() {
    super((new Biome.BiomeProperties("Darker Realm")).setRainfall(0.0F).setBaseHeight(0.4F).setWaterColor(-16751002).setHeightVariation(0.2F)
        .setTemperature(0.5F));
    setRegistryName("darkerrealm");
    this.topBlock = BlockDenseDarkstone.block.getDefaultState();
    this.fillerBlock = BlockDenseDarkstone.block.getDefaultState();
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
    this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityCreeper.class, 15, 1, 15));
    this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityZombie.class, 15, 1, 15));
    this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 15, 1, 15));
    this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityEnderman.class, 15, 1, 15));
    this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntitySpider.class, 15, 1, 15));
  }

  @SideOnly(Side.CLIENT)
  public int getGrassColorAtPos(BlockPos pos) {
    return -16777216;
  }

  @SideOnly(Side.CLIENT)
  public int getFoliageColorAtPos(BlockPos pos) {
    return -16777216;
  }

  @SideOnly(Side.CLIENT)
  public int getSkyColorByTemp(float currentTemperature) {
    return -10066330;
  }

  public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
    return super.getRandomTreeFeature(rand);
  }
}
