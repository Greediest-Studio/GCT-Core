package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;

public class BlockMistLotus extends BlockLilyPad {
    public BlockMistLotus() {
        setRegistryName("mist_lotus");
        setTranslationKey("gctcore.mist_lotus");
        setCreativeTab(CreativeTabs.DECORATIONS);
        setLightLevel(0.35F);
        setSoundType(SoundType.PLANT);
    }
}
