package com.smd.gctcore.common.tile.botania;

import com.smd.gctcore.common.blocks.botania.BlockGctManaPool;
import com.smd.gctcore.common.botania.GctManaPoolTier;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.botania.common.block.tile.mana.TilePool;

import javax.annotation.Nonnull;

public class TileGctManaPool extends TilePool {

    @Override
    public void func_73660_a() {
        int capacity = getTier().getCapacity();
        if (manaCap != capacity) {
            manaCap = capacity;
            recieveMana(0);
        }
        super.func_73660_a();
    }

    public int getConfiguredCapacity() {
        return getTier().getCapacity();
    }

    private GctManaPoolTier getTier() {
        if (world != null) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockGctManaPool && state.getPropertyKeys().contains(BlockGctManaPool.TIER)) {
                return state.getValue(BlockGctManaPool.TIER);
            }
        }
        return GctManaPoolTier.JOETUNHEIM;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        if (oldState.getBlock() != newState.getBlock()) {
            return true;
        }
        if (!(oldState.getBlock() instanceof BlockGctManaPool)) {
            return true;
        }
        return oldState.getValue(BlockGctManaPool.TIER) != newState.getValue(BlockGctManaPool.TIER);
    }
}
