package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.common.config.JotunheimConfig;
import com.smd.gctcore.common.world.biome.jotunheim.BiomeProviderJotunheim;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class WorldProviderJotunheim extends WorldProvider {
    private JotunheimTerrainKernel kernel;

    @Override
    public void init() {
        kernel = new JotunheimTerrainKernel(world.getSeed(), JotunheimConfig.snapshot());
        biomeProvider = new BiomeProviderJotunheim(kernel);
        hasSkyLight = true;
    }

    public JotunheimTerrainKernel terrainKernel() { return kernel; }

    @Override
    public DimensionType getDimensionType() { return JotunheimDimension.TYPE; }

    @Override
    public IChunkGenerator createChunkGenerator() { return new ChunkGeneratorJotunheim(world, kernel); }

    @Override
    public boolean isSurfaceWorld() { return true; }

    @Override
    public boolean canRespawnHere() { return false; }

    @Override
    public int getAverageGroundLevel() { return 150; }

    @Override
    public float getCloudHeight() { return 272.0F; }

    @Override
    public boolean doesWaterVaporize() { return false; }

    @Override
    public boolean canDoLightning(Chunk chunk) { return false; }

    @Override
    public boolean canDoRainSnowIce(Chunk chunk) { return false; }

    @Override
    public void updateWeather() {
        // Local height/biome weather belongs to ecology stage four.
        // Keep this world's strengths dry without touching shared/derived WorldInfo flags.
        world.setRainStrength(0.0F);
        world.setThunderStrength(0.0F);
    }

    @Override
    public boolean canCoordinateBeSpawn(int x, int z) {
        for (int y : kernel.surfaceHeights(x, z)) {
            if (y >= 135 && y <= 170) return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
        float day = daylight(world.getCelestialAngle(partialTicks));
        return new Vec3d(0.10D + day * 0.32D, 0.14D + day * 0.43D, 0.23D + day * 0.49D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(float celestialAngle, float partialTicks) {
        float day = daylight(celestialAngle);
        return new Vec3d(0.10D + day * 0.48D, 0.13D + day * 0.51D, 0.19D + day * 0.49D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getCloudColor(float partialTicks) {
        float day = daylight(world.getCelestialAngle(partialTicks));
        return new Vec3d(0.22D + day * 0.64D, 0.27D + day * 0.63D, 0.35D + day * 0.58D);
    }

    private static float daylight(float celestialAngle) {
        return MathHelper.clamp(MathHelper.cos(celestialAngle * ((float) Math.PI * 2.0F)) * 2.0F + 0.5F, 0.0F, 1.0F);
    }
}
