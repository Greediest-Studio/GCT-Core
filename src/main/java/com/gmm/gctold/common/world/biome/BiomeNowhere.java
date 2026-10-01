package com.gmm.gctold.common.world.biome;

import net.minecraft.init.Blocks;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeNowhere extends Biome {
    public static final BiomeNowhere biome = new BiomeNowhere();

    public BiomeNowhere() {
        super(new Biome.BiomeProperties("Nowhere")
                .setRainfall(0.0F)
                .setBaseHeight(0.8F).setWaterColor(-16777216)
                .setHeightVariation(0.6F)
                .setTemperature(0.5F));
        setRegistryName("nowhere");
        topBlock = Blocks.OBSIDIAN.getDefaultState();
        fillerBlock = Blocks.OBSIDIAN.getDefaultState();
        decorator.treesPerChunk = 0;
        decorator.flowersPerChunk = 0;
        decorator.grassPerChunk = 0;
        decorator.mushroomsPerChunk = 0;
        decorator.bigMushroomsPerChunk = 0;
        decorator.reedsPerChunk = 0;
        decorator.cactiPerChunk = 1;
        decorator.sandPatchesPerChunk = 32;
        decorator.gravelPatchesPerChunk = 64;
        spawnableMonsterList.clear();
        spawnableCreatureList.clear();
        spawnableWaterCreatureList.clear();
        spawnableCaveCreatureList.clear();
    }
    public static void init() {
        BiomeDictionary.addTypes(biome, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.MESA, BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS, BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.SANDY, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.WASTELAND, BiomeDictionary.Type.BEACH, BiomeDictionary.Type.VOID, BiomeDictionary.Type.HOT, BiomeDictionary.Type.COLD, BiomeDictionary.Type.SPARSE, BiomeDictionary.Type.DENSE, BiomeDictionary.Type.WET, BiomeDictionary.Type.DRY, BiomeDictionary.Type.SAVANNA, BiomeDictionary.Type.CONIFEROUS, BiomeDictionary.Type.JUNGLE, BiomeDictionary.Type.SPOOKY, BiomeDictionary.Type.DEAD, BiomeDictionary.Type.LUSH, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.END, BiomeDictionary.Type.MUSHROOM, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.RARE, BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.RIVER, BiomeDictionary.Type.WATER);
    }


    @SideOnly(Side.CLIENT)
    public int getGrassColorAtPos(BlockPos pos) {
        return -6750055;
    }

    @SideOnly(Side.CLIENT)
    public int getFoliageColorAtPos(BlockPos pos) {
        return -6750055;
    }

    @SideOnly(Side.CLIENT)
    public int getSkyColorByTemp(float currentTemperature) {
        return -6750055;
    }

    public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
        return super.getRandomTreeFeature(rand);
    }
}
