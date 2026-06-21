package com.smd.gctcore.common.world.NilfheimDim;

import com.smd.gctcore.common.world.biome.nilfheim.BiomeProviderNilfheim;
import com.smd.gctcore.common.world.chunks.ChunkGeneratorNilfheim;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderNilfheim extends WorldProvider {
    @Override
    public DimensionType getDimensionType() {
        return DimensionTypeNilfheim.NILFHEIM;
    }

    @Override
    public void init() {
        this.biomeProvider = new BiomeProviderNilfheim(world.getSeed());
        this.hasSkyLight = true;
        if (world.isRemote) {
            setupWeatherRenderer();
        }
    }

    @SideOnly(Side.CLIENT)
    private void setupWeatherRenderer() {
        this.setWeatherRenderer(new NilfheimSnowRenderer());
    }

    @Override
    protected void generateLightBrightnessTable() {
        for (int i = 0; i <= 15; ++i) {
            float inverse = 1.0F - i / 15.0F;
            this.lightBrightnessTable[i] = ((1.0F - inverse) / (inverse * 3.0F + 1.0F)) * 0.62F;
        }
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorNilfheim(world);
    }

    @Override
    public boolean canRespawnHere() {
        return false;
    }

    @Override
    public boolean isSurfaceWorld() {
        return false;
    }

    @Override
    public int getAverageGroundLevel() {
        return 64;
    }

    @Override
    public float getCloudHeight() {
        return 192.0F;
    }

    @Override
    public boolean doesWaterVaporize() {
        return false;
    }

    @Override
    public boolean canDoLightning(net.minecraft.world.chunk.Chunk chunk) {
        return false;
    }

    @Override
    public boolean canDoRainSnowIce(net.minecraft.world.chunk.Chunk chunk) {
        return true;
    }

    @Override
    public boolean canCoordinateBeSpawn(int x, int z) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
        return new Vec3d(0.045D, 0.075D, 0.120D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(float celestialAngle, float partialTicks) {
        return new Vec3d(0.080D, 0.135D, 0.215D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getCloudColor(float partialTicks) {
        return new Vec3d(0.100D, 0.155D, 0.240D);
    }

    @Override
    public float getSunBrightness(float partialTicks) {
        return 0.18F;
    }

    @Override
    public float getSunBrightnessFactor(float partialTicks) {
        return 0.12F;
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return 0.30F;
    }
}
