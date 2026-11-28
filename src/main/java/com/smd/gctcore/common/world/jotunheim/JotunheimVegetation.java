package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimMoss;
import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimPlant;
import com.smd.gctcore.misc.JotunheimBlocks;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.smd.gctcore.common.world.jotunheim.JotunheimTerrainKernel.HeightLayer;

/**
 * Seeded decorations written directly to a new chunk's primer. Neighbouring feature
 * blueprints are replayed and clipped, so generation never reads or loads world chunks.
 * Every site check uses the same interpolated terrain as the chunk generator.
 */
public final class JotunheimVegetation {
    private static final int CELL = 32;
    private static final int FEATURE_MARGIN = JotunheimTreeShapes.MAX_RADIUS + 1;
    private static final HeightLayer[] LAYERS = HeightLayer.values();
    private final long seed;
    private final JotunheimTerrainKernel kernel;
    private final JotunheimLakes lakes;
    private final JotunheimCyclopsCaves caves;
    private final Map<Long, JotunheimTerrainKernel.SampledChunk> terrain = cache(64);
    private final Map<Long, List<Pond>> ponds = cache(96);
    private final Map<Long, List<JotunheimTreeShapes.Plan>> trees = cache(96);

    public JotunheimVegetation(long seed, JotunheimTerrainKernel kernel) {
        this(seed, kernel, null);
    }

    public JotunheimVegetation(long seed, JotunheimTerrainKernel kernel, JotunheimLakes lakes) {
        this(seed, kernel, lakes, null);
    }

    public JotunheimVegetation(long seed, JotunheimTerrainKernel kernel, JotunheimLakes lakes,
                               JotunheimCyclopsCaves caves) {
        this.seed = seed;
        this.kernel = kernel;
        this.lakes = lakes;
        this.caves = caves;
    }

