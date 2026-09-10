package com.smd.gctcore.common.world.NothingnessDim;

import com.smd.gctcore.common.world.WorldProviderLockedTime;
import com.smd.gctcore.common.world.chunks.ChunkGeneratorNothingness;

import net.minecraft.init.Biomes;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import org.jetbrains.annotations.NotNull;

public class WorldProviderNothingness extends WorldProviderLockedTime {

    private static final long MIDNIGHT = 18000L;

    public WorldProviderNothingness() {
        super(MIDNIGHT);
    }

    @Override
    public @NotNull DimensionType getDimensionType() {
        return DimensionTypeNothingness.nothingness;
    }

    @Override
    public boolean canRespawnHere() {
        return super.canRespawnHere();
    }

    @Override
    public @NotNull IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorNothingness(world);
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
