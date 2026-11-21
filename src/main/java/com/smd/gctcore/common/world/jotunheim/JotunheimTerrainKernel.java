package com.smd.gctcore.common.world.jotunheim;

import java.util.Arrays;

/**
 * Stateless, Minecraft-independent four-layer terrain sampler.
 * All random decisions depend only on the seed, absolute coordinates and salts.
 * The same globally anchored 4x2x4 interpolation is used by point and chunk APIs.
 */
public final class JotunheimTerrainKernel {
    public static final int BIOME_COUNT = 8;
    public static final int GRID_XZ = 4;
    public static final int GRID_Y = 2;
    private static final double EMPTY = -32.0D;

    public enum HeightLayer {
        LAVA(5, 30, 20, 4.0D, 12.0D, 176, 0.78D, 0x153A4B29L),
        DEEP(35, 105, 73, 12.0D, 34.0D, 208, 0.83D, 0x25A69F31L),
        SURFACE(115, 190, 154, 14.0D, 25.0D, 256, 0.88D, 0x38A25C17L),
        CLOUD(200, 250, 224, 7.0D, 14.0D, 192, 0.66D, 0x491C7E63L);

        public final int minY;
        public final int maxY;
        public final int typicalSurface;
        private final double variation;
        private final double thickness;
        private final int cellSize;
        private final double occurrence;
        private final long salt;

        HeightLayer(int minY, int maxY, int typicalSurface, double variation,
                    double thickness, int cellSize, double occurrence, long salt) {
            this.minY = minY;
            this.maxY = maxY;
            this.typicalSurface = typicalSurface;
            this.variation = variation;
            this.thickness = thickness;
            this.cellSize = cellSize;
            this.occurrence = occurrence;
            this.salt = salt;
        }
    }

    private static final HeightLayer[] LAYERS = HeightLayer.values();
    private static final HeightLayer[] LAYER_BY_Y = new HeightLayer[256];

    static {
        for (HeightLayer layer : LAYERS) {
            for (int y = layer.minY; y <= layer.maxY; y++) LAYER_BY_Y[y] = layer;
        }
    }

    /** Immutable snapshot. Changing configuration does not alter an open generator. */
    public static final class Settings {
        public final double biomeScale;
        public final double islandDensity;
        public final double islandScale;
        public final double thickness;
        public final double heightVariation;
        public final double erosion;
        public final double caveAmount;

        public Settings() {
            this(640.0D, 1.0D, 1.0D, 1.0D, 1.0D, 0.85D, 1.0D);
        }

        public Settings(double biomeScale, double islandDensity, double islandScale,
                        double thickness, double heightVariation, double erosion, double caveAmount) {
            this.biomeScale = bounded(biomeScale, 256.0D, 4096.0D, 640.0D);
            this.islandDensity = bounded(islandDensity, 0.55D, 1.4D, 1.0D);
            this.islandScale = bounded(islandScale, 0.65D, 1.75D, 1.0D);
            this.thickness = bounded(thickness, 0.55D, 1.5D, 1.0D);
            this.heightVariation = bounded(heightVariation, 0.25D, 1.75D, 1.0D);
            this.erosion = bounded(erosion, 0.0D, 1.6D, 0.85D);
            this.caveAmount = bounded(caveAmount, 0.0D, 1.5D, 1.0D);
        }
    }

    /** Dense chunk-local result. Indexing is (localX*16+localZ)*256+y. */
    public static final class SampledChunk {
        private final float[] densities = new float[16 * 16 * 256];

        public double density(int localX, int y, int localZ) {
            if (y < 0 || y > 255) return EMPTY;
            return densities[(localX * 16 + localZ) * 256 + y];
        }

        public boolean isSolid(int localX, int y, int localZ) {
            return density(localX, y, localZ) > 0.0D;
        }

        /** Standable feet levels, including cave floors, in ascending Y order. */
        public int[] surfaceHeights(int localX, int localZ) {
            int[] values = new int[128];
            int count = 0;
            for (int y = 1; y < 255; y++) {
                if (isSolid(localX, y - 1, localZ) && !isSolid(localX, y, localZ)
                        && !isSolid(localX, y + 1, localZ)) values[count++] = y;
            }
            return Arrays.copyOf(values, count);
        }
    }

    private static final class LayerColumn {
        final HeightLayer layer;
        final double side;
        final double top;
        final double bottom;

        LayerColumn(HeightLayer layer, double side, double top, double bottom) {
            this.layer = layer;
            this.side = side;
            this.top = top;
            this.bottom = bottom;
        }
    }

    private final long seed;
    private final Settings settings;

    public JotunheimTerrainKernel(long seed) {
        this(seed, new Settings());
    }

