package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.misc.YmirBlocks;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Ymir's Hell: an open crown, repeating sixteen-block shaft and sealed foot.
 * Plans sample only the terrain kernel; placement touches only the new chunk's
 * primer. Neither exploration order nor neighbouring loaded chunks affect it.
 */
public final class JotunheimYmirWells {
    public static final String STRUCTURE_NAME = "YmirHell";
    public static final int REGION_CHUNKS = 32;
    public static final int RADIUS = 13;
    private static final int REGION_BLOCKS = REGION_CHUNKS * 16;
    private static final int CANDIDATES = 16;
    private static final int[] DX = {0, 1, 0, -1};
    private static final int[] DZ = {-1, 0, 1, 0};
    private static final EnumFacing[] FACING = {EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};
    private static final JotunheimTerrainKernel.HeightLayer[] LAYERS =
            JotunheimTerrainKernel.HeightLayer.values();

    /** Ground block heights, not feet heights. Exits are N, E, S, W. */
    public static final class Plan {
        public final int x, z, lowerFloorY, upperFloorY, lowerExit, upperExit;
        public final JotunheimTerrainKernel.HeightLayer lowerLayer, upperLayer;
        final int foundationY, lowerDrop, upperDrop;

        private Plan(int x, int z, int lower, int upper, int foundation, int pair,
                     int lowerExit, int upperExit, int lowerDrop, int upperDrop) {
            this.x = x;
            this.z = z;
            lowerFloorY = lower;
            upperFloorY = upper;
            foundationY = foundation;
            lowerLayer = LAYERS[pair];
            upperLayer = LAYERS[pair + 1];
            this.lowerExit = lowerExit;
            this.upperExit = upperExit;
            this.lowerDrop = lowerDrop;
            this.upperDrop = upperDrop;
        }

        public boolean contains(int worldX, int y, int worldZ) {
            if (y < 0 || y > 255) return false;
            int dx = worldX - x, dz = worldZ - z;
            if (octagon(dx, dz, 12, 18)) return y >= foundationY && y <= upperFloorY + 8;
            return (inEntrance(dx, dz, lowerExit) && y >= lowerFloorY - lowerDrop - 2
                    && y <= lowerFloorY + 5)
                    || (inEntrance(dx, dz, upperExit) && y >= upperFloorY - upperDrop - 2
                    && y <= upperFloorY + 5);
        }

        public BlockPos entrance() {
            return new BlockPos(x + DX[upperExit] * 11, upperFloorY + 1,
                    z + DZ[upperExit] * 11);
        }

        /** Walkable interior, crown gallery and three-wide approaches, excluding the outer facade. */
        public boolean shelters(BlockPos pos) {
            if (!contains(pos.getX(), pos.getY(), pos.getZ())) return false;
            int dx = pos.getX() - x, dz = pos.getZ() - z, y = pos.getY();
            if (octagon(dx, dz, 7, 11) && y > lowerFloorY && y <= upperFloorY + 6) return true;
            if (y > upperFloorY && y <= upperFloorY + 6 && octagon(dx, dz, 12, 18)) return true;
            return (inEntrance(dx, dz, lowerExit) && y > lowerFloorY - lowerDrop && y <= lowerFloorY + 6)
                    || (inEntrance(dx, dz, upperExit) && y > upperFloorY - upperDrop && y <= upperFloorY + 6);
        }
    }

