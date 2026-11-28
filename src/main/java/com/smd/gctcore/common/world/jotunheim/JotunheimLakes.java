package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.misc.JotunheimBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.smd.gctcore.common.world.jotunheim.JotunheimTerrainKernel.HeightLayer;

/** Seeded island basins, replayed into individual primers without reading world chunks. */
public final class JotunheimLakes {
    private static final int WATER_CELL = 160;
    private static final int LAVA_CELL = 56;
    private final long seed;
    private final JotunheimTerrainKernel kernel;
    private final JotunheimYmirWells wells;
    private final Map<Long, JotunheimTerrainKernel.SampledChunk> terrain = cache(48);
    private final Map<Long, List<Lake>> waterPlans = cache(128);
    private final Map<Long, List<Lake>> lavaPlans = cache(192);

    public JotunheimLakes(long seed, JotunheimTerrainKernel kernel) {
        this(seed, kernel, null);
    }

    public JotunheimLakes(long seed, JotunheimTerrainKernel kernel, JotunheimYmirWells wells) {
        this.seed = seed;
        this.kernel = kernel;
        this.wells = wells;
    }

    private static <K, V> Map<K, V> cache(final int limit) {
        return new LinkedHashMap<K, V>(limit, 0.75F, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<K, V> entry) { return size() > limit; }
        };
    }

    /** Each basin fits inside its planning cell, but may span several real chunks. */
    public synchronized void decorate(ChunkPrimer primer, int chunkX, int chunkZ,
                                      JotunheimTerrainKernel.SampledChunk sampled) {
        terrain.put(key(chunkX, chunkZ), sampled);
        int x0 = chunkX * 16, z0 = chunkZ * 16;
        for (boolean lava : new boolean[] {false, true}) {
            int cell = lava ? LAVA_CELL : WATER_CELL;
            for (int cx = Math.floorDiv(x0, cell); cx <= Math.floorDiv(x0 + 15, cell); cx++) {
                for (int cz = Math.floorDiv(z0, cell); cz <= Math.floorDiv(z0 + 15, cell); cz++) {
                    for (Lake lake : plans(cx, cz, lava)) {
                        for (Column column : lake.columns) {
                            if (column == null || column.x < x0 || column.x >= x0 + 16
                                    || column.z < z0 || column.z >= z0 + 16) continue;
                            int x = column.x - x0, z = column.z - z0;
                            // Two solid layers beneath every wet column seal it from caves and the void.
                            for (int y = column.floorY - 1; y <= column.floorY; y++) {
                                primer.setBlockState(x, y, z, lake.bed);
                            }
                            if (column.fluid) {
                                for (int y = column.floorY + 1; y <= lake.waterY; y++) {
                                    primer.setBlockState(x, y, z, lake.fluid);
                                }
                            } else {
                                for (int y = column.floorY + 1; y <= column.shoreY; y++) {
                                    primer.setBlockState(x, y, z, lake.bed);
                                }
                            }
                            int clearFrom = column.fluid ? lake.waterY + 1 : column.shoreY + 1;
                            for (int y = clearFrom; y <= column.clearTop; y++) {
                                primer.setBlockState(x, y, z, Blocks.AIR.getDefaultState());
                            }
                        }
                    }
                }
            }
        }
    }

    /** Prevents trees and older little pools from using terrain removed by a basin. */
    public synchronized boolean blocksVegetation(int x, int y, int z) {
        for (boolean lava : new boolean[] {false, true}) {
            int cell = lava ? LAVA_CELL : WATER_CELL;
            for (Lake lake : plans(Math.floorDiv(x, cell), Math.floorDiv(z, cell), lava)) {
                Column column = lake.column(x, z);
                if (column != null && y >= column.floorY - 1 && y <= column.clearTop + 1) return true;
            }
        }
        return false;
    }

    /** Package access permits deterministic generation checks without constructing a World. */
    synchronized List<Lake> plans(int cx, int cz, boolean lava) {
        Map<Long, List<Lake>> cache = lava ? lavaPlans : waterPlans;
        long id = key(cx, cz);
        List<Lake> result = cache.get(id);
        if (result != null) return result;
        result = new ArrayList<>();
        int cell = lava ? LAVA_CELL : WATER_CELL;
        for (HeightLayer layer : HeightLayer.values()) {
            if (!lava && layer != HeightLayer.SURFACE) continue;
            Random random = random(cx, cz, (lava ? 9301L : 9101L) + layer.ordinal() * 101L);
            if (random.nextDouble() > 0.62D) continue;
            int margin = lava ? 12 : 34;
            for (int attempt = 0; attempt < (lava ? 2 : 3); attempt++) {
                int x = cx * cell + margin + random.nextInt(cell - margin * 2);
                int z = cz * cell + margin + random.nextInt(cell - margin * 2);
                int biome = kernel.biomeIndex(x, z);
                if (lava ? biome != 6 : biome == 6) continue;
                Lake lake = create(x, z, layer, lava, random);
                if (lake != null) { result.add(lake); break; }
            }
        }
        result = Collections.unmodifiableList(result);
        cache.put(id, result);
        return result;
    }

    private Lake create(int x, int z, HeightLayer layer, boolean lava, Random random) {
        int centerTop = top(x, z, layer);
        if (centerTop < layer.minY + (lava ? 6 : 11)) return null;
        double radiusX = lava ? 3.3D + random.nextDouble() * 2.5D : 16.0D + random.nextDouble() * 9.0D;
        double radiusZ = radiusX * (0.68D + random.nextDouble() * 0.32D);
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double phase = random.nextDouble() * Math.PI * 2.0D;
        int bound = (int) Math.ceil(radiusX * 1.13D) + 3;
        if (overlapsWell(x, z, bound, layer)) return null;
        int width = bound * 2 + 1;
        double[] distances = new double[width * width];
        int[] tops = new int[distances.length];
        int minTop = centerTop, maxTop = centerTop;
        int wetCount = 0;
        for (int dz = -bound; dz <= bound; dz++) for (int dx = -bound; dx <= bound; dx++) {
            int index = (dz + bound) * width + dx + bound;
            double rx = dx * Math.cos(angle) + dz * Math.sin(angle);
            double rz = -dx * Math.sin(angle) + dz * Math.cos(angle);
            double theta = Math.atan2(rz / radiusZ, rx / radiusX);
            double lobes = 1.0D + Math.sin(theta * 3.0D + phase) * 0.085D
                    + Math.sin(theta * 5.0D - phase * 0.7D) * 0.035D;
            double distance = Math.sqrt(rx * rx / (radiusX * radiusX)
                    + rz * rz / (radiusZ * radiusZ)) / lobes;
            distances[index] = distance;
            // The shoreline is a real two-block-wide ring, not a vertical invisible fluid wall.
            if (distance > 1.0D + 2.3D / radiusZ) continue;
            int wx = x + dx, wz = z + dz;
            if (lava != (kernel.biomeIndex(wx, wz) == 6)) return null;
            int top = top(wx, wz, layer);
            if (top < layer.minY + (lava ? 6 : 11)) return null;
            tops[index] = top;
            minTop = Math.min(minTop, top);
            maxTop = Math.max(maxTop, top);
            if (distance <= 1.0D) wetCount++;
        }
        if (maxTop - minTop > (lava ? 10 : 18) || wetCount < (lava ? 20 : 450)) return null;
        int waterY = minTop - 1;
        Lake lake = new Lake(x - bound, z - bound, width, waterY, layer, lava);
        for (int dz = -bound; dz <= bound; dz++) for (int dx = -bound; dx <= bound; dx++) {
            int index = (dz + bound) * width + dx + bound;
            if (tops[index] == 0) continue;
            double distance = distances[index];
            boolean wet = distance <= 1.0D;
            int depth = wet ? (lava ? 1 : 2) + (int) ((lava ? 2.2D : 5.0D)
                    * Math.max(0.0D, 1.0D - distance * distance)) : 0;
            int floorY = waterY - depth;
            int wx = x + dx, wz = z + dz;
            // Validate the full shell before modifying any primer, including caves under a lake.
            for (int y = floorY - 1; y <= waterY; y++) {
                if (!solid(wx, y, wz)) return null;
            }
            double bank = Math.max(0.0D, Math.min(1.0D, (distance - 1.0D) * radiusZ / 2.3D));
            bank = bank * bank * (3.0D - 2.0D * bank);
            int shoreY = wet ? waterY : waterY + 1
                    + (int) Math.round((tops[index] - waterY - 1) * bank);
            lake.columns[index] = new Column(wx, wz, floorY, shoreY, tops[index], wet);
        }
        return lake;
    }

    private boolean overlapsWell(int x, int z, int radius, HeightLayer layer) {
        if (wells == null) return false;
        int region = JotunheimYmirWells.REGION_CHUNKS * 16;
        // Include the exterior approaches, not just the main shaft.
        int margin = radius + 16;
        for (int rx = Math.floorDiv(x - margin, region); rx <= Math.floorDiv(x + margin, region); rx++) {
            for (int rz = Math.floorDiv(z - margin, region); rz <= Math.floorDiv(z + margin, region); rz++) {
                JotunheimYmirWells.Plan well = wells.planForRegion(rx, rz);
                if (well != null && Math.abs(well.x - x) <= margin && Math.abs(well.z - z) <= margin
                        && layer.maxY >= well.lowerFloorY - 8 && layer.minY <= well.upperFloorY + 8) return true;
            }
        }
        return false;
    }

    private JotunheimTerrainKernel.SampledChunk terrain(int x, int z) {
        int cx = Math.floorDiv(x, 16), cz = Math.floorDiv(z, 16);
        long id = key(cx, cz);
        JotunheimTerrainKernel.SampledChunk chunk = terrain.get(id);
        if (chunk == null) { chunk = kernel.sampleChunk(cx, cz); terrain.put(id, chunk); }
        return chunk;
    }

    private int top(int x, int z, HeightLayer layer) {
        JotunheimTerrainKernel.SampledChunk chunk = terrain(x, z);
        for (int y = layer.maxY; y >= layer.minY; y--) {
            if (chunk.isSolid(Math.floorMod(x, 16), y, Math.floorMod(z, 16))) return y;
        }
        return -1;
    }

    private boolean solid(int x, int y, int z) {
        return terrain(x, z).isSolid(Math.floorMod(x, 16), y, Math.floorMod(z, 16));
    }

    static final class Lake {
        final int minX, minZ, width, waterY;
        final HeightLayer layer;
        final boolean lava;
        final IBlockState fluid, bed;
        final Column[] columns;

        Lake(int minX, int minZ, int width, int waterY, HeightLayer layer, boolean lava) {
            this.minX = minX; this.minZ = minZ; this.width = width; this.waterY = waterY;
            this.layer = layer; this.lava = lava;
            fluid = (lava ? Blocks.LAVA : Blocks.WATER).getDefaultState();
            bed = JotunheimBlocks.state(lava ? "volcanic_rock" : "humus");
            columns = new Column[width * width];
        }

        Column column(int x, int z) {
            int dx = x - minX, dz = z - minZ;
            return dx < 0 || dz < 0 || dx >= width || dz >= width ? null : columns[dz * width + dx];
        }
    }

    static final class Column {
        final int x, z, floorY, shoreY, clearTop;
        final boolean fluid;
        Column(int x, int z, int floorY, int shoreY, int clearTop, boolean fluid) {
            this.x = x; this.z = z; this.floorY = floorY; this.shoreY = shoreY;
            this.clearTop = clearTop; this.fluid = fluid;
        }
    }

    private Random random(int x, int z, long salt) {
        long value = seed ^ ((long) x * 0x632BE59BD9B4E019L) ^ ((long) z * 0x9E3779B97F4A7C15L)
                ^ (salt * 0x94D049BB133111EBL);
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return new Random(value ^ (value >>> 31));
    }

    private static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xFFFFFFFFL); }
}
