package com.gmm.gctold.common.world.structure;

import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;

public interface StructureFeature {
    void generateWorld(Random random, int chunkX, int chunkZ, World world, int dimensionId,
            IChunkGenerator chunkGenerator, IChunkProvider chunkProvider);
}
