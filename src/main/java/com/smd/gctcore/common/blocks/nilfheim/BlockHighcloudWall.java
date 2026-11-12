package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.Block;

public class BlockHighcloudWall extends BlockNilfheimWall {
    public BlockHighcloudWall(String name, Block modelBlock) {
        super(name, modelBlock);
        setHardness(0.5F);
        setResistance(15.0F);
        setHarvestLevel("pickaxe", 0);
    }
}
