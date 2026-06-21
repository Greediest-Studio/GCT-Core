package com.smd.gctcore.common.world.biome.nilfheim;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BiomeProviderNilfheim extends BiomeProvider {
    private static final double SUNKEN_TEAR_THRESHOLD = -0.68D;

    private final long seed;
    private final List<Biome> spawnBiomes;

    public BiomeProviderNilfheim(long seed) {
        super();
        this.seed = seed;
        this.spawnBiomes = Arrays.asList(NilfheimBiomes.SOUL_EMBER_PLAINS, NilfheimBiomes.HOWLING_ICEFIELD);
    }

    @Override
    public List<Biome> getBiomesToSpawnIn() {
        return spawnBiomes;
    }

    @Override
    public Biome getBiome(BlockPos pos, Biome defaultBiome) {
        return getBiomeAt(pos.getX(), pos.getZ());
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        return getBiomes(biomes, x, z, width, height, false);
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
        if (listToReuse == null || listToReuse.length < width * length) {
            listToReuse = new Biome[width * length];
        }

        for (int dz = 0; dz < length; dz++) {
            for (int dx = 0; dx < width; dx++) {
                listToReuse[dx + dz * width] = getBiomeAt(x + dx, z + dz);
            }
        }

        return listToReuse;
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        for (int dz = -radius; dz <= radius; dz += 16) {
            for (int dx = -radius; dx <= radius; dx += 16) {
                if (!allowed.contains(getBiomeAt(x + dx, z + dz))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    @Nullable
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {
        BlockPos result = null;
        int found = 0;

        for (int dz = -range; dz <= range; dz += 16) {
            for (int dx = -range; dx <= range; dx += 16) {
                Biome biome = getBiomeAt(x + dx, z + dz);
                if (biomes.contains(biome) && (result == null || random.nextInt(++found) == 0)) {
                    result = new BlockPos(x + dx, 0, z + dz);
                }
            }
        }

        return result;
    }

    public Biome getBiomeAt(int x, int z) {
        if (NilfheimBiomes.MISTVEIL_DEPTHS == null) {
            return Biomes.ICE_PLAINS;
        }

        int warpedX = warpX(x, z);
        int warpedZ = warpZ(x, z);

        double basin = basinNoise(warpedX, warpedZ);
        if (basin < SUNKEN_TEAR_THRESHOLD) {
            return NilfheimBiomes.SUNKEN_TEAR;
        }

        double boundary = Math.abs(noise(warpedX, warpedZ, 0.006D, 91L)
                + noise(warpedX, warpedZ, 0.021D, 92L) * 0.28D);
        double throat = noise(warpedX, warpedZ, 0.018D, 571L) + noise(warpedX, warpedZ, 0.055D, 572L) * 0.35D;
        double localRoughness = noise(warpedX, warpedZ, 0.038D, 573L);
        if (!isNearSunkenTear(x, z) && (boundary < 0.036D + localRoughness * 0.012D || throat > 0.80D)) {
            return NilfheimBiomes.HVERGELMIR_THROAT;
        }

        double mountain = noise(warpedX, warpedZ, 0.0045D, 131L) + noise(warpedX, warpedZ, 0.018D, 132L) * 0.45D;
        if (mountain > 0.48D + noise(warpedX, warpedZ, 0.031D, 133L) * 0.06D) {
            return NilfheimBiomes.RIMEFANG_MOUNTAINS;
        }

        double forest = noise(warpedX, warpedZ, 0.0065D, 311L) + noise(warpedX, warpedZ, 0.023D, 312L) * 0.25D;
        if (forest > 0.38D + noise(warpedX, warpedZ, 0.040D, 313L) * 0.055D) {
            return NilfheimBiomes.GLIMMERING_HOLLOW;
        }

        double ice = noise(warpedX, warpedZ, 0.004D, 411L);
        if (ice < -0.28D + noise(warpedX, warpedZ, 0.026D, 412L) * 0.05D) {
            return NilfheimBiomes.HOWLING_ICEFIELD;
        }

        double ember = noise(warpedX, warpedZ, 0.007D, 511L);
        if (ember > 0.18D + noise(warpedX, warpedZ, 0.030D, 512L) * 0.05D) {
            return NilfheimBiomes.SOUL_EMBER_PLAINS;
        }

        return NilfheimBiomes.MISTVEIL_DEPTHS;
    }

    private boolean isNearSunkenTear(int x, int z) {
        int[] offsets = new int[] {-96, -64, -32, 0, 32, 64, 96};
        for (int dx : offsets) {
            for (int dz : offsets) {
                int warpedX = warpX(x + dx, z + dz);
                int warpedZ = warpZ(x + dx, z + dz);
                if (basinNoise(warpedX, warpedZ) < SUNKEN_TEAR_THRESHOLD) {
                    return true;
                }
            }
        }
        return false;
    }

    private double basinNoise(int x, int z) {
        return noise(x, z, 0.0055D, 211L) + noise(x, z, 0.026D, 212L) * 0.35D;
    }

    private int warpX(int x, int z) {
        return x + (int) Math.round(noise(x, z, 0.0028D, 901L) * 54.0D
                + noise(x, z, 0.014D, 902L) * 14.0D);
    }

    private int warpZ(int x, int z) {
        return z + (int) Math.round(noise(x, z, 0.0028D, 903L) * 54.0D
                + noise(x, z, 0.014D, 904L) * 14.0D);
    }

    private double noise(int x, int z, double scale, long salt) {
        int xi = fastFloor(x * scale);
        int zi = fastFloor(z * scale);
        double xf = x * scale - xi;
        double zf = z * scale - zi;
        double u = fade(xf);
        double v = fade(zf);
        double a = value(xi, zi, salt);
        double b = value(xi + 1, zi, salt);
        double c = value(xi, zi + 1, salt);
        double d = value(xi + 1, zi + 1, salt);
        return lerp(lerp(a, b, u), lerp(c, d, u), v);
    }

    private double value(int x, int z, long salt) {
        long n = x * 341873128712L + z * 132897987541L + seed + salt * 42317861L;
        n = (n ^ (n >> 13)) * 1274126177L;
        n = n ^ (n >> 16);
        return ((n & 0xFFFFFF) / (double) 0x7FFFFF) - 1.0D;
    }

    private static int fastFloor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6.0D - 15.0D) + 10.0D);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
