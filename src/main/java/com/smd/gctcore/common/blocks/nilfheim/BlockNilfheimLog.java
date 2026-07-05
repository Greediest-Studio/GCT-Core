package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.BlockLog;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;

public class BlockNilfheimLog extends BlockLog {
    public BlockNilfheimLog(String name) {
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(2.0F);
        setSoundType(SoundType.WOOD);
        setDefaultState(blockState.getBaseState().withProperty(LOG_AXIS, EnumAxis.Y));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        switch (state.getValue(LOG_AXIS)) {
            case X:
                return 4;
            case Z:
                return 8;
            case NONE:
                return 12;
            case Y:
            default:
                return 0;
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();

        switch (meta & 12) {
            case 4:
                return state.withProperty(LOG_AXIS, EnumAxis.X);
            case 8:
                return state.withProperty(LOG_AXIS, EnumAxis.Z);
            case 12:
                return state.withProperty(LOG_AXIS, EnumAxis.NONE);
            case 0:
            default:
                return state.withProperty(LOG_AXIS, EnumAxis.Y);
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LOG_AXIS);
    }
}
