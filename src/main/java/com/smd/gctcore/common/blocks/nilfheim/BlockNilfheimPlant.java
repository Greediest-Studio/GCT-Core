package com.smd.gctcore.common.blocks.nilfheim;

import com.smd.gctcore.misc.BlockRegistry;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;

public class BlockNilfheimPlant extends BlockBush {
    public BlockNilfheimPlant(String name, float lightLevel) {
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.DECORATIONS);
        setSoundType(SoundType.PLANT);
        setHardness(0.0F);
        setLightLevel(lightLevel);
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return state.getBlock() == BlockRegistry.EROSION_MOSS
                || state.getBlock() == BlockRegistry.ASHEN_SOIL
                || state.getBlock() == BlockRegistry.PERMAFROST
                || state.getBlock() == BlockRegistry.BOILING_MUD
                || super.canSustainBush(state);
    }
}
