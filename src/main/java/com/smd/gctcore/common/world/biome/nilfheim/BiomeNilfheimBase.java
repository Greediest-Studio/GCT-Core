package com.smd.gctcore.common.world.biome.nilfheim;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;

public class BiomeNilfheimBase extends Biome {
    private final int fogColor;
    private final int foliageColor;
    private final int grassColor;

    public BiomeNilfheimBase(String registryName, String displayName, IBlockState topBlock, IBlockState fillerBlock,
                             float baseHeight, float heightVariation, int waterColor, int fogColor,
                             int foliageColor, int grassColor) {
        super(new BiomeProperties(displayName)
                .setBaseHeight(baseHeight)
                .setHeightVariation(heightVariation)
                .setTemperature(0.0F)
                .setRainfall(0.7F)
                .setWaterColor(waterColor)
                .setSnowEnabled());
        setRegistryName(registryName);
        this.topBlock = topBlock;
        this.fillerBlock = fillerBlock;
        this.fogColor = fogColor;
        this.foliageColor = foliageColor;
        this.grassColor = grassColor;
        this.spawnableMonsterList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCaveCreatureList.clear();
        this.decorator.treesPerChunk = -999;
        this.decorator.flowersPerChunk = -999;
        this.decorator.grassPerChunk = -999;
        this.decorator.generateFalls = false;
    }

    public int getFogColor() {
        return fogColor;
    }

    @Override
    public int getFoliageColorAtPos(net.minecraft.util.math.BlockPos pos) {
        return foliageColor;
    }

    @Override
    public int getGrassColorAtPos(net.minecraft.util.math.BlockPos pos) {
        return grassColor;
    }
}
