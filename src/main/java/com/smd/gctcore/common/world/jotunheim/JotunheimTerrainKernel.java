package com.smd.gctcore.common.world.jotunheim;

import java.util.Arrays;

/**
 * Stateless, Minecraft-independent four-layer terrain sampler.
 * All random decisions depend only on the seed, absolute coordinates and salts.
 * Point and chunk APIs share the globally anchored 4x2x4 base interpolation,
 * followed by the same continuous per-column underside shell.
 */
public final class JotunheimTerrainKernel {
    public static final int BIOME_COUNT = 8;
    public static final int GRID_XZ = 4;
    public static final int GRID_Y = 2;
    private static final double EMPTY = -32.0D;
    private static final int RELIEF_BLEND_GRID = 96;
    // Additional downward mass, ordered LAVA, DEEP, SURFACE, CLOUD. Tops stay unchanged.
    private static final double[] UNDERSIDE_DEPTH = {6.5D, 16.0D, 18.0D, 12.0D};
    // Macro amplitude, ridges, sharp escarpments, terraces, caldera, thickness, rolling hills.
    // Forest, frostwaste, highlands, fen, crystal, storm, volcanic, elder wastes.
    private static final double[][] RELIEF_STYLES = {
            {1.02D, 0.12D, 0.05D, 0.00D, 0.00D, 1.00D, 0.60D},
            {1.35D, 0.65D, 0.25D, 0.20D, 0.00D, 1.16D, 0.00D},
            {2.15D, 1.75D, 0.90D, 0.00D, 0.00D, 1.42D, 0.00D},
            {0.52D, 0.00D, 0.00D, 0.48D, 0.00D, 1.05D, 0.28D},
            {1.85D, 1.45D, 0.80D, 0.10D, 0.00D, 1.32D, 0.00D},
            {1.55D, 0.80D, 1.25D, 0.20D, 0.00D, 1.15D, 0.00D},
            {1.75D, 0.90D, 0.45D, 0.00D, 1.85D, 1.38D, 0.00D},
            {1.12D, 0.15D, 0.28D, 0.72D, 0.00D, 1.24D, 0.40D}
    };

    public enum HeightLayer {
        LAVA(5, 30, 20, 4.0D, 12.0D, 176, 0.78D, 0x153A4B29L),
        DEEP(35, 105, 73, 12.0D, 34.0D, 208, 0.83D, 0x25A69F31L),
        SURFACE(115, 190, 154, 14.0D, 25.0D, 256, 0.88D, 0x38A25C17L),
        CLOUD(200, 250, 224, 7.0D, 14.0D, 192, 0.66D, 0x491C7E63L);

