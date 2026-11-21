package com.smd.gctcore.common.blocks.jotunheim;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockJotunheimReed extends BlockJotunheimPlant {
    public enum Segment implements IStringSerializable {
        LOWER, MIDDLE, TOP;
        @Override
        public String getName() { return name().toLowerCase(java.util.Locale.ROOT); }
    }

    public static final PropertyEnum<Segment> SEGMENT = PropertyEnum.create("segment", Segment.class);

    public BlockJotunheimReed(String name) {
        super(name, false, false);
        setDefaultState(blockState.getBaseState().withProperty(SEGMENT, Segment.LOWER));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, SEGMENT);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        boolean above = world.getBlockState(pos.up()).getBlock() == this;
        boolean below = world.getBlockState(pos.down()).getBlock() == this;
        return state.withProperty(SEGMENT, !above ? Segment.TOP : below ? Segment.MIDDLE : Segment.LOWER);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return world.getBlockState(pos.down()).getBlock() == this
                || canSustainBush(world.getBlockState(pos.down()));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 1.0D, 0.875D);
    }
}
