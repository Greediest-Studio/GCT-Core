package com.smd.gctcore.common.world.chunks;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorFlat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ChunkGeneratorAirport implements IChunkGenerator {
    private final World world;
    private final ChunkGeneratorFlat flatGenerator;
    private final Random random;

    public ChunkGeneratorAirport(World world) {
        this.world = world;
        this.random = new Random(world.getSeed());

        String generatorOptions = "3;2*7,3*1,1*2;1;";

        this.flatGenerator = new ChunkGeneratorFlat(world, world.getSeed(), true, generatorOptions);
    }

    @Nonnull
    @Override
    public Chunk generateChunk(int chunkX, int chunkZ) {
        return flatGenerator.generateChunk(chunkX, chunkZ);
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
        Biome biome = this.world.getBiome(position);
        return biome.getSpawnableList(creatureType);
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