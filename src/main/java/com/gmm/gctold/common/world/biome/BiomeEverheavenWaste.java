package com.gmm.gctold.common.world.biome;

import java.util.Random;
import com.gmm.gctold.common.blocks.BlockMeteor;
import com.gmm.gctold.common.entity.EntityBligtz;
import com.gmm.gctold.common.entity.EntityBnatuz;
import com.gmm.gctold.common.entity.EntityBninz;
import com.gmm.gctold.common.entity.EntityBthdz;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeEverheavenWaste extends Biome {
  public static final BiomeEverheavenWaste biome = new BiomeEverheavenWaste();
  public BiomeEverheavenWaste() {
    super((new Biome.BiomeProperties("Everheaven Waste")).setRainfall(0.0F).setBaseHeight(0.1F).setWaterColor(-13434880).setHeightVariation(0.2F)
        .setTemperature(2.0F));
    setRegistryName("everheaven_waste");
    this.topBlock = BlockMeteor.block.getDefaultState();
    this.fillerBlock = BlockMeteor.block.getDefaultState();
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
    this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityBthdz.BthdzEntity.class, 10, 1, 4));
    this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityBninz.BninzEntity.class, 10, 1, 2));
    this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityBligtz.BligtzEntity.class, 10, 1, 2));
    this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityBnatuz.BnatuzEntity.class, 10, 1, 2));
    this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityZombie.class, 20, 1, 2));
  }

  @SideOnly(Side.CLIENT)
  public int getGrassColorAtPos(BlockPos pos) {
    return -13434880;
  }

  @SideOnly(Side.CLIENT)
  public int getFoliageColorAtPos(BlockPos pos) {
    return -13434880;
  }

  @SideOnly(Side.CLIENT)
  public int getSkyColorByTemp(float currentTemperature) {
    return -6710785;
  }

  public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
    return super.getRandomTreeFeature(rand);
  }
}
