package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockDawniumOre extends Block {
    public static final Block block = new BlockDawniumOre();

    public BlockDawniumOre() {
        super(Material.ROCK);
        setTranslationKey("dawnium_ore");
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 9);
        setHardness(45.0F);
        setResistance(4000.0F);
        setLightLevel(5.0F / 15.0F);
        setLightOpacity(15);
        setCreativeTab(GctAllCreativeTab.TAB);
    }
}
