package com.wdcftgg.witherstormmod.api.common.event;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.eventhandler.Event.HasResult;

// Compile-time stub only - excluded from packaged jar by build.gradle
@HasResult
public final class WitherStormConsumeBlockEvent extends WitherStormEvent {
    private final IBlockState state;

    public WitherStormConsumeBlockEvent(WitherStormEntity storm, IBlockState state) {
        super(storm);
        this.state = state;
    }

    public IBlockState getState() {
        return state;
    }
}

