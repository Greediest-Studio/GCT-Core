package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockSenterianCase extends Block {
    public BlockSenterianCase(String name) {
        super(Material.ROCK);
        setTranslationKey(name);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 8);
        setHardness(50.0F);
        setResistance(4000.0F);
        setLightOpacity(15);
        setCreativeTab(GctAllCreativeTab.TAB);
    }
}
