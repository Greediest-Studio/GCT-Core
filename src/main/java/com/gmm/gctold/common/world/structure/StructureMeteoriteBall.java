package com.gmm.gctold.common.world.structure;

import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureMeteoriteBall extends TemplateStructureFeature {
    public StructureMeteoriteBall() {
        super(0, 10000, GctAllStructureTemplates.METEORITE_BALL);
    }

    @Override
    protected BlockPos findOrigin(Random random, int chunkX, int chunkZ, World world) {
        int x = StructureGenerationHelper.randomChunkX(random, chunkX);
        int z = StructureGenerationHelper.randomChunkZ(random, chunkZ);
        int surfaceY = StructureGenerationHelper.findSurfaceY(world, x, z, false);
        int y = Math.abs(random.nextInt(Math.max(1, surfaceY)) - 24);
        return new BlockPos(x, y, z);
    }
}
