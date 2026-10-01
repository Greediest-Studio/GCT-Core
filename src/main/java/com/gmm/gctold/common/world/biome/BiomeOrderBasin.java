package com.gmm.gctold.common.world.biome;

import com.gmm.gctold.common.blocks.BlockOrderCobblestone;
import com.gmm.gctold.common.blocks.BlockOrderStone;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeOrderBasin extends Biome {
    public static final BiomeOrderBasin biome = new BiomeOrderBasin();

    public BiomeOrderBasin() {
        super(new Biome.BiomeProperties("Order Basin")
                .setRainfall(0.0F)
                .setBaseHeight(0.0F)
                .setHeightVariation(1.0F)
                .setTemperature(0.5F));
        setRegistryName("order_basin");
        topBlock = BlockOrderCobblestone.block.getDefaultState();
        fillerBlock = BlockOrderStone.block.getDefaultState();
        decorator.treesPerChunk = 1;
        decorator.flowersPerChunk = 4;
        decorator.grassPerChunk = 4;
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

    @SideOnly(Side.CLIENT)
    public int getGrassColorAtPos(BlockPos pos) {
        return -1;
    }

    @SideOnly(Side.CLIENT)
    public int getFoliageColorAtPos(BlockPos pos) {
        return -1;
    }

    @SideOnly(Side.CLIENT)
    public int getSkyColorByTemp(float currentTemperature) {
        return -1;
    }

    public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
        return super.getRandomTreeFeature(rand);
    }
}