    public JotunheimTerrainKernel(long seed, Settings settings) {
        this.seed = seed;
        this.settings = settings == null ? new Settings() : settings;
    }

    public Settings settings() {
        return settings;
    }

    /** Height strata are separate from the eight ordinary XZ biome IDs. */
    public static HeightLayer layerAt(int y) {
        return y < 0 || y >= 256 ? null : LAYER_BY_Y[y];
    }

    /**
     * Eight seed-stable, domain-warped Voronoi regions. No world/biome cache reads.
     * 0 forest, 1 frozen, 2 plateau, 3 swamp, 4 crystal, 5 thunder, 6 volcano, 7 ruins.
     */
    public int biomeIndex(int x, int z) {
        double scale = settings.biomeScale;
        double wx = x + fbm2(x, z, scale * 1.6D, 7101L) * scale * 0.24D;
        double wz = z + fbm2(x, z, scale * 1.6D, 7102L) * scale * 0.24D;
        int cellX = floor(wx / scale);
        int cellZ = floor(wz / scale);
        int biome = 0;
        double best = Double.POSITIVE_INFINITY;
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                int cx = cellX + dx;
                int cz = cellZ + dz;
                double px = (cx + 0.5D + value(cx, cz, 7111L) * 0.34D) * scale;
                double pz = (cz + 0.5D + value(cx, cz, 7112L) * 0.34D) * scale;
                double d = square(wx - px) + square(wz - pz);
                if (d < best) {
                    best = d;
                    biome = (int) (hash(cx, cz, 7113L) & 7L);
                }
            }
        }
        return biome;
    }

    /** Exact point sample of the same global interpolation used for chunk generation. */
    public double density(int x, int y, int z) {
        if (y < 0 || y > 255 || layerAt(y) == null) return EMPTY;
        int gx = Math.floorDiv(x, GRID_XZ) * GRID_XZ;
        int gz = Math.floorDiv(z, GRID_XZ) * GRID_XZ;
        int gy = Math.floorDiv(y, GRID_Y) * GRID_Y;
        double tx = (x - gx) / (double) GRID_XZ;
        double ty = (y - gy) / (double) GRID_Y;
        double tz = (z - gz) / (double) GRID_XZ;
        double[] values = new double[8];
        for (int dz = 0; dz <= 1; dz++) {
            for (int dx = 0; dx <= 1; dx++) {
                LayerColumn[] column = columns(gx + dx * GRID_XZ, gz + dz * GRID_XZ);
                int i = (dz * 2 + dx) * 2;
                values[i] = rawDensity(column, gx + dx * GRID_XZ, gy, gz + dz * GRID_XZ);
                values[i + 1] = rawDensity(column, gx + dx * GRID_XZ, gy + GRID_Y, gz + dz * GRID_XZ);
            }
        }
        return interpolate(values[0], values[2], values[4], values[6],
                values[1], values[3], values[5], values[7], tx, ty, tz);
    }

    public boolean isSolid(int x, int y, int z) {
        return density(x, y, z) > 0.0D;
    }

    public int[] surfaceHeights(int x, int z) {
        int gx = Math.floorDiv(x, GRID_XZ) * GRID_XZ;
        int gz = Math.floorDiv(z, GRID_XZ) * GRID_XZ;
        double tx = (x - gx) / (double) GRID_XZ;
        double tz = (z - gz) / (double) GRID_XZ;
        double[][] grid = new double[4][129];
        for (int dz = 0; dz <= 1; dz++) {
            for (int dx = 0; dx <= 1; dx++) {
                int wx = gx + dx * GRID_XZ;
                int wz = gz + dz * GRID_XZ;
                LayerColumn[] column = columns(wx, wz);
                for (int iy = 0; iy <= 128; iy++) grid[dz*2+dx][iy] = rawDensity(column, wx, iy*GRID_Y, wz);
            }
        }
        boolean[] solid = new boolean[256];
        for (int y = 0; y < 256; y++) {
            if (layerAt(y) == null) continue;
            int iy = y / GRID_Y;
            solid[y] = interpolate(grid[0][iy],grid[1][iy],grid[2][iy],grid[3][iy],
                    grid[0][iy+1],grid[1][iy+1],grid[2][iy+1],grid[3][iy+1],
                    tx,y % GRID_Y/(double)GRID_Y,tz) > 0.0D;
        }
        int[] result = new int[128];
        int count = 0;
        for (int y = 1; y < 255; y++) {
            if (solid[y-1] && !solid[y] && !solid[y+1]) result[count++] = y;
        }
        return Arrays.copyOf(result,count);
    }

    /**
     * 3225 coarse samples instead of 65536 full terrain evaluations per chunk.
     * Shared border samples use absolute grid coordinates, including negative chunks.
     */
    public SampledChunk sampleChunk(int chunkX, int chunkZ) {
        double[][][] grid = new double[5][5][129];
        int startX = chunkX * 16;
        int startZ = chunkZ * 16;
        for (int ix = 0; ix <= 4; ix++) {
            for (int iz = 0; iz <= 4; iz++) {
                int x = startX + ix * GRID_XZ;
                int z = startZ + iz * GRID_XZ;
                LayerColumn[] column = columns(x, z);
                for (int iy = 0; iy <= 128; iy++) grid[ix][iz][iy] = rawDensity(column, x, iy * GRID_Y, z);
            }
        }
        SampledChunk result = new SampledChunk();
        for (int x = 0; x < 16; x++) {
            int ix = x / GRID_XZ;
            double tx = x % GRID_XZ / (double) GRID_XZ;
            for (int z = 0; z < 16; z++) {
                int iz = z / GRID_XZ;
                double tz = z % GRID_XZ / (double) GRID_XZ;
                for (int y = 0; y < 256; y++) {
                    int iy = y / GRID_Y;
                    double d = layerAt(y) == null ? EMPTY : interpolate(
                            grid[ix][iz][iy], grid[ix+1][iz][iy], grid[ix][iz+1][iy], grid[ix+1][iz+1][iy],
                            grid[ix][iz][iy+1], grid[ix+1][iz][iy+1], grid[ix][iz+1][iy+1], grid[ix+1][iz+1][iy+1],
                            tx, y % GRID_Y / (double) GRID_Y, tz);
                    result.densities[(x * 16 + z) * 256 + y] = (float) d;
                }
            }
        }
        return result;
    }

    private LayerColumn[] columns(int x, int z) {
        LayerColumn[] result = new LayerColumn[LAYERS.length];
        // Noise-only morphology keeps terrain continuous across hard biome ID borders.
        double broadRelief = fbm2(x, z, 290.0D, 8101L);
        for (HeightLayer layer : LAYERS) {
            long salt = layer.salt;
            double cell = layer.cellSize * settings.islandScale;
            double wx = x + fbm2(x, z, cell * 0.92D, salt + 1) * cell * 0.13D;
            double wz = z + fbm2(x, z, cell * 0.92D, salt + 2) * cell * 0.13D;
            int cellX = floor(wx / cell);
            int cellZ = floor(wz / cell);
            double bestQ = -10.0D;
            double bestRadius = cell * 0.4D;
            double outline = fbm2(x, z, cell * 0.23D, salt + 8) * 0.11D;
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int cx = cellX + dx;
                    int cz = cellZ + dz;
                    double chance = (value(cx, cz, salt + 3) + 1.0D) * 0.5D;
                    if (chance > Math.min(0.96D, layer.occurrence * settings.islandDensity)) continue;
                    double px = (cx + 0.5D + value(cx, cz, salt + 4) * 0.30D) * cell;
                    double pz = (cz + 0.5D + value(cx, cz, salt + 5) * 0.30D) * cell;
                    double radius = cell * (0.40D + value(cx, cz, salt + 6) * 0.12D)
                            * Math.pow(settings.islandDensity, 0.18D);
                    double rx = radius * (1.0D + value(cx, cz, salt + 7) * 0.18D);
                    double rz = radius * (1.0D - value(cx, cz, salt + 7) * 0.18D);
                    double q = 1.0D - Math.sqrt(square((wx-px)/rx) + square((wz-pz)/rz));
                    q += outline;
                    if (q > bestQ) {
                        bestQ = q;
                        bestRadius = radius;
                    }
                }
            }
            double relief = fbm2(x, z, cell * 0.75D, salt + 11) * 1.45D
                    + fbm2(x, z, cell * 0.18D, salt + 12) * 0.28D + broadRelief * 0.15D;
            double top = layer.typicalSurface + relief * layer.variation * settings.heightVariation;
            top -= square(1.0D - clamp(bestQ, 0.0D, 1.0D)) * (layer == HeightLayer.DEEP ? 6.0D : 4.0D);
            // Cloud soil remains <=232: at least 18 blocks reserved for future crowns.
            top = clamp(top, layer.minY + 8.0D, layer == HeightLayer.CLOUD ? 232.0D : layer.maxY - 4.0D);
            double thick = layer.thickness * settings.thickness
                    * (0.87D + fbm2(x, z, cell * 0.55D, salt + 13) * 0.27D);
            thick *= 0.12D + 0.88D * Math.pow(clamp(bestQ, 0.0D, 1.0D), 0.6D);
            thick = Math.min(thick + 1.8D, top - layer.minY - 1.0D);
            result[layer.ordinal()] = new LayerColumn(layer, bestQ * bestRadius * 0.48D, top, top - thick);
        }
        return result;
    }

    private double rawDensity(LayerColumn[] columns, int x, int y, int z) {
        HeightLayer layer = layerAt(y);
        if (layer == null) return EMPTY;
        LayerColumn c = columns[layer.ordinal()];
        double topDepth = c.top - y;
        double bottomDepth = y - c.bottom;
        double density = Math.min(c.side, Math.min(topDepth, bottomDepth));
        if (density < -5.0D) return Math.max(EMPTY, density);
        double erosion = noise3(x * 0.067D, y * 0.10D, z * 0.067D, layer.salt + 21)
                + noise3(x * 0.13D, y * 0.14D, z * 0.13D, layer.salt + 22) * 0.35D;
        density += erosion * settings.erosion * (layer == HeightLayer.DEEP ? 2.6D : 1.9D);
        if (settings.caveAmount > 0.0D && layer == HeightLayer.DEEP && topDepth > 4.0D && bottomDepth > 3.0D) {
            double caveA = Math.abs(noise3(x * 0.037D, y * 0.058D, z * 0.037D, layer.salt + 31));
            double caveB = Math.abs(noise3(x * 0.052D, y * 0.041D, z * 0.052D, layer.salt + 32));
            double tunnel = (caveA + caveB * 0.65D - 0.17D * settings.caveAmount) * 21.0D;
            density = Math.min(density, tunnel);
        }
        return Math.min(density, Math.min(y - layer.minY + 0.49D, layer.maxY - y + 0.49D));
    }

    private double fbm2(double x, double z, double scale, long salt) {
        return noise2(x / scale, z / scale, salt) * 0.66D
                + noise2(x / scale * 2.07D, z / scale * 2.07D, salt + 101) * 0.25D
                + noise2(x / scale * 4.11D, z / scale * 4.11D, salt + 202) * 0.09D;
    }

    private double noise2(double x, double z, long salt) {
        int ix = floor(x);
        int iz = floor(z);
        double tx = fade(x-ix);
        double tz = fade(z-iz);
        return lerp(lerp(value(ix, iz, salt), value(ix+1, iz, salt), tx),
                lerp(value(ix, iz+1, salt), value(ix+1, iz+1, salt), tx), tz);
    }

    private double noise3(double x, double y, double z, long salt) {
        int ix = floor(x);
        int iy = floor(y);
        int iz = floor(z);
        double tx = fade(x-ix);
        double ty = fade(y-iy);
        double tz = fade(z-iz);
        return interpolate(value3(ix, iy, iz, salt), value3(ix+1, iy, iz, salt),
                value3(ix, iy, iz+1, salt), value3(ix+1, iy, iz+1, salt),
                value3(ix, iy+1, iz, salt), value3(ix+1, iy+1, iz, salt),
                value3(ix, iy+1, iz+1, salt), value3(ix+1, iy+1, iz+1, salt), tx, ty, tz);
    }

    private double value(int x, int z, long salt) {
        return ((hash(x, z, salt) >>> 11) * 0x1.0p-53) * 2.0D - 1.0D;
    }

    private double value3(int x, int y, int z, long salt) {
        return ((mix(hash(x, z, salt) ^ (y * 0x9E3779B97F4A7C15L)) >>> 11) * 0x1.0p-53) * 2.0D - 1.0D;
    }

    private long hash(int x, int z, long salt) {
        return mix(seed ^ (x * 0x632BE59BD9B4E019L) ^ (z * 0x9E3779B97F4A7C15L) ^ (salt * 0x94D049BB133111EBL));
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static double interpolate(double a, double b, double c, double d,
                                      double e, double f, double g, double h,
                                      double tx, double ty, double tz) {
        return lerp(lerp(lerp(a,b,tx), lerp(c,d,tx), tz),
                lerp(lerp(e,f,tx), lerp(g,h,tx), tz), ty);
    }

    private static double lerp(double a, double b, double t) { return a+(b-a)*t; }
    private static double fade(double t) { return t*t*t*(t*(t*6.0D-15.0D)+10.0D); }
    private static double square(double value) { return value*value; }
    private static int floor(double value) { int i=(int)value; return value<i ? i-1 : i; }
    private static double clamp(double value, double min, double max) { return Math.max(min,Math.min(max,value)); }
    private static double bounded(double value, double min, double max, double fallback) {
        return Double.isNaN(value) || Double.isInfinite(value) ? fallback : clamp(value,min,max);
    }
}
