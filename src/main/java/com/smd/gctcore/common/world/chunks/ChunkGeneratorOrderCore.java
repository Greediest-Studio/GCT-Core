package com.smd.gctcore.common.world.chunks;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ChunkGeneratorOrderCore implements IChunkGenerator {

    private final World world;
    private final Random random;

    public ChunkGeneratorOrderCore(World world) {
        this.world = world;
        this.random = new Random(world.getSeed());
    }

    @Nonnull
    @Override
    public Chunk generateChunk(int chunkX, int chunkZ) {

        ChunkPrimer primer = new ChunkPrimer();

        Chunk chunk = new Chunk(this.world, primer, chunkX, chunkZ);

        byte[] biomeArray = chunk.getBiomeArray();
        for (int i = 0; i < biomeArray.length; ++i) {
            biomeArray[i] = (byte) Biome.getIdForBiome(net.minecraft.init.Biomes.VOID);
        }

        chunk.generateSkylightMap();
        return chunk;
    }
    @Override
    public void populate(int chunkX, int chunkZ) {
    }

    @Override
    public boolean generateStructures(@Nonnull Chunk chunkIn, int chunkX, int chunkZ) {
        return false;
    }

    @Nonnull
    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(@Nonnull EnumCreatureType creatureType, @Nonnull BlockPos position) {
        return Collections.emptyList();
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