    private final long seed;
    private final JotunheimTerrainKernel kernel;
    // Cache failures too. No terrain/chunk data is retained by a plan.
    private final Map<Long, java.util.List<Plan>> plans = new LinkedHashMap<Long, java.util.List<Plan>>(128, .75F, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, java.util.List<Plan>> entry) {
            return size() > 256;
        }
    };
    private final IBlockState air = Blocks.AIR.getDefaultState();
    private final IBlockState stone = YmirBlocks.state("ymir_stone");
    private final IBlockState bricks = YmirBlocks.state("ymir_stone_bricks");
    private final IBlockState cracked = YmirBlocks.state("cracked_ymir_stone_bricks");
    private final IBlockState darkStone = YmirBlocks.state("dark_ymir_stone");
    private final IBlockState darkBricks = YmirBlocks.state("dark_ymir_stone_bricks");
    private final IBlockState darkCracked = YmirBlocks.state("cracked_dark_ymir_stone_bricks");
    private final IBlockState chiseled = YmirBlocks.state("chiseled_ymir_stone");
    private final IBlockState darkChiseled = YmirBlocks.state("chiseled_dark_ymir_stone");
    private final IBlockState pillar = YmirBlocks.state("dark_ymir_stone_pillar");
    private final IBlockState slab = YmirBlocks.state("ymir_stone_slab")
            .withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM);
    private final IBlockState wall = YmirBlocks.state("dark_ymir_stone_wall");
    private final IBlockState stairs = YmirBlocks.state("ymir_stone_stairs");
    private final IBlockState darkStairs = YmirBlocks.state("dark_ymir_stone_stairs");
    private final IBlockState darkSlab = YmirBlocks.state("dark_ymir_stone_slab")
            .withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM);

    public JotunheimYmirWells(long seed, JotunheimTerrainKernel kernel) {
        this.seed = seed;
        this.kernel = kernel;
    }

    public static boolean acceptsName(String name) {
        return STRUCTURE_NAME.equalsIgnoreCase(name) || "ymir_hell".equalsIgnoreCase(name)
                || "gctcore:ymir_hell".equalsIgnoreCase(name);
    }

    /** At most one well per 1024-chunk region; no eligible overlap means no well. */
    @Nullable
    public synchronized Plan planForRegion(int regionX, int regionZ) {
        long key = key(regionX, regionZ);
        java.util.List<Plan> cached = plans.get(key);
        if (cached != null) return cached.isEmpty() ? null : cached.get(0);
        Random random = new Random(mix(seed ^ key ^ 0x594D495248454C4CL));
        Plan result = null;
        for (int attempt = 0; attempt < CANDIDATES && result == null; attempt++) {
            // All masonry/entrances fit within the owning region, including at negative X/Z.
            int x = regionX * REGION_BLOCKS + 32 + random.nextInt(REGION_BLOCKS - 64);
            int z = regionZ * REGION_BLOCKS + 32 + random.nextInt(REGION_BLOCKS - 64);
            int firstPair = random.nextInt(3);
            for (int offset = 0; offset < 3 && result == null; offset++)
                result = planAt(x, z, (firstPair + offset) % 3);
        }
        plans.put(key, result == null ? Collections.emptyList() : Collections.singletonList(result));
        return result;
    }

    /** Tests exactly this adjacent pair. An absent layer is never skipped. */
    @Nullable
    Plan planAt(int x, int z, int pair) {
        if (pair < 0 || pair >= 3) return null;
        int[] center = highestSurfaces(kernel.surfaceHeights(x, z));
        if (center[pair] < 0 || center[pair + 1] < 0) return null;
        // Cheap footprint rejection before allocating full sampled chunks.
        for (int dz = -6; dz <= 6; dz += 6) {
            for (int dx = -6; dx <= 6; dx += 6) {
                if (dx == 0 && dz == 0) continue;
                int[] heights = highestSurfaces(kernel.surfaceHeights(x + dx, z + dz));
                if (heights[pair] < 0 || heights[pair + 1] < 0) return null;
                if (Math.abs(heights[pair] - center[pair]) > 7
                        || Math.abs(heights[pair + 1] - center[pair + 1]) > 7) return null;
            }
        }
        Terrain terrain = new Terrain();
        int lowMin = 256, lowMax = -1, highMin = 256, highMax = -1;
        for (int dz = -9; dz <= 9; dz++) {
            for (int dx = -9; dx <= 9; dx++) {
                if (!octagon(dx, dz, 9, 14)) continue;
                int[] heights = terrain.heights(x + dx, z + dz);
                int lower = heights[pair], upper = heights[pair + 1];
                if (lower < 0 || upper < 0) return null;
                lowMin = Math.min(lowMin, lower);
                lowMax = Math.max(lowMax, lower);
                highMin = Math.min(highMin, upper);
                highMax = Math.max(highMax, upper);
                if (lowMax - lowMin > 7 || highMax - highMin > 7) return null;
            }
        }
        if (highMax - lowMax < 20 || highMax + 5 > 255) return null;
        int firstExit = (int) (mix(seed ^ key(x, z)) & 3);
        int[] lowerExit = entrance(terrain, x, z, pair, lowMax, firstExit);
        int[] upperExit = entrance(terrain, x, z, pair + 1, highMax, (firstExit + 2) % 4);
        if (lowerExit == null || upperExit == null) return null;
        return new Plan(x, z, lowMax, highMax, Math.max(1, lowMin - 2), pair,
                lowerExit[0], upperExit[0], lowerExit[1], upperExit[1]);
    }

    @Nullable
    private int[] entrance(Terrain terrain, int x, int z, int layer, int floor, int first) {
        for (int side = 0; side < 4; side++) {
            int direction = (first + side) % 4;
            int endMin = 256, endMax = -1;
            boolean valid = true;
            for (int r = 10; r <= 14 && valid; r++) {
                for (int w = -1; w <= 1; w++) {
                    int[] heights = terrain.heights(x + DX[direction] * r - DZ[direction] * w,
                            z + DZ[direction] * r + DX[direction] * w);
                    int ground = heights[layer];
                    if (ground < floor - 3 || ground > floor + 1) { valid = false; break; }
                    if (r >= 13) { endMin = Math.min(endMin, ground); endMax = Math.max(endMax, ground); }
                }
            }
            if (valid && endMax - endMin <= 1 && endMax <= floor && endMin >= floor - 2)
                return new int[] {direction, floor - endMax};
        }
        return null;
    }

    /** Ephemeral exact-footprint sampler: a few chunks, never world chunk loads. */
    private final class Terrain {
        private final Map<Long, JotunheimTerrainKernel.SampledChunk> sampled = new HashMap<>();
        private final Map<Long, int[]> columns = new HashMap<>();

        int[] heights(int x, int z) {
            long columnKey = key(x, z);
            int[] result = columns.get(columnKey);
            if (result != null) return result;
            int cx = Math.floorDiv(x, 16), cz = Math.floorDiv(z, 16);
            JotunheimTerrainKernel.SampledChunk chunk = sampled.computeIfAbsent(key(cx, cz),
                    ignored -> kernel.sampleChunk(cx, cz));
            result = highestSurfaces(chunk.surfaceHeights(Math.floorMod(x, 16), Math.floorMod(z, 16)));
            columns.put(columnKey, result);
            return result;
        }
    }

    private static int[] highestSurfaces(int[] feetHeights) {
        int[] result = {-1, -1, -1, -1};
        for (int feet : feetHeights) {
            int ground = feet - 1;
            JotunheimTerrainKernel.HeightLayer layer = JotunheimTerrainKernel.layerAt(ground);
            if (layer != null) result[layer.ordinal()] = Math.max(result[layer.ordinal()], ground);
        }
        return result;
    }

    public synchronized void decorate(ChunkPrimer primer, int chunkX, int chunkZ) {
        Plan plan = planForRegion(Math.floorDiv(chunkX, REGION_CHUNKS), Math.floorDiv(chunkZ, REGION_CHUNKS));
        if (plan == null || chunkX * 16 > plan.x + RADIUS || chunkX * 16 + 15 < plan.x - RADIUS
                || chunkZ * 16 > plan.z + RADIUS || chunkZ * 16 + 15 < plan.z - RADIUS) return;
        int maxY = Math.min(255, plan.upperFloorY + 8);
        for (int lx = 0; lx < 16; lx++) {
            int dx = chunkX * 16 + lx - plan.x;
            for (int lz = 0; lz < 16; lz++) {
                int dz = chunkZ * 16 + lz - plan.z;
                for (int y = plan.foundationY; y <= maxY; y++) {
                    IBlockState state = blockAt(plan, dx, y, dz);
                    if (state != null) primer.setBlockState(lx, y, lz, state);
                }
            }
        }
    }

    /** Null means preserve the natural block. Coordinates X/Z are relative to the shaft. */
    @Nullable
    IBlockState blockAt(Plan p, int x, int y, int z) {
        if (y < 0 || y > 255 || y < p.foundationY || y > p.upperFloorY + 8) return null;
        boolean outer = octagon(x, z, 9, 14);
        if (!outer) {
            // The three-wide approach and its headroom always take priority over ornament.
            IBlockState lower = entranceBlock(p, x, y, z, false);
            if (lower != null) return lower;
            IBlockState upper = entranceBlock(p, x, y, z, true);
            return upper != null ? upper : exteriorBlock(p, x, y, z);
        }
        if (y < p.foundationY || y > p.upperFloorY + 8) return null;
        if (y <= p.lowerFloorY) return y == p.lowerFloorY ? darkBricks : darkStone;
        boolean inner = octagon(x, z, 7, 11);
        if (!inner) {
            if (door(x, z, p.lowerExit) && y <= p.lowerFloorY + 5) return air;
            if (door(x, z, p.upperExit) && y > p.upperFloorY && y <= p.upperFloorY + 5) return air;
            if (y > p.upperFloorY + 3) return null;
            if (y > p.upperFloorY) {
                int along = Math.min(Math.abs(x), Math.abs(z));
                if (y == p.upperFloorY + 1) return darkBricks;
                // Broad merlons rather than a solid, level rim.
                if (Math.floorMod(along, 4) < 2)
                    return y == p.upperFloorY + 3 ? slab : chiseled;
                return air;
            }
            return masonry(p, x, y, z);
        }
        if (y > p.upperFloorY + 1) return air;
        int index = stepIndex(x, z);
        int totalHalfSteps = 2 * (p.upperFloorY - p.lowerFloorY);
        int lastHere = index < 0 ? -1 : index + 32 * Math.floorDiv(totalHalfSteps - index, 32);
        int remaining = index < 0 ? -1 : totalHalfSteps - lastHere;
        boolean stairOpening = remaining > 0 && remaining < 8;
        // Top ring excludes the final rising flight, leaving real headroom below its opening.
        if (y == p.upperFloorY && Math.max(Math.abs(x), Math.abs(z)) >= 3 && !stairOpening)
            return darkBricks;
        if (y == p.upperFloorY + 1 && Math.max(Math.abs(x), Math.abs(z)) == 2) return wall;
        if (index >= 0) {
            for (int step = index; step <= totalHalfSteps; step += 32) {
                int sy = p.lowerFloorY + (step + 1) / 2;
                if (y == sy) return (step & 1) == 0 ? bricks : slab;
                if (y == sy - 1) return bricks;
            }
        }
        // Inner balustrade follows the staircase; a broad central shaft remains open.
        int rail = railIndex(x, z);
        if (rail >= 0) {
            for (int step = rail; step < totalHalfSteps - 2; step += 32) {
                int sy = p.lowerFloorY + (step + 1) / 2;
                if (y == sy) return darkBricks;
                if (y == sy + 1) return wall;
            }
        }
        return air;
    }

    private IBlockState masonry(Plan p, int x, int y, int z) {
        int level = y - p.lowerFloorY;
        int band = Math.floorMod(level, 16);
        boolean corner = Math.abs(x) + Math.abs(z) >= 14;
        if (corner) return pillar;
        int face = Math.max(Math.abs(x), Math.abs(z));
        int along = Math.min(Math.abs(x), Math.abs(z));
        if (level > 7 && y < p.upperFloorY - 6 && face >= 8) {
            // An arched recess is cut only into the outer leaf of the two-block wall.
            int width = band >= 10 ? 0 : band == 9 || band == 4 ? 1 : 2;
            boolean niche = band >= 4 && band <= 10 && along <= width;
            if (face == 9 && niche) return air;
            if (face == 8 && niche) {
                // Narrow, barred light slits illuminate the stairs without opening a full doorway.
                if (along == 0 && band >= 5 && band <= 7) return wall;
                if (along == 0 && band == 8) return chiseled;
                return darkBricks;
            }
            if (face == 9 && along == 3 && band >= 3 && band <= 11) return pillar;
            if (face == 9 && band == 3 && along <= 2) return darkSlab;
            if (face == 9 && band == 11 && along <= 2)
                return stair(darkStairs, outward(x, z).getOpposite(), true);
        }
        if (band == 0 || band == 15 || y >= p.upperFloorY) return darkBricks;
        int variant = (int) (mix(seed ^ key(p.x + x, p.z + z) ^ (y / 2) * 7187L) & 31);
        if (variant < 4) return cracked;
        if (variant < 6) return stone;
        return bricks;
    }

    /** Attached masonry outside the original tube, all within the existing 13-block placement margin. */
    @Nullable
    private IBlockState exteriorBlock(Plan p, int x, int y, int z) {
        if (!octagon(x, z, 12, 18) || y < p.foundationY || y > p.upperFloorY + 8) return null;
        IBlockState portal = portalBlock(p, x, y, z, false);
        if (portal == null) portal = portalBlock(p, x, y, z, true);
        if (portal != null) return portal;
        int ax = Math.abs(x), az = Math.abs(z);
        int level = y - p.lowerFloorY;
        int top = y - p.upperFloorY;
        boolean pier = ax >= 7 && az >= 7 && ax + az <= 18;

        // The open crown flares out into an octagonal gallery above deep stone corbels.
        if (top >= -3) {
            if (top == -3 && octagon(x, z, 10, 15))
                return stair(darkStairs, outward(x, z).getOpposite(), true);
            if (top == -2 && octagon(x, z, 11, 17)) return darkBricks;
            if (top == -1) return darkStone;
            if (top == 0) return bricks;
            boolean rim = !octagon(x, z, 10, 16);
            boolean cornerTower = ax >= 8 && az >= 8 && ax + az <= 18;
            if (cornerTower && top >= 1 && top <= 6) {
                if (top == 6) return darkSlab;
                if (top == 5) return chiseled;
                return top == 3 ? darkChiseled : pillar;
            }
            if (rim && top == 1) return darkBricks;
            if (rim && top >= 2 && top <= 4) {
                int along = Math.min(ax, az);
                if (Math.floorMod(along, 4) < 2) return top == 4 ? darkSlab : bricks;
                if (top == 2) return wall;
            }
            if (top > 0) return null;
        }

        // A broad, stepped plinth makes the closed lower end read as a foundation.
        if (level <= 0) return level == 0 ? darkBricks : darkStone;
        if (level == 1 && octagon(x, z, 11, 17)) return darkBricks;
        if (level == 2 && octagon(x, z, 10, 16))
            return stair(darkStairs, outward(x, z).getOpposite(), false);

        int band = Math.floorMod(level, 16);
        // Projecting cornices break up the silhouette at every repeat joint.
        if (level >= 12 && y <= p.upperFloorY - 6) {
            if (band == 14 && octagon(x, z, 10, 16))
                return stair(darkStairs, outward(x, z).getOpposite(), true);
            if (band == 15 && octagon(x, z, 11, 17)) return darkBricks;
            if (band == 0 && octagon(x, z, 11, 17)) return slab;
        }

        // Four substantial corner buttresses taper between the supporting collars.
        if (pier && y < p.upperFloorY - 1) {
            int reach = level < 5 || band >= 12 || band <= 2 ? 18 : 16;
            if (ax + az <= reach) {
                if (ax == az) return band == 8 ? darkChiseled : pillar;
                if (band == 11 && ax + az == 16)
                    return stair(darkStairs, outward(x, z).getOpposite(), true);
                return band == 6 ? darkCracked : darkBricks;
            }
        }
        return null;
    }

    /** Recessed three-wide portal with paired jambs, a stepped arch and projecting keystone. */
    @Nullable
    private IBlockState portalBlock(Plan p, int x, int y, int z, boolean upper) {
        int exit = upper ? p.upperExit : p.lowerExit;
        int floor = upper ? p.upperFloorY : p.lowerFloorY;
        int r = x * DX[exit] + z * DZ[exit];
        int w = Math.abs(-x * DZ[exit] + z * DX[exit]);
        int height = y - floor;
        if (r < 10 || r > 12 || w > 3 || height < 0 || height > 7) return null;
        if (height == 0 && w >= 2) return darkBricks;
        if (r <= 11 && w == 3 && height >= 1 && height <= 5)
            return height == 4 ? darkChiseled : pillar;
        if (r <= 11 && w == 2 && height == 5)
            return stair(stairs, wFacing(exit, x, z), true);
        if (height == 6) return w == 0 && r == 12 ? chiseled : darkBricks;
        if (height == 7) return slab;
        return null;
    }

    private static EnumFacing wFacing(int exit, int x, int z) {
        int w = -x * DZ[exit] + z * DX[exit];
        return FACING[Math.floorMod(exit + (w > 0 ? 1 : -1), 4)];
    }

    private static EnumFacing outward(int x, int z) {
        return Math.abs(x) >= Math.abs(z) ? (x < 0 ? EnumFacing.WEST : EnumFacing.EAST)
                : (z < 0 ? EnumFacing.NORTH : EnumFacing.SOUTH);
    }

    private static IBlockState stair(IBlockState state, EnumFacing facing, boolean upsideDown) {
        return state.withProperty(BlockStairs.FACING, facing).withProperty(BlockStairs.HALF,
                upsideDown ? BlockStairs.EnumHalf.TOP : BlockStairs.EnumHalf.BOTTOM);
    }

    @Nullable
    private IBlockState entranceBlock(Plan p, int x, int y, int z, boolean upper) {
        int exit = upper ? p.upperExit : p.lowerExit;
        if (!inEntrance(x, z, exit)) return null;
        int r = x * DX[exit] + z * DZ[exit];
        int floor = upper ? p.upperFloorY : p.lowerFloorY;
        int drop = upper ? p.upperDrop : p.lowerDrop;
        int down = Math.min(drop, Math.max(0, r - 10));
        int pathY = floor - down;
        if (y < pathY - 2 || y > floor + 5) return null;
        if (y < pathY) return darkStone;
        // The high half must meet the preceding platform's top, not sit a block below it.
        if (y == pathY + 1 && r >= 11 && r <= 10 + drop)
            return stairs.withProperty(BlockStairs.FACING, FACING[(exit + 2) % 4]);
        if (y > pathY) return air;
        return darkBricks;
    }

    private static boolean door(int x, int z, int direction) {
        int r = x * DX[direction] + z * DZ[direction];
        int w = -x * DZ[direction] + z * DX[direction];
        return r >= 7 && Math.abs(w) <= 1;
    }

    private static boolean inEntrance(int x, int z, int direction) {
        int r = x * DX[direction] + z * DZ[direction];
        int w = -x * DZ[direction] + z * DX[direction];
        return r >= 8 && r <= 13 && Math.abs(w) <= 1;
    }

    private static boolean octagon(int x, int z, int radius, int diagonal) {
        return Math.max(Math.abs(x), Math.abs(z)) <= radius && Math.abs(x) + Math.abs(z) <= diagonal;
    }

    /** Two-wide straight flights joined by four 2x2 level corner platforms. */
    static int stepIndex(int x, int z) {
        if (x >= 4 && x <= 5 && z >= -5 && z <= -4) return 0;
        if (x >= 4 && x <= 5 && z >= -3 && z <= 3) return z + 4;
        if (x >= 4 && x <= 5 && z >= 4 && z <= 5) return 8;
        if (z >= 4 && z <= 5 && x >= -3 && x <= 3) return 12 - x;
        if (x >= -5 && x <= -4 && z >= 4 && z <= 5) return 16;
        if (x >= -5 && x <= -4 && z >= -3 && z <= 3) return 20 - z;
        if (x >= -5 && x <= -4 && z >= -5 && z <= -4) return 24;
        if (z >= -5 && z <= -4 && x >= -3 && x <= 3) return 28 + x;
        return -1;
    }

    private static int railIndex(int x, int z) {
        if (x == 3 && z >= -3 && z <= 3) return z + 4;
        if (z == 3 && x >= -2 && x <= 2) return 12 - x;
        if (x == -3 && z >= -3 && z <= 3) return 20 - z;
        if (z == -3 && x >= -2 && x <= 2) return 28 + x;
        return -1;
    }

    public boolean isInside(BlockPos pos) {
        Plan plan = wellAt(pos);
        return plan != null && plan.contains(pos.getX(), pos.getY(), pos.getZ());
    }

    @Nullable
    public Plan wellAt(BlockPos pos) {
        Plan plan = planForRegion(Math.floorDiv(pos.getX(), REGION_BLOCKS), Math.floorDiv(pos.getZ(), REGION_BLOCKS));
        return plan != null && plan.contains(pos.getX(), pos.getY(), pos.getZ()) ? plan : null;
    }

    /** Bounded locator. Terrain prediction never loads/generates the destination chunks. */
    @Nullable
    public BlockPos nearest(BlockPos origin, boolean unexplored, World world) {
        int rx = Math.floorDiv(origin.getX(), REGION_BLOCKS), rz = Math.floorDiv(origin.getZ(), REGION_BLOCKS);
        BlockPos best = null;
        double distance = Double.POSITIVE_INFINITY;
        for (int ring = 0; ring <= 6; ring++) {
            for (int dz = -ring; dz <= ring; dz++) {
                for (int dx = -ring; dx <= ring; dx++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
                    Plan plan = planForRegion(rx + dx, rz + dz);
                    if (plan == null || (unexplored && world.isChunkGeneratedAt(
                            Math.floorDiv(plan.x, 16), Math.floorDiv(plan.z, 16)))) continue;
                    BlockPos entrance = plan.entrance();
                    double d = origin.distanceSq(entrance);
                    if (d < distance) { distance = d; best = entrance; }
                }
            }
            // All unsearched regions are at least ring*512 blocks away horizontally.
            if (best != null && distance < (double) ring * ring * REGION_BLOCKS * REGION_BLOCKS) break;
        }
        return best;
    }

    private static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xffffffffL); }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }
}
