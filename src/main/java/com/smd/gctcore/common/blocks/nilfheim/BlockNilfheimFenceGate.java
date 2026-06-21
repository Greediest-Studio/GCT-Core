package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;

public class BlockNilfheimFenceGate extends BlockFenceGate {
    public BlockNilfheimFenceGate(String name) {
        super(BlockPlanks.EnumType.SPRUCE);
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(2.0F);
        setResistance(5.0F);
        setSoundType(SoundType.WOOD);
        setHarvestLevel("axe", 0);
    }
}
