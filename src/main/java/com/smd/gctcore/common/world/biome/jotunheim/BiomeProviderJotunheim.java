package com.smd.gctcore.common.world.biome.jotunheim;

import com.smd.gctcore.common.world.jotunheim.JotunheimTerrainKernel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Stable XZ identity only. Height-dependent ecology never mutates biome bytes. */
public final class BiomeProviderJotunheim extends BiomeProvider {
    private final JotunheimTerrainKernel kernel;
    private final List<Biome> spawnBiomes;

    public BiomeProviderJotunheim(JotunheimTerrainKernel kernel) {
        if (kernel == null) throw new IllegalArgumentException("Jotunheim terrain kernel cannot be null");
        if (JotunheimBiomes.JOTUNNWOOD == null || JotunheimBiomes.GIANTS_HIGHLANDS == null) {
            throw new IllegalStateException("Initialize Jotunheim biomes before creating the world provider");
        }
        this.kernel = kernel;
        this.spawnBiomes = Arrays.<Biome>asList(JotunheimBiomes.JOTUNNWOOD, JotunheimBiomes.GIANTS_HIGHLANDS);
    }

    public JotunheimTerrainKernel kernel() { return kernel; }

    @Override
    public List<Biome> getBiomesToSpawnIn() { return spawnBiomes; }

    @Override
    public Biome getBiome(BlockPos pos) { return getBiome(pos, null); }

    @Override
    public Biome getBiome(BlockPos pos, Biome fallback) {
        return JotunheimBiomes.byIndex(kernel.biomeIndex(pos.getX(), pos.getZ()));
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] reuse, int x, int z, int width, int length) {
        // This vanilla API operates at quarter-block resolution, unlike getBiomes.
        if (reuse == null || reuse.length < width * length) reuse = new Biome[width * length];
        for (int dz = 0; dz < length; dz++) {
            for (int dx = 0; dx < width; dx++) {
                reuse[dx + dz * width] = JotunheimBiomes.byIndex(kernel.biomeIndex((x + dx) * 4, (z + dz) * 4));
            }
        }
        return reuse;
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] reuse, int x, int z, int width, int length) {
        return getBiomes(reuse, x, z, width, length, false);
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] reuse, int x, int z, int width, int length, boolean cacheFlag) {
        if (reuse == null || reuse.length < width * length) reuse = new Biome[width * length];
        for (int dz = 0; dz < length; dz++) {
            for (int dx = 0; dx < width; dx++) {
                reuse[dx + dz * width] = JotunheimBiomes.byIndex(kernel.biomeIndex(x + dx, z + dz));
            }
        }
        return reuse;
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        for (int dz = -radius; dz <= radius; dz += 4) {
            for (int dx = -radius; dx <= radius; dx += 4) {
                if (!allowed.contains(JotunheimBiomes.byIndex(kernel.biomeIndex(x + dx, z + dz)))) return false;
            }
        }
        return true;
    }

    @Override
    @Nullable
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> allowed, Random random) {
        BlockPos result = null;
        int found = 0;
        for (int dz = -range; dz <= range; dz += 4) {
            for (int dx = -range; dx <= range; dx += 4) {
                if (allowed.contains(JotunheimBiomes.byIndex(kernel.biomeIndex(x + dx, z + dz)))) {
                    if (random.nextInt(++found) == 0) result = new BlockPos(x + dx, 0, z + dz);
                }
            }
        }
        return result;
    }
}
