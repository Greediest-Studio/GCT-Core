package com.smd.gctcore.common.tile.botania;

import com.smd.gctcore.common.blocks.botania.BlockGctManaPylon;
import com.smd.gctcore.common.botania.GctManaPoolTier;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/** Tier is stored entirely in the block metadata; no server tick is needed. */
public class TileGctManaPylon extends TileEntity {

    public GctManaPoolTier getTier() {
        if (world != null) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockGctManaPylon) {
                return state.getValue(BlockGctManaPylon.TIER);
            }
        }
        return GctManaPoolTier.JOETUNHEIM;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState,
                                 @Nonnull IBlockState newState) {
        if (oldState.getBlock() != newState.getBlock()) {
            return true;
        }
        return oldState.getBlock() instanceof BlockGctManaPylon
                && oldState.getValue(BlockGctManaPylon.TIER)
                != newState.getValue(BlockGctManaPylon.TIER);
    }

    @Override
    @Nonnull
    public AxisAlignedBB getRenderBoundingBox() {
        // The rotating ring extends slightly past the block; the crystal bobs
        // above its 1.25-block default top. Keep both visible at frustum edges.
        return new AxisAlignedBB(-0.0625D, 0.0D, -0.0625D,
                1.0625D, 1.375D, 1.0625D).offset(pos);
    }
}