        /** Lowest allowed island-body block, including the extended underside; not the surface. */
        public final int minY;
        public final int maxY;
        /** Terrain shaping reference only; digging below it must not change the force-field layer. */
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
     * Eight XZ biomes shared by all height layers. The deepest projected island
     * interior chooses its centre's large-scale biome, pulling most transitions
     * into the surrounding void. Overlapping strata necessarily share one ID.
     * Fine, globally anchored noise roughens the remaining boundary, with no
     * chunk/world reads and no changes to the terrain density field.
     */
    public int biomeIndex(int x, int z) {
        double anchorX = x;
        double anchorZ = z;
        double bestInterior = -6.0D;
        boolean anchored = false;
        double radiusDensity = Math.pow(settings.islandDensity, 0.18D);
        double edgeNoise = noise2(x / 19.0D, z / 19.0D, 7121L) * 2.5D
                + noise2(x / 5.0D, z / 5.0D, 7122L) * 1.25D;
        for (HeightLayer layer : LAYERS) {
            long salt = layer.salt;
            double cell = layer.cellSize * settings.islandScale;
            double wx = x + fbm2(x, z, cell * 0.92D, salt + 1) * cell * 0.13D;
            double wz = z + fbm2(x, z, cell * 0.92D, salt + 2) * cell * 0.13D;
            int cellX = floor(wx / cell);
            int cellZ = floor(wz / cell);
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
                            * radiusDensity;
                    double aspect = value(cx, cz, salt + 7) * 0.18D;
                    double q = 1.0D - Math.sqrt(square((wx-px)/(radius*(1.0D+aspect)))
                            + square((wz-pz)/(radius*(1.0D-aspect))));
                    double interior = (q + outline) * radius + edgeNoise * value(cx, cz, salt + 9);
                    if (interior > bestInterior) {
                        bestInterior = interior;
                        anchored = true;
                        anchorX = px;
                        anchorZ = pz;
                    }
                }
            }
        }
        return regionBiome(anchorX, anchorZ, !anchored);
    }

    private int regionBiome(double x, double z, boolean roughen) {
        // Reserve room for an island across a region seam instead of cutting it in half.
        double scale = settings.biomeScale + 160.0D * settings.islandScale;
        double wx = x + fbm2(x, z, scale * 1.6D, 7101L) * scale * 0.24D;
        double wz = z + fbm2(x, z, scale * 1.6D, 7102L) * scale * 0.24D;
        if (roughen) {
            wx += noise2(x / 28.0D, z / 28.0D, 7131L) * 16.0D
                    + noise2(x / 7.0D, z / 7.0D, 7132L) * 6.0D;
            wz += noise2(x / 28.0D, z / 28.0D, 7133L) * 16.0D
                    + noise2(x / 7.0D, z / 7.0D, 7134L) * 6.0D;
        }
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

    /** Exact point sample of the base interpolation and underside shell used by chunks. */
    public double density(int x, int y, int z) {
        if (y < 0 || y > 255 || layerAt(y) == null) return EMPTY;
        int gx = Math.floorDiv(x, GRID_XZ) * GRID_XZ;
        int gz = Math.floorDiv(z, GRID_XZ) * GRID_XZ;
        int gy = Math.floorDiv(y, GRID_Y) * GRID_Y;
        double tx = (x - gx) / (double) GRID_XZ;
        double ty = (y - gy) / (double) GRID_Y;
        double tz = (z - gz) / (double) GRID_XZ;
        double[] values = new double[8];
        LayerColumn[][] corners = new LayerColumn[4][];
        for (int dz = 0; dz <= 1; dz++) {
            for (int dx = 0; dx <= 1; dx++) {
                LayerColumn[] column = columns(gx + dx * GRID_XZ, gz + dz * GRID_XZ);
                corners[dz * 2 + dx] = column;
                int i = (dz * 2 + dx) * 2;
                values[i] = rawDensity(column, gx + dx * GRID_XZ, gy, gz + dz * GRID_XZ);
                values[i + 1] = rawDensity(column, gx + dx * GRID_XZ, gy + GRID_Y, gz + dz * GRID_XZ);
            }
        }
        double original = interpolate(values[0], values[2], values[4], values[6],
                values[1], values[3], values[5], values[7], tx, ty, tz);
        if (original > 0.0D) return original;
        HeightLayer layer = layerAt(y);
        int bottom = originalBottom(corners, layer, gx, gz, tx, tz);
        if (bottom < 0 || y >= bottom) return original;
        double lower = extendedBottom(corners, layer, x, z, tx, tz, bottom);
        return y > lower ? Math.max(original, y-lower) : original;
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
        LayerColumn[][] corners = new LayerColumn[4][];
        for (int dz = 0; dz <= 1; dz++) {
            for (int dx = 0; dx <= 1; dx++) {
                int wx = gx + dx * GRID_XZ;
                int wz = gz + dz * GRID_XZ;
                LayerColumn[] column = columns(wx, wz);
                corners[dz * 2 + dx] = column;
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
        for (HeightLayer layer : LAYERS) {
            int bottom = -1;
            for (int y = layer.minY; y <= layer.maxY; y++) if (solid[y]) { bottom = y; break; }
            if (bottom < 0) continue;
            double lower = extendedBottom(corners, layer, x, z, tx, tz, bottom);
            for (int y = bottom - 1; y > lower; y--) solid[y] = true;
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
        LayerColumn[][][] profiles = new LayerColumn[5][5][];
        int startX = chunkX * 16;
        int startZ = chunkZ * 16;
        for (int ix = 0; ix <= 4; ix++) {
            for (int iz = 0; iz <= 4; iz++) {
                int x = startX + ix * GRID_XZ;
                int z = startZ + iz * GRID_XZ;
                LayerColumn[] column = columns(x, z);
                profiles[ix][iz] = column;
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
                // Append only below the original lowest solid in each occupied column.
                // Existing surfaces, caves, density samples and projected coastlines are untouched.
                LayerColumn[][] corners = {profiles[ix][iz], profiles[ix+1][iz],
                        profiles[ix][iz+1], profiles[ix+1][iz+1]};
                for (HeightLayer layer : LAYERS) {
                    int bottom = -1;
                    for (int y = layer.minY; y <= layer.maxY; y++) {
                        if (result.isSolid(x, y, z)) { bottom = y; break; }
                    }
                    if (bottom < 0) continue;
                    double lower = extendedBottom(corners, layer, startX+x, startZ+z, tx, tz, bottom);
                    for (int y = bottom-1; y > lower; y--) {
                        int index = (x * 16 + z) * 256 + y;
                        result.densities[index] = (float) Math.max(result.densities[index], y-lower);
                    }
                }
            }
        }
        return result;
    }

    /** Lowest solid of the unmodified field, evaluated only near its possible lower envelope. */
    private int originalBottom(LayerColumn[][] corners, HeightLayer layer,
                               int gx, int gz, double tx, double tz) {
        double minBottom = Double.POSITIVE_INFINITY, maxTop = -Double.MAX_VALUE, maxSide = -Double.MAX_VALUE;
        for (LayerColumn[] corner : corners) {
            LayerColumn c = corner[layer.ordinal()];
            minBottom = Math.min(minBottom, c.bottom);
            maxTop = Math.max(maxTop, c.top);
            maxSide = Math.max(maxSide, c.side);
        }
        double erosion = 1.35D * settings.erosion * (layer == HeightLayer.DEEP ? 2.6D : 1.9D);
        if (maxSide < -erosion) return -1;
        int first = Math.max(layer.minY, floor(minBottom-erosion)-GRID_Y);
        int last = Math.min(layer.maxY, (int)Math.ceil(maxTop+erosion)+GRID_Y);
        int gy = Math.floorDiv(first, GRID_Y) * GRID_Y;
        double low = originalGridDensity(corners, gx, gy, gz, tx, tz);
        for (; gy <= last; gy += GRID_Y) {
            double high = originalGridDensity(corners, gx, gy+GRID_Y, gz, tx, tz);
            for (int dy = 0; dy < GRID_Y; dy++) {
                int y = gy+dy;
                if (y >= first && y <= last && lerp(low, high, dy/(double)GRID_Y) > 0.0D) return y;
            }
            low = high;
        }
        return -1;
    }

    private double originalGridDensity(LayerColumn[][] corners, int gx, int y, int gz, double tx, double tz) {
        return lerp(lerp(rawDensity(corners[0], gx, y, gz), rawDensity(corners[1], gx+GRID_XZ, y, gz), tx),
                lerp(rawDensity(corners[2], gx, y, gz+GRID_XZ),
                        rawDensity(corners[3], gx+GRID_XZ, y, gz+GRID_XZ), tx), tz);
    }

    /**
     * A connected lower shell: broad hanging masses, diagonal rock ribs and smaller
     * tapered pendants. All fields are continuous in world XZ, never per-column RNG.
     * Smoothly running out of room above the stratum floor avoids a clipped flat base.
     */
    private double extendedBottom(LayerColumn[][] corners, HeightLayer layer, int x, int z,
                                  double tx, double tz, int originalBottom) {
        int index = layer.ordinal();
        double side = lerp(lerp(corners[0][index].side, corners[1][index].side, tx),
                lerp(corners[2][index].side, corners[3][index].side, tx), tz);
        double interior = 0.20D + 0.80D * fade(clamp((side+1.5D)/24.0D, 0.0D, 1.0D));
        double scale = layer.cellSize * settings.islandScale;
        long salt = layer.salt;
        double bulk = 0.5D + 0.5D * fbm2(x, z, scale*0.32D, salt+51L);
        double ribs = Math.pow(Math.max(0.0D, 1.0D-Math.abs(noise2(
                (x+z*0.38D)/(scale*0.15D), (z-x*0.25D)/(scale*0.30D), salt+52L))*1.8D), 3.0D);
        double pendants = Math.pow(clamp(0.5D+0.5D*noise2(
                x/(scale*0.075D), z/(scale*0.075D), salt+53L), 0.0D, 1.0D), 2.5D);
        double requested = UNDERSIDE_DEPTH[index] * settings.thickness * interior
                * (0.40D + bulk*0.60D + ribs*0.62D + pendants*0.70D);
        double reserve = 2.2D + noise2(x/(scale*0.14D), z/(scale*0.14D), salt+54L);
        double room = Math.max(0.0D, originalBottom-layer.minY-reserve);
        return originalBottom - (room == 0.0D ? 0.0D : room * (1.0D-Math.exp(-requested/room)));
    }

    private LayerColumn[] columns(int x, int z) {
        LayerColumn[] result = new LayerColumn[LAYERS.length];
        // Interpolate morphology independently from hard biome IDs: a jagged material
        // boundary must never turn into a density discontinuity or a chunk seam.
        double[] style = reliefStyle(x, z);
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
            double ridge = 1.0D - Math.abs(fbm2(x, z, cell * 0.24D, salt + 41)) * 1.6D;
            ridge = square(Math.max(0.0D, ridge)) - 0.46D;
            double cliff = Math.tanh(noise2(x / (cell * 0.24D), z / (cell * 0.24D), salt + 42) * 5.0D);
            double shaped = relief * style[0] + ridge * style[1] + cliff * style[2] * 0.40D;
            // Broad, smooth hummocks give the gentler biomes local relief without
            // introducing the sharp ridge/cliff profile used by mountainous regions.
            double hills = noise2(x / (cell * 0.28D), z / (cell * 0.28D), salt + 43) * 0.80D
                    + noise2(x / (cell * 0.53D), z / (cell * 0.53D), salt + 44) * 0.20D;
            shaped += hills * style[6];
            shaped = lerp(shaped, terrace(shaped, 0.32D), style[3]);
            double distance = 1.0D - clamp(bestQ, 0.0D, 1.0D);
            double caldera = Math.exp(-square((distance - 0.43D) / 0.20D)) * 0.95D
                    - Math.pow(clamp(bestQ, 0.0D, 1.0D), 6.0D) * 1.05D - 0.12D;
            shaped += caldera * style[4];
            double top = layer.typicalSurface + shaped * layer.variation * settings.heightVariation;
            top -= square(1.0D - clamp(bestQ, 0.0D, 1.0D)) * (layer == HeightLayer.DEEP ? 6.0D : 4.0D);
            // Cloud geometry stays <=232; fine erosion can add a block. Tree plans
            // adapt to the actual remaining room beneath the cloud layer's ceiling.
            top = boundedRelief(top, layer.typicalSurface, layer.minY + 8.0D,
                    layer == HeightLayer.CLOUD ? 232.0D : layer.maxY - 4.0D);
            double thick = layer.thickness * settings.thickness
                    * (0.87D + fbm2(x, z, cell * 0.55D, salt + 13) * 0.27D) * style[5];
            thick *= 0.12D + 0.88D * Math.pow(clamp(bestQ, 0.0D, 1.0D), 0.6D);
            thick = Math.min(thick + 1.8D, top - layer.minY - 1.0D);
            result[layer.ordinal()] = new LayerColumn(layer, bestQ * bestRadius * 0.48D, top, top - thick);
        }
        return result;
    }

    private double[] reliefStyle(int x, int z) {
        int gx = Math.floorDiv(x, RELIEF_BLEND_GRID) * RELIEF_BLEND_GRID;
        int gz = Math.floorDiv(z, RELIEF_BLEND_GRID) * RELIEF_BLEND_GRID;
        double tx = fade((x - gx) / (double) RELIEF_BLEND_GRID);
        double tz = fade((z - gz) / (double) RELIEF_BLEND_GRID);
        double[] a = RELIEF_STYLES[biomeIndex(gx, gz)];
        double[] b = RELIEF_STYLES[biomeIndex(gx + RELIEF_BLEND_GRID, gz)];
        double[] c = RELIEF_STYLES[biomeIndex(gx, gz + RELIEF_BLEND_GRID)];
        double[] d = RELIEF_STYLES[biomeIndex(gx + RELIEF_BLEND_GRID, gz + RELIEF_BLEND_GRID)];
        double[] result = new double[7];
        for (int i = 0; i < result.length; i++) result[i] = lerp(lerp(a[i], b[i], tx), lerp(c[i], d[i], tx), tz);
        return result;
    }

    /** Continuous shelves joined by a narrow smooth ramp; no rounded-height steps. */
    private static double terrace(double height, double step) {
        double scaled = height / step;
        double shelf = Math.floor(scaled);
        double ramp = clamp((scaled - shelf - 0.58D) / 0.42D, 0.0D, 1.0D);
        return (shelf + fade(ramp)) * step;
    }

    /** Smooth saturation retains relief near a layer's limit instead of clipping flat caps. */
    private static double boundedRelief(double height, double centre, double low, double high) {
        double delta = height - centre;
        double span = delta < 0.0D ? centre - low : high - centre;
        return centre + Math.signum(delta) * span * (1.0D - Math.exp(-Math.abs(delta) / span));
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
