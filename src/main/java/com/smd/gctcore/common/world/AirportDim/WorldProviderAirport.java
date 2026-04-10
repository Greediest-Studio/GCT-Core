package com.smd.gctcore.common.world.AirportDim;

import com.smd.gctcore.common.world.chunks.ChunkGeneratorAirport;

import net.minecraft.init.Biomes;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderAirport extends WorldProvider {

    private static final long Freeze_time = 6000L;
    private boolean timeLocked = true;

    @Override
    public long getWorldTime() {
        return timeLocked ? Freeze_time : world.getWorldTime();
    }

    public void lockTimeAtNoon() {
        this.timeLocked = true;
    }

    @Override
    public DimensionType getDimensionType() {
        return DimensionTypeAirport.Airport;
    }

    @Override
    public boolean canRespawnHere() {
        return true;
    }
    @Override
    public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorAirport(world);
    }

    @Override
    public float getCloudHeight(){
        return 255;
    }

    @Override
    protected void init() {
        this.biomeProvider = new BiomeProviderSingle(Biomes.PLAINS);
        this.hasSkyLight = true;
    }
}