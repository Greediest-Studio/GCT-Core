package com.smd.gctcore.common.blocks.jotunheim;

import net.minecraft.block.BlockLog;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;

public class BlockJotunheimLog extends BlockLog {
    public BlockJotunheimLog(String name) {
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(2.0F);
        setResistance(5.0F);
        setHarvestLevel("axe", 0);
        setSoundType(SoundType.WOOD);
        setDefaultState(blockState.getBaseState().withProperty(LOG_AXIS, EnumAxis.Y));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        switch (state.getValue(LOG_AXIS)) {
            case X: return 4;
            case Z: return 8;
            case NONE: return 12;
            default: return 0;
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumAxis axis;
        switch (meta & 12) {
            case 4: axis = EnumAxis.X; break;
            case 8: axis = EnumAxis.Z; break;
            case 12: axis = EnumAxis.NONE; break;
            default: axis = EnumAxis.Y;
        }
        return getDefaultState().withProperty(LOG_AXIS, axis);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LOG_AXIS);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }
}
