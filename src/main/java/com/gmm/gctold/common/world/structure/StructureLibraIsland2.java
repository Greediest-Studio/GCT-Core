package com.gmm.gctold.common.world.structure;

import com.gmm.gctold.common.world.biome.BiomeLibraIsland;
import com.gmm.gctold.common.world.dimension.WorldStarland;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureLibraIsland2 extends AirborneTemplateStructure {
    public StructureLibraIsland2() {
        super(WorldStarland.DIMID, 100000, GctAllStructureTemplates.ASTRAL_ISLAND_2, 17, 50, 0);
    }

    @Override
    protected boolean canGenerateAt(World world, BlockPos origin) {
        return hasBiome(world, origin, BiomeLibraIsland.biome);
    }
}
