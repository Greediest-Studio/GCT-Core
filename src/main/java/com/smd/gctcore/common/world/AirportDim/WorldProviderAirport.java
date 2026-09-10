package com.smd.gctcore.common.world.AirportDim;

import com.smd.gctcore.common.world.WorldProviderLockedTime;

import net.minecraft.init.Biomes;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.IChunkGenerator;
import org.jetbrains.annotations.NotNull;

public class WorldProviderAirport extends WorldProviderLockedTime {

    private static final long NOON = 6000L;

    public WorldProviderAirport() {
        super(NOON);
    }

    @Override
    public @NotNull DimensionType getDimensionType() {
        return DimensionTypeAirport.Airport;
    }

    @Override
    public boolean canRespawnHere() {
        return super.canRespawnHere();
    }

    @Override
    public @NotNull IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorFlat(world, world.getSeed(), true, "3;2*7,3*1,1*2;1;");
    }

    @Override
    public float getCloudHeight() {
        return 255;
    }

    @Override
    protected void init() {
        this.biomeProvider = new BiomeProviderSingle(Biomes.VOID);
        this.hasSkyLight = true;
    }

    @Override
    public void calculateInitialWeather() {
    }

    @Override
    public void updateWeather() {
    }
}
