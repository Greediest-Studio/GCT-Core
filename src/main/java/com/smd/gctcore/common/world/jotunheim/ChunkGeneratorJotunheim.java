package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimMoss;
import com.smd.gctcore.common.config.JotunheimConfig;
import com.smd.gctcore.common.world.biome.jotunheim.JotunheimBiomes;
import com.smd.gctcore.common.world.biome.jotunheim.JotunheimBiomeStorage;
import com.smd.gctcore.common.world.biome.jotunheim.BiomeJotunheimBase;
import com.smd.gctcore.misc.JotunheimBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Layered terrain and vegetation without cross-chunk world reads or cascading population. */
public final class ChunkGeneratorJotunheim implements IChunkGenerator {
    private final World world;
    private final JotunheimTerrainKernel kernel;
    private final JotunheimVegetation vegetation;
    private final JotunheimSkyFeatures skyFeatures;
    private final JotunheimYmirWells ymirWells;
    private final boolean structuresEnabled;
    private final IBlockState[][][] palette = new IBlockState[8][4][3];

    public ChunkGeneratorJotunheim(World world) {
        this(world, new JotunheimTerrainKernel(world.getSeed(), JotunheimConfig.snapshot()));
    }

    public ChunkGeneratorJotunheim(World world, JotunheimTerrainKernel kernel) {
        this(world, kernel, new JotunheimYmirWells(world.getSeed(), kernel));
    }

    public ChunkGeneratorJotunheim(World world, JotunheimTerrainKernel kernel, JotunheimYmirWells wells) {
        this.world = world;
        this.kernel = kernel;
        this.vegetation = new JotunheimVegetation(world.getSeed(), kernel);
        this.skyFeatures = new JotunheimSkyFeatures(world.getSeed(), kernel);
        this.ymirWells = wells;
        this.structuresEnabled = world.getWorldInfo().isMapFeaturesEnabled();
        Map<String, IBlockState> states = new HashMap<>();
        for (int biome = 0; biome < 8; biome++) {
            for (JotunheimTerrainKernel.HeightLayer layer : JotunheimTerrainKernel.HeightLayer.values()) {
                JotunheimEcology.Profile profile = JotunheimEcology.resolve(biome, layer);
                palette[biome][layer.ordinal()][0] = state(states, profile.rock);
                palette[biome][layer.ordinal()][1] = BlockJotunheimMoss.withBase(
                        state(states, profile.top), "cloudrock".equals(profile.rock));
                palette[biome][layer.ordinal()][2] = state(states, profile.filler);
            }
        }
    }

    public JotunheimTerrainKernel terrainKernel() { return kernel; }

