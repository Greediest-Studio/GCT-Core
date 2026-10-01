package com.gmm.gctold.common.world.biome;

import net.minecraft.init.Blocks;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeVoidPlain extends Biome {
    public static final BiomeVoidPlain biome = new BiomeVoidPlain();

    public BiomeVoidPlain() {
        super(new Biome.BiomeProperties("Void Plain")
                .setRainfall(0.5F)
                .setBaseHeight(0.3F).setWaterColor(-3355444)
                .setHeightVariation(1.2F)
                .setTemperature(2.0F));
        setRegistryName("void_plain");
        topBlock = Blocks.BEDROCK.getDefaultState();
        fillerBlock = Blocks.BEDROCK.getDefaultState();
        decorator.treesPerChunk = 0;
        decorator.flowersPerChunk = 0;
        decorator.grassPerChunk = 0;
        decorator.mushroomsPerChunk = 0;
        decorator.bigMushroomsPerChunk = 0;
        decorator.reedsPerChunk = 0;
        decorator.cactiPerChunk = 0;
        decorator.sandPatchesPerChunk = 0;
        decorator.gravelPatchesPerChunk = 0;
        spawnableMonsterList.clear();
        spawnableCreatureList.clear();
        spawnableWaterCreatureList.clear();
        spawnableCaveCreatureList.clear();
    }
    public static void init() {
        BiomeDictionary.addTypes(biome, BiomeDictionary.Type.WASTELAND, BiomeDictionary.Type.VOID, BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY, BiomeDictionary.Type.DEAD);
    }


    @SideOnly(Side.CLIENT)
    public int getGrassColorAtPos(BlockPos pos) {
        return -10066330;
    }

    @SideOnly(Side.CLIENT)
    public int getFoliageColorAtPos(BlockPos pos) {
        return -10066330;
    }

    @SideOnly(Side.CLIENT)
    public int getSkyColorByTemp(float currentTemperature) {
        return -10066330;
    }

    public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
        return super.getRandomTreeFeature(rand);
    }
}
