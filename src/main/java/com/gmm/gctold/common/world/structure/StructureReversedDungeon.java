package com.gmm.gctold.common.world.structure;

import com.gmm.gctold.common.world.biome.BiomeReversedForest;
import com.gmm.gctold.common.world.dimension.WorldAlfheim;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureReversedDungeon extends BiomeSurfaceTemplateStructure {
    public StructureReversedDungeon() {
        super(WorldAlfheim.DIMID, 5000, GctAllStructureTemplates.REVERSED_DUNGEON, -50, Rotation.NONE, Mirror.NONE,
                BiomeReversedForest.biome);
    }

    @Override
    protected boolean canGenerateAt(World world, BlockPos origin) {
        return hasBiome(world, origin.up(50), BiomeReversedForest.biome);
    }
}