    private static IBlockState state(Map<String, IBlockState> states, String key) {
        IBlockState value = states.get(key);
        if (value != null) return value;
        if ("magma".equals(key)) {
            value = Blocks.MAGMA.getDefaultState();
        } else if ("basalt".equals(key)) {
            Block basalt = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("netherized", "basalt"));
            // Development environments may omit Netherized. A real pack uses its registered basalt.
            value = basalt != null && basalt != Blocks.AIR ? basalt.getDefaultState() : JotunheimBlocks.state("volcanic_rock");
        } else {
            value = JotunheimBlocks.state(key);
        }
        states.put(key, value);
        return value;
    }

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ) {
        ChunkPrimer primer = new ChunkPrimer();
        JotunheimTerrainKernel.SampledChunk sampled = kernel.sampleChunk(chunkX, chunkZ);
        Biome[] biomes = new Biome[256];
        int[] biomeIndices = new int[256];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int biome = kernel.biomeIndex(chunkX * 16 + x, chunkZ * 16 + z);
                biomeIndices[z * 16 + x] = biome;
                biomes[z * 16 + x] = JotunheimBiomes.byIndex(biome);
                int depth = 0;
                for (int y = 255; y >= 0; y--) {
                    if (!sampled.isSolid(x, y, z)) {
                        depth = 0;
                        continue;
                    }
                    JotunheimTerrainKernel.HeightLayer layer = JotunheimTerrainKernel.layerAt(y);
                    IBlockState[] materials = palette[biome][layer.ordinal()];
                    IBlockState block = depth == 0 ? materials[1] : depth <= 3 ? materials[2] : materials[0];
                    primer.setBlockState(x, y, z, block);
                    depth++;
                }
            }
        }
        vegetation.decorate(primer, chunkX, chunkZ, sampled, biomeIndices);
        skyFeatures.decorate(primer, chunkX, chunkZ);
        if (structuresEnabled) ymirWells.decorate(primer, chunkX, chunkZ);
        Chunk chunk = new Chunk(world, primer, chunkX, chunkZ);
        JotunheimBiomeStorage.write(chunk, biomes);
        chunk.generateSkylightMap();
        JotunheimSkyFeatures.initializeBlockLight(chunk);
        return chunk;
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        // Vegetation is already in the new chunk primer; never load neighbouring world chunks here.
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type, BlockPos pos) {
        if (type != EnumCreatureType.MONSTER || !world.isBlockLoaded(pos, false)) return Collections.emptyList();
        Biome biome = world.getBiome(pos);
        return biome instanceof BiomeJotunheimBase
                ? JotunheimGiantSpawns.getPossibleCreatures(type, ((BiomeJotunheimBase) biome).index(), pos)
                : Collections.emptyList();
    }

    @Override public boolean generateStructures(Chunk chunk, int chunkX, int chunkZ) { return false; }
    @Override public void recreateStructures(Chunk chunk, int chunkX, int chunkZ) {}
    @Override public boolean isInsideStructure(World world, String name, BlockPos pos) {
        return structuresEnabled && JotunheimYmirWells.acceptsName(name) && ymirWells.isInside(pos);
    }
    @Override @Nullable
    public BlockPos getNearestStructurePos(World world, String name, BlockPos pos, boolean findUnexplored) {
        return structuresEnabled && JotunheimYmirWells.acceptsName(name)
                ? ymirWells.nearest(pos, findUnexplored, world) : null;
    }

    /** Current world blocks, rather than predicted terrain: honors edits and cached saves. No chunk loads. */
    public static List<BlockPos> enumerateStandingSurfaces(World world, int x, int z,
                                                          int minY, int maxY, int minClearance) {
        if (!world.isBlockLoaded(new BlockPos(x, 64, z), false)) return Collections.emptyList();
        int clearance = Math.max(2, minClearance);
        int low = Math.max(1, minY);
        int high = Math.min(256 - clearance, maxY);
        List<BlockPos> result = new ArrayList<>();
        for (int y = low; y <= high; y++) {
            BlockPos feet = new BlockPos(x, y, z);
            if (safeStandingPosition(world, feet, clearance)) result.add(feet);
        }
        return result;
    }

    /** Only considers already loaded columns within the supplied height band. */
    @Nullable
    public static BlockPos findSafeLanding(World world, int x, int z, int preferredY, int searchRadius,
                                          int minY, int maxY, int minClearance) {
        BlockPos best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        int radius = Math.max(0, Math.min(128, searchRadius));
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dz * dz > radius * radius) continue;
                for (BlockPos pos : enumerateStandingSurfaces(world, x+dx, z+dz, minY, maxY, minClearance)) {
                    double dy = pos.getY() - preferredY;
                    double score = dx * dx + dz * dz + dy * dy * 0.16D;
                    if (score < bestScore) {
                        best = pos;
                        bestScore = score;
                    }
                }
            }
        }
        return best;
    }

    private static boolean safeStandingPosition(World world, BlockPos feet, int clearance) {
        IBlockState support = world.getBlockState(feet.down());
        Block block = support.getBlock();
        if (!support.isFullCube() || support.getMaterial().isLiquid() || block == Blocks.MAGMA
                || block == Blocks.CACTUS || block == Blocks.FIRE) return false;
        // A 3x3 air margin gives a newly teleported player room to turn and step.
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                BlockPos base = feet.add(dx, 0, dz);
                if (!world.isBlockLoaded(base, false)) return false;
                for (int dy = 0; dy < clearance; dy++) {
                    IBlockState state = world.getBlockState(base.up(dy));
                    if (state.getMaterial() != Material.AIR) return false;
                }
            }
        }
        return true;
    }
}