    private static <K, V> Map<K, V> cache(final int limit) {
        return new LinkedHashMap<K, V>(limit, 0.75F, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<K, V> entry) { return size() > limit; }
        };
    }

    /** Instance caches are bounded, and guarded if a pack generates chunks concurrently. */
    public synchronized void decorate(ChunkPrimer primer, int chunkX, int chunkZ,
                                      JotunheimTerrainKernel.SampledChunk sampled, int[] biomes) {
        terrain.put(key(chunkX, chunkZ), sampled);
        int x0 = chunkX * 16, z0 = chunkZ * 16;
        int minX = Math.floorDiv(x0 - FEATURE_MARGIN, CELL);
        int maxX = Math.floorDiv(x0 + 15 + FEATURE_MARGIN, CELL);
        int minZ = Math.floorDiv(z0 - FEATURE_MARGIN, CELL);
        int maxZ = Math.floorDiv(z0 + 15 + FEATURE_MARGIN, CELL);
        for (int cx = minX; cx <= maxX; cx++) for (int cz = minZ; cz <= maxZ; cz++) {
            for (Pond pond : ponds(cx, cz)) {
                for (Map.Entry<BlockPos, IBlockState> entry : pond.blocks.entrySet()) {
                    put(primer, x0, z0, entry.getKey(), entry.getValue());
                }
            }
        }
        // Lexicographic global cell order gives overlapping trees identical priority in every chunk.
        for (int cx = minX; cx <= maxX; cx++) for (int cz = minZ; cz <= maxZ; cz++) {
            for (JotunheimTreeShapes.Plan plan : trees(cx, cz)) {
                for (Map.Entry<BlockPos, IBlockState> entry : plan.blocks.entrySet()) {
                    BlockPos pos = entry.getKey();
                    if (!inside(pos, x0, z0)) continue;
                    Material old = primer.getBlockState(pos.getX()-x0, pos.getY(), pos.getZ()-z0).getMaterial();
                    if (old == Material.AIR || old == Material.LEAVES || old == Material.PLANTS || old == Material.VINE) {
                        put(primer, x0, z0, pos, entry.getValue());
                    }
                }
            }
        }
        for (int cx = minX; cx <= maxX; cx++) for (int cz = minZ; cz <= maxZ; cz++) {
            for (JotunheimTreeShapes.Plan plan : trees(cx, cz)) leafVines(primer, plan, x0, z0);
        }
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            int wx = x0 + x, wz = z0 + z;
            int biome = biomes[z * 16 + x];
            for (int feet : sampled.surfaceHeights(x, z)) {
                HeightLayer layer = JotunheimTerrainKernel.layerAt(feet - 1);
                if (layer == null || feet > layer.maxY - 1) continue;
                smallFlora(primer, x, feet, z, wx, wz, JotunheimFloraProfiles.resolve(biome, layer));
            }
            for (HeightLayer layer : LAYERS) {
                JotunheimFloraProfiles.Profile profile = JotunheimFloraProfiles.resolve(biome, layer);
                underside(primer, sampled, x, z, wx, wz, profile);
                vines(primer, x, z, wx, wz, profile);
            }
        }
    }

    private JotunheimTerrainKernel.SampledChunk terrain(int x, int z) {
        int cx = Math.floorDiv(x, 16), cz = Math.floorDiv(z, 16);
        long id = key(cx, cz);
        JotunheimTerrainKernel.SampledChunk chunk = terrain.get(id);
        if (chunk == null) {
            chunk = kernel.sampleChunk(cx, cz);
            terrain.put(id, chunk);
        }
        return chunk;
    }

    private boolean solid(int x, int y, int z) {
        return y >= 0 && y <= 255 && terrain(x, z).isSolid(Math.floorMod(x, 16), y, Math.floorMod(z, 16));
    }

    private int topFeet(int x, int z, HeightLayer layer) {
        int result = -1;
        for (int y : terrain(x, z).surfaceHeights(Math.floorMod(x, 16), Math.floorMod(z, 16))) {
            if (JotunheimTerrainKernel.layerAt(y - 1) == layer) result = y;
        }
        return result;
    }

    private List<JotunheimTreeShapes.Plan> trees(int cx, int cz) {
        long id = key(cx, cz);
        List<JotunheimTreeShapes.Plan> result = trees.get(id);
        if (result != null) return result;
        result = new ArrayList<>();
        for (HeightLayer layer : LAYERS) for (int candidate = 0; candidate < 8; candidate++) {
            Random random = random(cx, cz, 1000 + layer.ordinal() * 37 + candidate * 113);
            int x = cx * CELL + 3 + random.nextInt(CELL - 6);
            int z = cz * CELL + 3 + random.nextInt(CELL - 6);
            JotunheimFloraProfiles.Profile profile = JotunheimFloraProfiles.resolve(kernel.biomeIndex(x, z), layer);
            if (candidate >= profile.treeCandidates) continue;
            double selection = random.nextDouble();
            if (selection >= profile.treeChance && selection >= profile.rootChance * 8.0D) continue;
            int y = topFeet(x, z, layer);
            if (y < 0) continue;
            JotunheimTreeShapes.Plan plan = largeFeature(x, y, z, cx, cz, profile, selection, random);
            // Forest sites get another chance to find a level footing among the new hills.
            // Every retry remains inside its original cell, preserving cross-chunk replay.
            for (int retry = 0; plan == null && retry < 2 && profile.biomeIndex == 0; retry++) {
                x = cx * CELL + 3 + random.nextInt(CELL - 6);
                z = cz * CELL + 3 + random.nextInt(CELL - 6);
                if (kernel.biomeIndex(x, z) != profile.biomeIndex) continue;
                y = topFeet(x, z, layer);
                if (y >= 0) plan = largeFeature(x, y, z, cx, cz, profile, selection, random);
            }
            if (plan != null) result.add(plan);
            if (candidate == 0 && layer == HeightLayer.SURFACE && profile.treeChance >= 0.6D
                    && random.nextDouble() < 0.42D) {
                int sx = Math.max(cx*CELL+3, Math.min(cx*CELL+CELL-4, x + (random.nextBoolean() ? 9 : -9)));
                int sz = Math.max(cz*CELL+3, Math.min(cz*CELL+CELL-4, z + (random.nextBoolean() ? 9 : -9)));
                int sy = topFeet(sx, sz, layer);
                if (sy >= 0) {
                    JotunheimTreeShapes.Plan shrub = JotunheimTreeShapes.createShrub(random, new BlockPos(sx, sy, sz), layer.maxY, false);
                    if (valid(shrub, cx, cz)) result.add(shrub);
                }
            }
        }
        trees.put(id, result);
        return result;
    }

    private JotunheimTreeShapes.Plan largeFeature(int x, int top, int z, int cx, int cz,
                                                 JotunheimFloraProfiles.Profile profile,
                                                 double selection, Random random) {
        int[] sites = profile.layer == HeightLayer.DEEP
                ? terrain(x, z).surfaceHeights(Math.floorMod(x, 16), Math.floorMod(z, 16)) : new int[] {top};
        int start = random.nextInt(sites.length);
        JotunheimFloraProfiles.Tree type = profile.tree(random);
        for (int attempt = 0; attempt < sites.length; attempt++) {
            int y = sites[(start+attempt) % sites.length];
            if (JotunheimTerrainKernel.layerAt(y-1) != profile.layer) continue;
            int maxY = profile.layer.maxY;
            // Interior cave floors may have much less room than the island's upper surface.
            for (int check = y; check <= maxY; check++) {
                if (solid(x, check, z)) { maxY = check-1; break; }
            }
            BlockPos feet = new BlockPos(x, y, z);
            JotunheimTreeShapes.Plan plan = selection < profile.treeChance
                    ? JotunheimTreeShapes.create(type, random, feet, maxY)
                    : JotunheimTreeShapes.createRoots(random, feet, maxY);
            if (valid(plan, cx, cz)) return plan;
        }
        return null;
    }

    private boolean valid(JotunheimTreeShapes.Plan plan, int cx, int cz) {
        if (plan == null) return false;
        for (BlockPos support : plan.supports) {
            if (lakes != null && lakes.blocksVegetation(support.getX(), support.getY(), support.getZ())) return false;
            if (caves != null && caves.blocksVegetation(support.getX(), support.getY(), support.getZ())) return false;
            if (!solid(support.getX(), support.getY()-1, support.getZ())
                    || solid(support.getX(), support.getY(), support.getZ())) return false;
        }
        for (BlockPos pos : plan.structural) {
            if (lakes != null && lakes.blocksVegetation(pos.getX(), pos.getY(), pos.getZ())) return false;
            if (caves != null && caves.blocksVegetation(pos.getX(), pos.getY(), pos.getZ())) return false;
            if (solid(pos.getX(), pos.getY(), pos.getZ())) return false;
        }
        // A complete tree must not be cut in half by one of the globally planned pools.
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            for (Pond pond : ponds(cx + dx, cz + dz)) {
                for (BlockPos support : plan.supports) {
                    IBlockState floor = pond.blocks.get(support.down());
                    if (floor != null && (!floor.isFullCube() || floor.getMaterial().isLiquid())) return false;
                }
                for (BlockPos pos : plan.structural) {
                    IBlockState block = pond.blocks.get(pos);
                    if (block != null && block.getMaterial() != Material.AIR) return false;
                }
            }
        }
        return true;
    }

    private static final class Pond {
        final Map<BlockPos, IBlockState> blocks = new LinkedHashMap<>();
    }

    private List<Pond> ponds(int cx, int cz) {
        long id = key(cx, cz);
        List<Pond> result = ponds.get(id);
        if (result != null) return result;
        result = new ArrayList<>();
        Random random = random(cx, cz, 3001);
        int x = cx * CELL + 6 + random.nextInt(CELL - 12);
        int z = cz * CELL + 6 + random.nextInt(CELL - 12);
        int biome = kernel.biomeIndex(x, z);
        for (HeightLayer layer : LAYERS) {
            JotunheimFloraProfiles.Profile profile = JotunheimFloraProfiles.resolve(biome, layer);
            if (profile.reedChance == 0 || random.nextDouble() > 0.68D) continue;
            int feet = topFeet(x, z, layer), radius = 2 + random.nextInt(2);
            if (feet < layer.minY + 4 || feet > layer.maxY - 3) continue;
            int waterY = feet - 1;
            boolean valid = true;
            for (int dx = -radius-1; dx <= radius+1 && valid; dx++) for (int dz = -radius-1; dz <= radius+1; dz++) {
                if (dx*dx + dz*dz > (radius+1)*(radius+1)) continue;
                int top = topFeet(x+dx, z+dz, layer);
                if (lakes != null && lakes.blocksVegetation(x+dx, top, z+dz)) { valid = false; break; }
                if (caves != null && caves.blocksVegetation(x+dx, top, z+dz)) { valid = false; break; }
                if (top < feet-1 || top > feet+2 || !solid(x+dx, waterY-2, z+dz)) { valid = false; break; }
            }
            if (!valid) continue;
            Pond pond = new Pond();
            for (int dx = -radius-1; dx <= radius+1; dx++) for (int dz = -radius-1; dz <= radius+1; dz++) {
                if (dx*dx + dz*dz > (radius+1)*(radius+1)) continue;
                BlockPos pos = new BlockPos(x+dx, waterY, z+dz);
                if (dx*dx + dz*dz > radius*radius) {
                    // A one-block bank seals the shallow pool on gently rolling wetland.
                    if (!solid(pos.getX(), waterY, pos.getZ())) pond.blocks.put(pos, JotunheimBlocks.state("humus"));
                    continue;
                }
                pond.blocks.put(pos.down(), JotunheimBlocks.state("humus"));
                pond.blocks.put(pos, Blocks.WATER.getDefaultState());
                pond.blocks.put(pos.up(), Blocks.AIR.getDefaultState());
                pond.blocks.put(pos.up(2), Blocks.AIR.getDefaultState());
            }
            result.add(pond);
        }
        ponds.put(id, result);
        return result;
    }

    private void smallFlora(ChunkPrimer primer, int x, int y, int z, int wx, int wz, JotunheimFloraProfiles.Profile p) {
        if (!air(primer, x, y, z)) return;
        IBlockState ground = primer.getBlockState(x, y-1, z);
        if (!ground.isFullCube() || ground.getMaterial().isLiquid()) return;
        Random random = random(wx, wz, 4001 + y * 31);
        // Coherent eight-block patches make meadows and undergrowth cluster instead of white noise.
        double patch = 0.5D + random(Math.floorDiv(wx, 8), Math.floorDiv(wz, 8), 4003+p.index()).nextDouble();
        double coverRoll = p.groundCover == null ? 1.0D : "jotun_grass".equals(p.groundCover)
                ? meadowPatch(wx, wz, 4011+p.index()) : random.nextDouble();
        if (p.groundCover != null && coverRoll < p.groundCoverChance * patch) {
            String rock = JotunheimEcology.resolve(p.biomeIndex, p.layer).rock;
            if (!p.groundCover.endsWith("moss") || "giantstone".equals(rock)
                    || "rotten_moss".equals(p.groundCover) && "cloudrock".equals(rock)) {
                ground = BlockJotunheimMoss.withBase(JotunheimBlocks.state(p.groundCover), "cloudrock".equals(rock));
                primer.setBlockState(x, y-1, z, ground);
                if (p.groundCover.endsWith("moss") && y >= 2 && primer.getBlockState(x, y-2, z).isFullCube()) {
                    primer.setBlockState(x, y-2, z, JotunheimBlocks.state(rock));
                }
            }
        }
        if (p.crystalKey != null && random.nextDouble() < p.crystalChance * patch) {
            // Cluster geometry already contains multiple points. Never stack it into a tower.
            primer.setBlockState(x, y, z, JotunheimBlocks.state(p.crystalKey));
        } else if (random.nextDouble() < p.iceSpikeChance * patch) {
            pillar(primer, x, y, z, 2 + random.nextInt(4), JotunheimBlocks.state("ancient_ice"),
                    JotunheimBlocks.state("ice_spike"), p.layer.maxY);
        } else if (random.nextDouble() < p.reedChance * patch && grows(JotunheimBlocks.state("giant_reed"), ground)) {
            pillar(primer, x, y, z, 2 + random.nextInt(3), JotunheimBlocks.state("giant_reed"),
                    JotunheimBlocks.state("giant_reed"), p.layer.maxY);
        } else if (random.nextDouble() < p.groundFungusChance * patch) {
            primer.setBlockState(x, y, z, JotunheimBlocks.state(
                    p.layer == HeightLayer.LAVA || p.biomeIndex == 6 ? "ember_fungus_block" : "luminous_fungal_block"));
        } else if (nativeFlowerSite(p, ground) && random.nextDouble() < flowerChance(p) * patch) {
            primer.setBlockState(x, y, z, vanillaFlower(p, random));
        } else if (random.nextDouble() < p.plantChance * patch) {
            String plant = p.plant(random);
            if (plant != null) {
                IBlockState block = JotunheimBlocks.state(plant);
                if (grows(block, ground)) primer.setBlockState(x, y, z, block);
            }
        }
    }

    private static boolean grows(IBlockState plant, IBlockState ground) {
        return plant.getBlock() instanceof BlockJotunheimPlant && ((BlockJotunheimPlant) plant.getBlock()).canGrowOn(ground);
    }

    private static boolean nativeFlowerSite(JotunheimFloraProfiles.Profile profile, IBlockState ground) {
        return (profile.layer == HeightLayer.SURFACE || profile.layer == HeightLayer.CLOUD)
                && (ground.getMaterial() == Material.GRASS || ground.getMaterial() == Material.GROUND)
                && (profile.biomeIndex == 0 || profile.biomeIndex == 2 || profile.biomeIndex == 3 || profile.biomeIndex == 7);
    }

    private static double flowerChance(JotunheimFloraProfiles.Profile profile) {
        return profile.biomeIndex == 0 ? 0.038D : profile.biomeIndex == 3 ? 0.030D
                : profile.biomeIndex == 2 ? 0.020D : 0.012D;
    }

    private static IBlockState vanillaFlower(JotunheimFloraProfiles.Profile profile, Random random) {
        if (profile.biomeIndex == 3 && random.nextBoolean()) return Blocks.RED_FLOWER.getStateFromMeta(1); // Blue orchid.
        if (profile.layer == HeightLayer.CLOUD) return Blocks.RED_FLOWER.getStateFromMeta(random.nextBoolean() ? 3 : 8);
        if (random.nextInt(4) == 0) return Blocks.YELLOW_FLOWER.getDefaultState();
        int[] colors = {0, 3, 4, 5, 6, 7, 8};
        return Blocks.RED_FLOWER.getStateFromMeta(colors[random.nextInt(colors.length)]);
    }

    private double meadowPatch(int x, int z, long salt) {
        int gx = Math.floorDiv(x, 12), gz = Math.floorDiv(z, 12);
        double tx = Math.floorMod(x, 12)/12.0D, tz = Math.floorMod(z, 12)/12.0D;
        tx = tx*tx*(3.0D-2.0D*tx);
        tz = tz*tz*(3.0D-2.0D*tz);
        double a = random(gx, gz, salt).nextDouble(), b = random(gx+1, gz, salt).nextDouble();
        double c = random(gx, gz+1, salt).nextDouble(), d = random(gx+1, gz+1, salt).nextDouble();
        double value = (a+(b-a)*tx)*(1.0D-tz) + (c+(d-c)*tx)*tz;
        return Math.max(0.0D, Math.min(1.0D, (value-0.5D)*2.0D+0.5D));
    }

    private void leafVines(ChunkPrimer primer, JotunheimTreeShapes.Plan plan, int x0, int z0) {
        if (plan.supports.isEmpty()) return;
        BlockPos foot = plan.supports.iterator().next();
        HeightLayer layer = JotunheimTerrainKernel.layerAt(foot.getY()-1);
        if (layer == null) return;
        JotunheimFloraProfiles.Profile profile = JotunheimFloraProfiles.resolve(kernel.biomeIndex(foot.getX(), foot.getZ()), layer);
        if (profile.vineKey == null) return;
        for (Map.Entry<BlockPos, IBlockState> entry : plan.blocks.entrySet()) {
            if (entry.getValue().getMaterial() != Material.LEAVES) continue;
            BlockPos leaf = entry.getKey();
            Random random = random(leaf.getX(), leaf.getZ(), 4501+leaf.getY()*17);
            if (random.nextDouble() > profile.vineChance*2.0D) continue;
            EnumFacing away = EnumFacing.HORIZONTALS[random.nextInt(4)];
            BlockPos start = leaf.offset(away);
            if (!inside(start, x0, z0) || plan.blocks.containsKey(start)) continue;
            IBlockState vine = vineState(profile.vineKey, away.getOpposite());
            int length = 2+random.nextInt(5);
            for (int n = 0; n < length; n++) {
                BlockPos pos = start.down(n);
                if (pos.getY() < 1 || solid(pos.getX(), pos.getY(), pos.getZ())) break;
                if (inside(pos, x0, z0)) {
                    if (!air(primer, pos.getX()-x0, pos.getY(), pos.getZ()-z0)) break;
                    put(primer, x0, z0, pos, vine);
                }
            }
        }
    }

    private static IBlockState vineState(String key, EnumFacing face) {
        return JotunheimBlocks.state(key).withProperty(
                face == EnumFacing.NORTH ? BlockVine.NORTH : face == EnumFacing.SOUTH ? BlockVine.SOUTH
                        : face == EnumFacing.WEST ? BlockVine.WEST : BlockVine.EAST, true);
    }

    private static void pillar(ChunkPrimer primer, int x, int y, int z, int height, IBlockState stem, IBlockState tip, int maxY) {
        int room = 0;
        while (room < height && y + room <= maxY && air(primer, x, y+room, z)) room++;
        if (room < 2) return;
        for (int n = 0; n < room; n++) primer.setBlockState(x, y+n, z, n == room-1 ? tip : stem);
    }

    private void underside(ChunkPrimer primer, JotunheimTerrainKernel.SampledChunk sampled,
                           int x, int z, int wx, int wz, JotunheimFloraProfiles.Profile p) {
        if (p.rootChance == 0) return;
        int y = p.layer.minY;
        while (y <= p.layer.maxY && !sampled.isSolid(x, y, z)) y++;
        if (y > p.layer.maxY || y < 2) return;
        Random random = random(wx, wz, 5001+p.index());
        if (random.nextDouble() < p.rootChance) {
            int length = 2 + random.nextInt(p.layer == HeightLayer.CLOUD ? 10 : 6);
            for (int n = 1; n <= length && y-n > 1 && air(primer, x, y-n, z); n++) {
                primer.setBlockState(x, y-n, z, JotunheimBlocks.state("ancient_root"));
            }
        }
    }

    private void vines(ChunkPrimer primer, int x, int z, int wx, int wz, JotunheimFloraProfiles.Profile p) {
        if (p.vineKey == null || p.vineChance <= 0) return;
        Random random = random(wx, wz, 6001+p.index());
        if (random.nextDouble() > p.vineChance * 3.0D) return;
        for (int y = p.layer.maxY-1; y >= p.layer.minY; y--) {
            if (!air(primer, x, y, z)) continue;
            for (EnumFacing face : EnumFacing.HORIZONTALS) {
                if (!solid(wx+face.getXOffset(), y, wz+face.getZOffset())) continue;
                IBlockState vine = vineState(p.vineKey, face);
                int length = 3 + random.nextInt(p.layer == HeightLayer.CLOUD ? 12 : 7);
                for (int n = 0; n < length && y-n >= Math.max(1, p.layer.minY-12) && air(primer, x, y-n, z); n++) {
                    primer.setBlockState(x, y-n, z, vine);
                }
                return;
            }
        }
    }

    private static boolean air(ChunkPrimer primer, int x, int y, int z) {
        return y > 0 && y < 255 && primer.getBlockState(x, y, z).getMaterial() == Material.AIR;
    }

    private static boolean inside(BlockPos pos, int x0, int z0) {
        return pos.getX() >= x0 && pos.getX() < x0+16 && pos.getZ() >= z0 && pos.getZ() < z0+16
                && pos.getY() > 0 && pos.getY() < 255;
    }

    private static void put(ChunkPrimer primer, int x0, int z0, BlockPos pos, IBlockState state) {
        if (inside(pos, x0, z0)) primer.setBlockState(pos.getX()-x0, pos.getY(), pos.getZ()-z0, state);
    }

    private Random random(int x, int z, long salt) {
        long value = seed ^ ((long) x * 0x632BE59BD9B4E019L) ^ ((long) z * 0x9E3779B97F4A7C15L) ^ (salt * 0x94D049BB133111EBL);
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return new Random(value ^ (value >>> 31));
    }

    private static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xFFFFFFFFL); }
}
