package com.smd.gctcore.common.world.chunks;

import com.smd.gctcore.common.world.biome.nilfheim.NilfheimBiomes;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimFluid;
import com.smd.gctcore.misc.BlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ChunkGeneratorNilfheim implements IChunkGenerator {
    private static final IBlockState AIR = Blocks.AIR.getDefaultState();
    private static final IBlockState BEDROCK = Blocks.BEDROCK.getDefaultState();
    private static final IBlockState WATER = Blocks.WATER.getDefaultState();
    private static final int SUNKEN_TEAR_SEA_LEVEL = 56;
    private static final int SUNKEN_TEAR_SHORE_HEIGHT = SUNKEN_TEAR_SEA_LEVEL + 3;
    private static final int SUNKEN_TEAR_SHORE_RADIUS = 28;
    private static final int PRIMORDIAL_POOL_MIN_LEVEL = 36;
    private static final int PRIMORDIAL_POOL_MAX_LEVEL = 46;
    private static final int MISTWOOD_TREE_CELL_SIZE = 8;
    private static final int SNOWPINE_TREE_CELL_SIZE = 10;
    private static final int TREE_GENERATION_MARGIN = 8;

    private final World world;
    private final long seed;

    public ChunkGeneratorNilfheim(World world) {
        this.world = world;
        this.seed = world.getSeed();
    }

    @Nonnull
    @Override
    public Chunk generateChunk(int chunkX, int chunkZ) {
        ChunkPrimer primer = new ChunkPrimer();
        Biome[] biomes = world.getBiomeProvider().getBiomes(null, chunkX * 16, chunkZ * 16, 16, 16, false);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;
                Biome biome = biomes[x + z * 16];
                int height = getSurfaceHeight(worldX, worldZ, biome);
                generateColumn(primer, x, z, worldX, worldZ, height, biome);
            }
        }

        carveCaves(primer, chunkX, chunkZ, biomes);
        carveTunnelCaves(primer, chunkX, chunkZ, biomes);
        floodSunkenTearAirPockets(primer, biomes);
        generateBiomeFeaturesInPrimer(primer, chunkX, chunkZ, biomes);
        generateTreesInPrimer(primer, chunkX, chunkZ);

        Chunk chunk = new Chunk(world, primer, chunkX, chunkZ);
        byte[] biomeArray = chunk.getBiomeArray();
        for (int i = 0; i < biomeArray.length; i++) {
            biomeArray[i] = (byte) Biome.getIdForBiome(biomes[i]);
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        Random rand = new Random(seed ^ (chunkX * 341873128712L) ^ (chunkZ * 132897987541L));
        BlockPos base = new BlockPos(chunkX * 16, 0, chunkZ * 16);

        for (int i = 0; i < 18; i++) {
            int x = base.getX() + 2 + rand.nextInt(12);
            int z = base.getZ() + 2 + rand.nextInt(12);
            BlockPos surface = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
            Biome biome = world.getBiome(surface);
            decorateSurface(rand, surface, biome);
        }

        for (int i = 0; i < 4; i++) {
            int x = base.getX() + 2 + rand.nextInt(12);
            int z = base.getZ() + 2 + rand.nextInt(12);
            int y = 24 + rand.nextInt(82);
            generateCrystalCluster(world, rand, new BlockPos(x, y, z));
        }
    }

    private void generateColumn(ChunkPrimer primer, int x, int z, int worldX, int worldZ, int height, Biome biome) {
        int sea = seaLevel(biome);
        boolean primordialPool = biome == NilfheimBiomes.HVERGELMIR_THROAT && shouldGeneratePrimordialPool(worldX, worldZ);
        int poolSurface = primordialPool ? primordialPoolSurface(worldX, worldZ) : -1;
        int poolFloor = primordialPool ? Math.min(height, poolSurface - 3 - Math.abs((int) Math.round(noise(worldX, worldZ, 0.12D, 635L) * 2.0D))) : height;
        IBlockState body = getBodyBlock(worldX, worldZ, biome);
        IBlockState top = getTopBlock(worldX, worldZ, height, biome);
        IBlockState filler = getFillerBlock(biome);

        primer.setBlockState(x, 0, z, BEDROCK);
        for (int y = 1; y <= height; y++) {
            if (primordialPool && y > poolFloor) {
                continue;
            }

            IBlockState state = body;
            if (y >= poolFloor - 4) {
                state = filler;
            }
            if (y == poolFloor) {
                state = top;
            }
            if (biome == NilfheimBiomes.SUNKEN_TEAR && y > height - 2 && y < sea - 1) {
                state = BlockRegistry.BLACK_RIME_STONE.getDefaultState();
            }
            primer.setBlockState(x, y, z, state);
        }

        if (primordialPool) {
            for (int y = Math.max(1, poolFloor + 1); y <= poolSurface; y++) {
                primer.setBlockState(x, y, z, getPrimordialLiquidState(poolSurface - y));
            }
        }

        if (height < sea) {
            for (int y = height + 1; y <= sea; y++) {
                primer.setBlockState(x, y, z, getLiquidStateForDepth(biome, sea - y));
            }
        }

        for (int y = 1; y < 5; y++) {
            primer.setBlockState(x, y, z, BEDROCK);
        }
    }

    private int getSurfaceHeight(int x, int z, Biome biome) {
        int[] offsets = new int[] {-12, 0, 12};
        double height = 0.0D;
        double totalWeight = 0.0D;

        for (int dx : offsets) {
            for (int dz : offsets) {
                Biome sampleBiome = dx == 0 && dz == 0
                        ? biome
                        : world.getBiomeProvider().getBiome(new BlockPos(x + dx, 0, z + dz), biome);
                double distance = Math.sqrt(dx * dx + dz * dz);
                double weight = dx == 0 && dz == 0 ? 4.0D : 1.0D / (1.0D + distance / 8.0D);
                int sampleHeight = rawSurfaceHeight(x + dx, z + dz, sampleBiome);
                if (biome != NilfheimBiomes.SUNKEN_TEAR && sampleBiome == NilfheimBiomes.SUNKEN_TEAR) {
                    sampleHeight = Math.max(sampleHeight, SUNKEN_TEAR_SHORE_HEIGHT);
                }

                height += sampleHeight * weight;
                totalWeight += weight;
            }
        }

        int blendedHeight = (int) Math.round(height / totalWeight);
        if (biome == NilfheimBiomes.SUNKEN_TEAR) {
            return clamp(blendedHeight, 24, 45);
        }
        if (needsSunkenTearShore(biome) && isNearSunkenTear(x, z, SUNKEN_TEAR_SHORE_RADIUS)) {
            blendedHeight = Math.max(blendedHeight, SUNKEN_TEAR_SHORE_HEIGHT);
        }
        return clamp(blendedHeight, 28, 184);
    }

    private boolean needsSunkenTearShore(Biome biome) {
        return biome != NilfheimBiomes.SUNKEN_TEAR
                && biome != NilfheimBiomes.HVERGELMIR_THROAT;
    }

    private boolean isNearSunkenTear(int x, int z, int radius) {
        for (int dx = -radius; dx <= radius; dx += 8) {
            for (int dz = -radius; dz <= radius; dz += 8) {
                if (world.getBiomeProvider().getBiome(new BlockPos(x + dx, 0, z + dz), NilfheimBiomes.MISTVEIL_DEPTHS)
                        == NilfheimBiomes.SUNKEN_TEAR) {
                    return true;
                }
            }
        }
        return false;
    }

    private int rawSurfaceHeight(int x, int z, Biome biome) {
        double broad = noise(x, z, 0.008D, 1L);
        double medium = noise(x, z, 0.026D, 2L);
        double fine = noise(x, z, 0.075D, 3L);

        if (biome == NilfheimBiomes.HOWLING_ICEFIELD) {
            return 64 + (int) Math.round(broad * 2.0D + fine * 1.2D);
        }
        if (biome == NilfheimBiomes.SUNKEN_TEAR) {
            return 34 + (int) Math.round(broad * 6.0D + medium * 4.0D);
        }
        if (biome == NilfheimBiomes.RIMEFANG_MOUNTAINS) {
            double ridge = Math.pow(Math.max(0.0D, broad * 0.7D + medium * 0.45D + 0.55D), 2.25D);
            return 72 + (int) Math.round(ridge * 76.0D + fine * 10.0D);
        }
        if (biome == NilfheimBiomes.SOUL_EMBER_PLAINS) {
            return 62 + (int) Math.round(broad * 8.0D + medium * 5.0D);
        }
        if (biome == NilfheimBiomes.GLIMMERING_HOLLOW) {
            return 62 + (int) Math.round(broad * 14.0D + medium * 9.0D + fine * 5.0D);
        }
        if (biome == NilfheimBiomes.HVERGELMIR_THROAT) {
            double pit = Math.abs(noise(x, z, 0.035D, 4L));
            return 58 + (int) Math.round(broad * 15.0D - (0.55D - pit) * 32.0D + fine * 8.0D);
        }
        return 67 + (int) Math.round(broad * 18.0D + medium * 13.0D + fine * 5.0D);
    }

    private void carveCaves(ChunkPrimer primer, int chunkX, int chunkZ, Biome[] biomes) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;
                Biome biome = biomes[x + z * 16];
                double caveBias = biome == NilfheimBiomes.MISTVEIL_DEPTHS ? 0.10D : 0.0D;
                caveBias += biome == NilfheimBiomes.GLIMMERING_HOLLOW ? 0.06D : 0.0D;
                caveBias += biome == NilfheimBiomes.HVERGELMIR_THROAT ? 0.14D : 0.0D;

                for (int y = 18; y < 116; y++) {
                    IBlockState state = primer.getBlockState(x, y, z);
                    if (state.getMaterial().isLiquid() || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.BEDROCK) {
                        continue;
                    }

                    double n = noise3(worldX, y, worldZ, 0.052D, 810L)
                            + noise3(worldX, y, worldZ, 0.018D, 811L) * 0.55D;
                    double vertical = Math.abs(y - 58) / 120.0D;
                    if (n > 0.53D - caveBias + vertical) {
                        primer.setBlockState(x, y, z, AIR);
                    }
                }
            }
        }
    }

    private void carveTunnelCaves(ChunkPrimer primer, int chunkX, int chunkZ, Biome[] biomes) {
        for (int sourceChunkX = chunkX - 2; sourceChunkX <= chunkX + 2; sourceChunkX++) {
            for (int sourceChunkZ = chunkZ - 2; sourceChunkZ <= chunkZ + 2; sourceChunkZ++) {
                Random rand = caveRandom(sourceChunkX, sourceChunkZ, 2401L);
                int sourceX = sourceChunkX * 16 + 8;
                int sourceZ = sourceChunkZ * 16 + 8;
                Biome sourceBiome = world.getBiomeProvider().getBiome(new BlockPos(sourceX, 0, sourceZ), NilfheimBiomes.MISTVEIL_DEPTHS);
                if (rand.nextDouble() > caveSystemChance(sourceBiome)) {
                    continue;
                }

                int systems = 1 + (rand.nextDouble() < 0.28D ? 1 : 0);
                for (int i = 0; i < systems; i++) {
                    double x = sourceChunkX * 16 + rand.nextInt(16);
                    double z = sourceChunkZ * 16 + rand.nextInt(16);
                    double y = caveStartY(rand, sourceBiome);
                    double yaw = rand.nextDouble() * Math.PI * 2.0D;
                    double pitch = (rand.nextDouble() - 0.5D) * 0.22D;
                    int length = caveLength(rand, sourceBiome);
                    double radius = caveRadius(rand, sourceBiome);
                    carveWindingTunnel(primer, chunkX, chunkZ, biomes, rand, x, y, z, yaw, pitch, length, radius, 0);
                }
            }
        }
    }

    private Random caveRandom(int chunkX, int chunkZ, long salt) {
        long n = chunkX * 341873128712L + chunkZ * 132897987541L + seed + salt * 73428767L;
        n = (n ^ (n >> 13)) * 1274126177L;
        n = n ^ (n >> 16);
        return new Random(n);
    }

    private double caveSystemChance(Biome biome) {
        if (biome == NilfheimBiomes.HVERGELMIR_THROAT) {
            return 0.50D;
        }
        if (biome == NilfheimBiomes.MISTVEIL_DEPTHS) {
            return 0.42D;
        }
        if (biome == NilfheimBiomes.RIMEFANG_MOUNTAINS) {
            return 0.38D;
        }
        if (biome == NilfheimBiomes.GLIMMERING_HOLLOW) {
            return 0.36D;
        }
        if (biome == NilfheimBiomes.SOUL_EMBER_PLAINS || biome == NilfheimBiomes.SUNKEN_TEAR) {
            return 0.22D;
        }
        return 0.16D;
    }

    private int caveStartY(Random rand, Biome biome) {
        if (biome == NilfheimBiomes.RIMEFANG_MOUNTAINS) {
            return 34 + rand.nextInt(82);
        }
        if (biome == NilfheimBiomes.SUNKEN_TEAR) {
            return 18 + rand.nextInt(28);
        }
        if (biome == NilfheimBiomes.HVERGELMIR_THROAT) {
            return 18 + rand.nextInt(48);
        }
        return 20 + rand.nextInt(64);
    }

    private int caveLength(Random rand, Biome biome) {
        int base = 38 + rand.nextInt(48);
        if (biome == NilfheimBiomes.MISTVEIL_DEPTHS || biome == NilfheimBiomes.HVERGELMIR_THROAT) {
            base += 18 + rand.nextInt(22);
        }
        if (biome == NilfheimBiomes.RIMEFANG_MOUNTAINS) {
            base += 12 + rand.nextInt(28);
        }
        return base;
    }

    private double caveRadius(Random rand, Biome biome) {
        double radius = 1.45D + rand.nextDouble() * 1.45D;
        if (biome == NilfheimBiomes.MISTVEIL_DEPTHS || biome == NilfheimBiomes.HVERGELMIR_THROAT) {
            radius += 0.45D;
        }
        if (biome == NilfheimBiomes.RIMEFANG_MOUNTAINS) {
            radius += 0.25D;
        }
        return radius;
    }

    private void carveWindingTunnel(ChunkPrimer primer, int chunkX, int chunkZ, Biome[] biomes, Random rand,
                                    double x, double y, double z, double yaw, double pitch,
                                    int length, double baseRadius, int depth) {
        for (int i = 0; i < length; i++) {
            double progress = i / (double) length;
            double radius = baseRadius * (0.75D + Math.sin(progress * Math.PI) * 0.42D + rand.nextDouble() * 0.10D);
            carveCaveEllipsoid(primer, chunkX, chunkZ, biomes, x, y, z, radius, radius * (0.62D + rand.nextDouble() * 0.18D), radius);

            if (depth < 2 && i > 10 && i < length - 10 && rand.nextDouble() < 0.026D) {
                carveWindingTunnel(primer, chunkX, chunkZ, biomes, rand, x, y, z,
                        yaw + (rand.nextDouble() - 0.5D) * 1.65D,
                        pitch * 0.45D + (rand.nextDouble() - 0.5D) * 0.18D,
                        Math.max(14, length / 2 + rand.nextInt(18) - 9),
                        baseRadius * (0.62D + rand.nextDouble() * 0.18D),
                        depth + 1);
            }

            if (rand.nextDouble() < 0.018D) {
                double cavern = radius + 2.4D + rand.nextDouble() * 3.2D;
                carveCaveEllipsoid(primer, chunkX, chunkZ, biomes, x, y, z, cavern, cavern * 0.56D, cavern);
            }

            yaw += (rand.nextDouble() - 0.5D) * 0.26D;
            pitch = pitch * 0.72D + (rand.nextDouble() - 0.5D) * 0.12D;
            x += Math.cos(yaw) * Math.cos(pitch) * 1.35D;
            z += Math.sin(yaw) * Math.cos(pitch) * 1.35D;
            y += Math.sin(pitch) * 1.05D;
            y = clamp((int) Math.round(y), 10, 124);
        }
    }

    private void carveCaveEllipsoid(ChunkPrimer primer, int chunkX, int chunkZ, Biome[] biomes,
                                    double centerX, double centerY, double centerZ,
                                    double radiusX, double radiusY, double radiusZ) {
        int minX = Math.max(0, fastFloor(centerX - radiusX) - chunkX * 16 - 1);
        int maxX = Math.min(15, fastFloor(centerX + radiusX) - chunkX * 16 + 1);
        int minY = Math.max(7, fastFloor(centerY - radiusY) - 1);
        int maxY = Math.min(126, fastFloor(centerY + radiusY) + 1);
        int minZ = Math.max(0, fastFloor(centerZ - radiusZ) - chunkZ * 16 - 1);
        int maxZ = Math.min(15, fastFloor(centerZ + radiusZ) - chunkZ * 16 + 1);

        for (int x = minX; x <= maxX; x++) {
            int worldX = chunkX * 16 + x;
            double dx = (worldX + 0.5D - centerX) / radiusX;
            double dx2 = dx * dx;
            if (dx2 >= 1.0D) {
                continue;
            }

            for (int z = minZ; z <= maxZ; z++) {
                int worldZ = chunkZ * 16 + z;
                double dz = (worldZ + 0.5D - centerZ) / radiusZ;
                double horizontal = dx2 + dz * dz;
                if (horizontal >= 1.0D) {
                    continue;
                }

                Biome biome = biomes[x + z * 16];
                int surfaceY = getSurfaceHeight(worldX, worldZ, biome);
                for (int y = minY; y <= maxY; y++) {
                    if (y >= surfaceY - 6) {
                        continue;
                    }

                    double dy = (y + 0.5D - centerY) / radiusY;
                    if (horizontal + dy * dy >= 1.0D) {
                        continue;
                    }

                    IBlockState state = primer.getBlockState(x, y, z);
                    if (state.getBlock() != Blocks.AIR && state.getBlock() != Blocks.BEDROCK && !state.getMaterial().isLiquid()) {
                        primer.setBlockState(x, y, z, AIR);
                    }
                }
            }
        }
    }

    private void floodSunkenTearAirPockets(ChunkPrimer primer, Biome[] biomes) {
        int sea = seaLevel(NilfheimBiomes.SUNKEN_TEAR);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (biomes[x + z * 16] != NilfheimBiomes.SUNKEN_TEAR) {
                    continue;
                }

                for (int y = 1; y <= sea; y++) {
                    IBlockState state = primer.getBlockState(x, y, z);
                    if (state.getBlock() == Blocks.AIR) {
                        primer.setBlockState(x, y, z, WATER);
                    }
                }
            }
        }
    }

    private void generateBiomeFeaturesInPrimer(ChunkPrimer primer, int chunkX, int chunkZ, Biome[] biomes) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;
                Biome biome = biomes[x + z * 16];
                int topY = findPrimerGroundY(primer, x, z);
                if (topY <= 0 || topY >= 250) {
                    continue;
                }

                if (biome == NilfheimBiomes.RIMEFANG_MOUNTAINS && topY > 106) {
                    primer.setBlockState(x, topY, z, BlockRegistry.ANCIENT_SNOW.getDefaultState());
                }

                if (biome == NilfheimBiomes.HVERGELMIR_THROAT && topY > 35 && noise(worldX, worldZ, 0.09D, 620L) > 0.62D) {
                    primer.setBlockState(x, topY, z, BlockRegistry.GLASSROCK.getDefaultState());
                }

                if (biome == NilfheimBiomes.HOWLING_ICEFIELD && noise(worldX, worldZ, 0.07D, 621L) > 0.76D) {
                    for (int y = Math.max(6, topY - 4); y <= topY; y++) {
                        primer.setBlockState(x, y, z, AIR);
                    }
                }

                generateOreLikeVeins(primer, x, z, worldX, worldZ, topY);
                generateCaveWallFeatures(primer, x, z, worldX, worldZ, topY, biome);
                generateApathyOre(primer, chunkX, chunkZ, x, z, worldX, worldZ, biome);
            }
        }
    }

    private int findPrimerGroundY(ChunkPrimer primer, int x, int z) {
        for (int y = 255; y >= 0; y--) {
            IBlockState state = primer.getBlockState(x, y, z);
            if (state.getBlock() != Blocks.AIR && !state.getMaterial().isLiquid()) {
                return y;
            }
        }
        return 0;
    }

    private void generateOreLikeVeins(ChunkPrimer primer, int x, int z, int worldX, int worldZ, int topY) {
        for (int y = 12; y < Math.min(topY - 5, 118); y++) {
            double mist = noise3(worldX, y, worldZ, 0.095D, 901L);
            if (mist > 0.82D) {
                replaceRock(primer, x, y, z, BlockRegistry.MIST_CRYSTAL_STONE.getDefaultState());
            }

            double black = noise3(worldX, y, worldZ, 0.055D, 902L);
            if (black > 0.78D || y < 18) {
                replaceRock(primer, x, y, z, BlockRegistry.BLACK_RIME_STONE.getDefaultState());
            }
        }
    }

    private void replaceRock(ChunkPrimer primer, int x, int y, int z, IBlockState replacement) {
        Block block = primer.getBlockState(x, y, z).getBlock();
        if (block == BlockRegistry.RIMESTEEL_STONE || block == BlockRegistry.BLACK_RIME_STONE) {
            primer.setBlockState(x, y, z, replacement);
        }
    }

    private void generateApathyOre(ChunkPrimer primer, int chunkX, int chunkZ, int x, int z, int worldX, int worldZ, Biome biome) {
        if (biome != NilfheimBiomes.SUNKEN_TEAR || !isApathyOreCandidateColumn(chunkX, chunkZ, worldX, worldZ)) {
            return;
        }

        int sea = seaLevel(NilfheimBiomes.SUNKEN_TEAR);
        for (int y = sea - 1; y >= 8; y--) {
            if (primer.getBlockState(x, y, z).getBlock() != BlockRegistry.BLACK_RIME_STONE) {
                continue;
            }

            if (hasAdjacentWater(primer, x, y, z)) {
                primer.setBlockState(x, y, z, BlockRegistry.APATHY_ORE.getDefaultState());
            }
            return;
        }
    }

    private boolean isApathyOreCandidateColumn(int chunkX, int chunkZ, int worldX, int worldZ) {
        Random rand = caveRandom(chunkX, chunkZ, 3101L);
        if (rand.nextInt(4) != 0) {
            return false;
        }

        int targetX = chunkX * 16 + rand.nextInt(16);
        int targetZ = chunkZ * 16 + rand.nextInt(16);
        return worldX == targetX && worldZ == targetZ;
    }

    private boolean hasAdjacentWater(ChunkPrimer primer, int x, int y, int z) {
        return isPrimerWater(primer, x + 1, y, z)
                || isPrimerWater(primer, x - 1, y, z)
                || isPrimerWater(primer, x, y + 1, z)
                || isPrimerWater(primer, x, y - 1, z)
                || isPrimerWater(primer, x, y, z + 1)
                || isPrimerWater(primer, x, y, z - 1);
    }

    private boolean isPrimerWater(ChunkPrimer primer, int x, int y, int z) {
        if (x < 0 || x >= 16 || z < 0 || z >= 16 || y < 1 || y >= 255) {
            return false;
        }
        return primer.getBlockState(x, y, z).getBlock() == Blocks.WATER;
    }

    private void generateCaveWallFeatures(ChunkPrimer primer, int x, int z, int worldX, int worldZ, int topY, Biome biome) {
        int maxY = Math.min(topY - 6, 118);
        for (int y = 10; y < maxY; y++) {
            if (!hasAdjacentCaveAir(primer, x, y, z)) {
                continue;
            }

            double crystal = noise3(worldX, y, worldZ, 0.135D, 2402L);
            if (crystal > 0.82D) {
                replaceRock(primer, x, y, z, BlockRegistry.MIST_CRYSTAL_STONE.getDefaultState());
                continue;
            }

            if ((biome == NilfheimBiomes.MISTVEIL_DEPTHS || biome == NilfheimBiomes.GLIMMERING_HOLLOW)
                    && y > 22 && y < 86 && noise3(worldX, y, worldZ, 0.105D, 2403L) > 0.84D) {
                replaceRock(primer, x, y, z, BlockRegistry.EROSION_MOSS.getDefaultState());
                continue;
            }

            if (biome == NilfheimBiomes.HVERGELMIR_THROAT
                    && y < 58 && noise3(worldX, y, worldZ, 0.120D, 2404L) > 0.86D) {
                replaceRock(primer, x, y, z, BlockRegistry.GLASSROCK.getDefaultState());
            }
        }
    }

    private boolean hasAdjacentCaveAir(ChunkPrimer primer, int x, int y, int z) {
        return isPrimerAir(primer, x + 1, y, z)
                || isPrimerAir(primer, x - 1, y, z)
                || isPrimerAir(primer, x, y + 1, z)
                || isPrimerAir(primer, x, y - 1, z)
                || isPrimerAir(primer, x, y, z + 1)
                || isPrimerAir(primer, x, y, z - 1);
    }

    private boolean isPrimerAir(ChunkPrimer primer, int x, int y, int z) {
        if (x < 0 || x >= 16 || z < 0 || z >= 16 || y < 1 || y >= 255) {
            return false;
        }
        return primer.getBlockState(x, y, z).getBlock() == Blocks.AIR;
    }

    private void decorateSurface(Random rand, BlockPos surface, Biome biome) {
        BlockPos pos = surface;
        if (!world.isAirBlock(pos)) {
            pos = pos.up();
        }
        BlockPos ground = pos.down();

        if (biome == NilfheimBiomes.MISTVEIL_DEPTHS) {
            if (rand.nextInt(3) == 0) placePlant(pos, BlockRegistry.MIST_FERN.getDefaultState());
            if (rand.nextInt(5) == 0) hangVine(pos, BlockRegistry.FROSTBOUND_VINE.getDefaultState(), 2 + rand.nextInt(5));
        } else if (biome == NilfheimBiomes.HOWLING_ICEFIELD) {
            if (rand.nextInt(55) == 0) placePlant(pos, BlockRegistry.WEEPING_ICE_FLOWER.getDefaultState());
        } else if (biome == NilfheimBiomes.SUNKEN_TEAR) {
            if (world.getBlockState(ground).getMaterial().isLiquid() && world.isAirBlock(pos)) {
                world.setBlockState(pos, BlockRegistry.MIST_LOTUS.getDefaultState(), 2);
            }
        } else if (biome == NilfheimBiomes.SOUL_EMBER_PLAINS) {
            if (rand.nextInt(3) == 0) placePlant(pos, BlockRegistry.SOULFIRE_GRASS.getDefaultState());
            if (rand.nextInt(6) == 0) placePlant(pos, BlockRegistry.ASHEN_SHRUB.getDefaultState());
        } else if (biome == NilfheimBiomes.GLIMMERING_HOLLOW) {
            if (rand.nextInt(3) == 0) placePlant(pos, BlockRegistry.SHADOWBERRY_BUSH.getRandomGrowthState(rand));
            if (rand.nextInt(5) == 0) hangVine(pos, BlockRegistry.GLOWING_CREEPER.getDefaultState(), 3 + rand.nextInt(7));
        } else if (biome == NilfheimBiomes.HVERGELMIR_THROAT) {
            if (rand.nextInt(5) == 0) placePlant(pos, BlockRegistry.ASHEN_MUSHROOM.getDefaultState());
        }
    }

    private void placePlant(BlockPos pos, IBlockState plant) {
        if (world.isAirBlock(pos) && plant.getBlock().canPlaceBlockAt(world, pos)) {
            world.setBlockState(pos, plant, 2);
        }
    }

    private void hangVine(BlockPos pos, IBlockState vine, int length) {
        BlockPos anchor = pos.up(1 + world.rand.nextInt(3));
        EnumFacing attachSide = findVineAttachSide(anchor);
        if (attachSide == null) {
            return;
        }

        IBlockState vineState = vine
                .withProperty(BlockVine.UP, Boolean.FALSE)
                .withProperty(BlockVine.NORTH, Boolean.FALSE)
                .withProperty(BlockVine.EAST, Boolean.FALSE)
                .withProperty(BlockVine.SOUTH, Boolean.FALSE)
                .withProperty(BlockVine.WEST, Boolean.FALSE)
                .withProperty(BlockVine.getPropertyFor(attachSide), Boolean.TRUE);
        for (int i = 0; i < length; i++) {
            BlockPos p = anchor.down(i);
            if (!world.isAirBlock(p)) {
                break;
            }
            world.setBlockState(p, vineState, 2);
        }
    }

    private EnumFacing findVineAttachSide(BlockPos pos) {
        for (EnumFacing face : EnumFacing.Plane.HORIZONTAL) {
            BlockPos support = pos.offset(face);
            IBlockState supportState = world.getBlockState(support);
            if (supportState.getBlockFaceShape(world, support, face.getOpposite()) == BlockFaceShape.SOLID) {
                return face;
            }
        }
        return null;
    }

    private Random treeCellRandom(int cellX, int cellZ, long salt) {
        long n = cellX * 341873128712L + cellZ * 132897987541L + seed + salt * 73428767L;
        n = (n ^ (n >> 13)) * 1274126177L;
        n = n ^ (n >> 16);
        return new Random(n);
    }

    private void generateTreesInPrimer(ChunkPrimer primer, int chunkX, int chunkZ) {
        int minX = chunkX * 16;
        int minZ = chunkZ * 16;
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        generateTreeCellsInPrimer(primer, minX, minZ, maxX, maxZ, MISTWOOD_TREE_CELL_SIZE, 0.46D, 1701L, true);
        generateTreeCellsInPrimer(primer, minX, minZ, maxX, maxZ, SNOWPINE_TREE_CELL_SIZE, 0.34D, 1702L, false);
    }

    private void generateTreeCellsInPrimer(ChunkPrimer primer, int minX, int minZ, int maxX, int maxZ,
                                           int cellSize, double chance, long salt, boolean mistwood) {
        int minCellX = Math.floorDiv(minX - TREE_GENERATION_MARGIN, cellSize);
        int maxCellX = Math.floorDiv(maxX + TREE_GENERATION_MARGIN, cellSize);
        int minCellZ = Math.floorDiv(minZ - TREE_GENERATION_MARGIN, cellSize);
        int maxCellZ = Math.floorDiv(maxZ + TREE_GENERATION_MARGIN, cellSize);

        for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
            for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
                Random cellRand = treeCellRandom(cellX, cellZ, salt);
                if (cellRand.nextDouble() >= chance) {
                    continue;
                }

                int x = cellX * cellSize + cellRand.nextInt(cellSize);
                int z = cellZ * cellSize + cellRand.nextInt(cellSize);

                Biome biome = world.getBiomeProvider().getBiome(new BlockPos(x, 0, z), NilfheimBiomes.MISTVEIL_DEPTHS);
                int surfaceY = getSurfaceHeight(x, z, biome);
                int groundY = getPostCarveGroundY(x, z, surfaceY, biome);
                if (groundY < surfaceY - 4) {
                    continue;
                }
                BlockPos base = new BlockPos(x, groundY + 1, z);

                if (mistwood) {
                    if (biome == NilfheimBiomes.GLIMMERING_HOLLOW && canGrowTreeOnSurface(x, z, groundY, biome)) {
                        generateMistwoodTreeInPrimer(primer, minX, minZ, cellRand, base);
                    }
                } else if (biome == NilfheimBiomes.RIMEFANG_MOUNTAINS && groundY < 118 && canGrowTreeOnSurface(x, z, groundY, biome)) {
                    generateSnowpineInPrimer(primer, minX, minZ, cellRand, base);
                }
            }
        }
    }

    private int getPostCarveGroundY(int x, int z, int surfaceY, Biome biome) {
        for (int y = surfaceY; y > 5; y--) {
            if (!isCaveCarved(x, y, z, biome)) {
                return y;
            }
        }
        return 0;
    }

    private boolean isCaveCarved(int x, int y, int z, Biome biome) {
        if (y < 18 || y >= 116) {
            return false;
        }

        double caveBias = biome == NilfheimBiomes.MISTVEIL_DEPTHS ? 0.10D : 0.0D;
        caveBias += biome == NilfheimBiomes.GLIMMERING_HOLLOW ? 0.06D : 0.0D;
        caveBias += biome == NilfheimBiomes.HVERGELMIR_THROAT ? 0.14D : 0.0D;
        double n = noise3(x, y, z, 0.052D, 810L)
                + noise3(x, y, z, 0.018D, 811L) * 0.55D;
        double vertical = Math.abs(y - 58) / 120.0D;
        return n > 0.53D - caveBias + vertical;
    }

    private boolean canGrowTreeOnSurface(int x, int z, int groundY, Biome biome) {
        if (groundY <= 4 || groundY >= 245 || groundY < seaLevel(biome)) {
            return false;
        }

        Block block = getTopBlock(x, z, groundY, biome).getBlock();
        return block == BlockRegistry.EROSION_MOSS
                || block == BlockRegistry.ANCIENT_SNOW
                || block == BlockRegistry.PERMAFROST;
    }

    private void generateMistwoodTreeInPrimer(ChunkPrimer primer, int minX, int minZ, Random rand, BlockPos pos) {
        int height = 10 + rand.nextInt(10);
        for (int y = 0; y < height; y++) {
            setPrimerBlockIfAir(primer, minX, minZ, pos.up(y), BlockRegistry.MISTWOOD_LOG.getDefaultState());
            if (y > 1 && rand.nextInt(3) == 0) {
                EnumFacing face = EnumFacing.Plane.HORIZONTAL.random(rand);
                setPrimerBlockIfAir(primer, minX, minZ, pos.up(y).offset(face), BlockRegistry.MISTWOOD_LOG.getDefaultState());
            }
        }
        extendTrunkToGround(primer, minX, minZ, pos, BlockRegistry.MISTWOOD_LOG.getDefaultState());
        placeLeafBlobInPrimer(primer, minX, minZ, pos.up(height - 2), 4, BlockRegistry.MISTWOOD_LEAVES.getDefaultState());
        placeLeafBlobInPrimer(primer, minX, minZ, pos.up(height + 1), 3, BlockRegistry.MISTWOOD_LEAVES.getDefaultState());
    }

    private void generateSnowpineInPrimer(ChunkPrimer primer, int minX, int minZ, Random rand, BlockPos pos) {
        int height = 7 + rand.nextInt(6);
        for (int y = 0; y < height; y++) {
            setPrimerBlockIfAir(primer, minX, minZ, pos.up(y), BlockRegistry.SNOWPINE_LOG.getDefaultState());
        }
        extendTrunkToGround(primer, minX, minZ, pos, BlockRegistry.SNOWPINE_LOG.getDefaultState());
        for (int y = 2; y < height; y++) {
            int radius = Math.max(1, (height - y) / 3 + 1);
            placeLeafDiskInPrimer(primer, minX, minZ, pos.up(y), radius, BlockRegistry.SNOWPINE_LEAVES.getDefaultState());
        }
        setPrimerBlockIfAir(primer, minX, minZ, pos.up(height), BlockRegistry.SNOWPINE_LEAVES.getDefaultState());
    }

    private void extendTrunkToGround(ChunkPrimer primer, int minX, int minZ, BlockPos pos, IBlockState log) {
        int localX = pos.getX() - minX;
        int localZ = pos.getZ() - minZ;
        if (localX < 0 || localX >= 16 || localZ < 0 || localZ >= 16) {
            return;
        }

        for (int y = pos.getY() - 1; y >= Math.max(5, pos.getY() - 6); y--) {
            IBlockState state = primer.getBlockState(localX, y, localZ);
            if (state.getBlock() != Blocks.AIR && !state.getMaterial().isLiquid()) {
                break;
            }
            primer.setBlockState(localX, y, localZ, log);
        }
    }

    private void placeLeafBlobInPrimer(ChunkPrimer primer, int minX, int minZ, BlockPos center, int radius, IBlockState leaves) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    if (dx * dx + dz * dz + dy * dy * 2 <= radius * radius + 2) {
                        setPrimerBlockIfAir(primer, minX, minZ, center.add(dx, dy, dz), leaves);
                    }
                }
            }
        }
    }

    private void placeLeafDiskInPrimer(ChunkPrimer primer, int minX, int minZ, BlockPos center, int radius, IBlockState leaves) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= radius + 1) {
                    setPrimerBlockIfAir(primer, minX, minZ, center.add(dx, 0, dz), leaves);
                }
            }
        }
    }

    private void setPrimerBlockIfAir(ChunkPrimer primer, int minX, int minZ, BlockPos pos, IBlockState state) {
        int localX = pos.getX() - minX;
        int localZ = pos.getZ() - minZ;
        if (localX < 0 || localX >= 16 || localZ < 0 || localZ >= 16 || pos.getY() <= 0 || pos.getY() >= 256) {
            return;
        }

        if (primer.getBlockState(localX, pos.getY(), localZ).getBlock() == Blocks.AIR) {
            primer.setBlockState(localX, pos.getY(), localZ, state);
        }
    }

    private void generateCrystalCluster(World world, Random rand, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() != BlockRegistry.RIMESTEEL_STONE
                && world.getBlockState(pos).getBlock() != BlockRegistry.BLACK_RIME_STONE) {
            return;
        }

        int size = 3 + rand.nextInt(6);
        for (int i = 0; i < size; i++) {
            BlockPos p = pos.add(rand.nextInt(5) - 2, rand.nextInt(5) - 2, rand.nextInt(5) - 2);
            Block block = world.getBlockState(p).getBlock();
            if (block == BlockRegistry.RIMESTEEL_STONE || block == BlockRegistry.BLACK_RIME_STONE) {
                world.setBlockState(p, BlockRegistry.MIST_CRYSTAL_STONE.getDefaultState(), 2);
            }
        }
    }

    private IBlockState getTopBlock(int x, int z, int y, Biome biome) {
        if (biome == NilfheimBiomes.RIMEFANG_MOUNTAINS && y > 106) {
            return BlockRegistry.ANCIENT_SNOW.getDefaultState();
        }
        if (biome == NilfheimBiomes.RIMEFANG_MOUNTAINS && y < 86) {
            return BlockRegistry.EROSION_MOSS.getDefaultState();
        }
        if (biome == NilfheimBiomes.HVERGELMIR_THROAT && noise(x, z, 0.08D, 77L) > 0.5D) {
            return BlockRegistry.GLASSROCK.getDefaultState();
        }
        return biome.topBlock;
    }

    private IBlockState getFillerBlock(Biome biome) {
        if (biome == NilfheimBiomes.HOWLING_ICEFIELD) {
            return BlockRegistry.PERMAFROST.getDefaultState();
        }
        return biome.fillerBlock;
    }

    private IBlockState getBodyBlock(int x, int z, Biome biome) {
        if (biome == NilfheimBiomes.RIMEFANG_MOUNTAINS && noise(x, z, 0.05D, 88L) > 0.32D) {
            return BlockRegistry.BLACK_RIME_STONE.getDefaultState();
        }
        return BlockRegistry.RIMESTEEL_STONE.getDefaultState();
    }

    private IBlockState getLiquidStateForDepth(Biome biome, int depthFromSurface) {
        if (biome != NilfheimBiomes.HVERGELMIR_THROAT) {
            return WATER;
        }

        return getPrimordialLiquidState(depthFromSurface);
    }

    private IBlockState getPrimordialLiquidState(int depthFromSurface) {
        int level = depthFromSurface == 0 ? 0 : clamp(depthFromSurface, 1, 7);
        return BlockRegistry.PRIMORDIAL_LIQUID.getDefaultState()
                .withProperty(BlockNilfheimFluid.LEVEL, level);
    }

    private boolean shouldGeneratePrimordialPool(int x, int z) {
        double basin = noise(x, z, 0.032D, 630L) + noise(x, z, 0.011D, 631L) * 0.48D;
        double fissure = Math.abs(noise(x, z, 0.070D, 632L) + noise(x, z, 0.020D, 633L) * 0.35D);
        return basin > 0.52D || fissure < 0.038D;
    }

    private int primordialPoolSurface(int x, int z) {
        int level = 41 + (int) Math.round(noise(x, z, 0.018D, 634L) * 5.0D);
        return clamp(level, PRIMORDIAL_POOL_MIN_LEVEL, PRIMORDIAL_POOL_MAX_LEVEL);
    }

    private int seaLevel(Biome biome) {
        if (biome == NilfheimBiomes.SUNKEN_TEAR) {
            return SUNKEN_TEAR_SEA_LEVEL;
        }
        if (biome == NilfheimBiomes.HVERGELMIR_THROAT) {
            return 38;
        }
        return 0;
    }

    private double noise(int x, int z, double scale, long salt) {
        int xi = fastFloor(x * scale);
        int zi = fastFloor(z * scale);
        double xf = x * scale - xi;
        double zf = z * scale - zi;
        double u = fade(xf);
        double v = fade(zf);
        return lerp(lerp(value(xi, zi, salt), value(xi + 1, zi, salt), u),
                lerp(value(xi, zi + 1, salt), value(xi + 1, zi + 1, salt), u), v);
    }

    private double noise3(int x, int y, int z, double scale, long salt) {
        int xi = fastFloor(x * scale);
        int yi = fastFloor(y * scale);
        int zi = fastFloor(z * scale);
        double xf = x * scale - xi;
        double yf = y * scale - yi;
        double zf = z * scale - zi;
        double u = fade(xf);
        double v = fade(yf);
        double w = fade(zf);
        double x00 = lerp(value3(xi, yi, zi, salt), value3(xi + 1, yi, zi, salt), u);
        double x10 = lerp(value3(xi, yi + 1, zi, salt), value3(xi + 1, yi + 1, zi, salt), u);
        double x01 = lerp(value3(xi, yi, zi + 1, salt), value3(xi + 1, yi, zi + 1, salt), u);
        double x11 = lerp(value3(xi, yi + 1, zi + 1, salt), value3(xi + 1, yi + 1, zi + 1, salt), u);
        return lerp(lerp(x00, x10, v), lerp(x01, x11, v), w);
    }

    private double value(int x, int z, long salt) {
        long n = x * 341873128712L + z * 132897987541L + seed + salt * 42317861L;
        n = (n ^ (n >> 13)) * 1274126177L;
        n = n ^ (n >> 16);
        return ((n & 0xFFFFFF) / (double) 0x7FFFFF) - 1.0D;
    }

    private double value3(int x, int y, int z, long salt) {
        long n = x * 341873128712L + y * 42317861L + z * 132897987541L + seed + salt * 73428767L;
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

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public boolean generateStructures(@Nonnull Chunk chunkIn, int chunkX, int chunkZ) {
        return false;
    }

    @Nonnull
    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(@Nonnull EnumCreatureType creatureType, @Nonnull BlockPos position) {
        return world.getBiome(position).getSpawnableList(creatureType);
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(@Nonnull World worldIn, @Nonnull String structureName, @Nonnull BlockPos position, boolean findUnexplored) {
        return null;
    }

    @Override
    public void recreateStructures(@Nonnull Chunk chunkIn, int chunkX, int chunkZ) {
    }

    @Override
    public boolean isInsideStructure(@Nonnull World worldIn, @Nonnull String structureName, @Nonnull BlockPos pos) {
        return false;
    }
}
