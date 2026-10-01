package com.gmm.gctold.common.world.structure;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureEndWitheriumComet extends AirborneTemplateStructure {
    public StructureEndWitheriumComet() {
        super(1, 1000, GctAllStructureTemplates.END_WITHERIUM, 16, 50, 0);
    }

    @Override
    protected boolean canGenerateAt(World world, BlockPos origin) {
        return blockAt(world, origin.up(), Blocks.AIR);
    }
}
