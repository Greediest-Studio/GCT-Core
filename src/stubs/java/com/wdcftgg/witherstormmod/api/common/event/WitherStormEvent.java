package com.wdcftgg.witherstormmod.api.common.event;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraftforge.fml.common.eventhandler.Event;

// Compile-time stub only - excluded from packaged jar by build.gradle
public abstract class WitherStormEvent extends Event {
    private final WitherStormEntity storm;

    public WitherStormEvent(WitherStormEntity storm) {
        this.storm = storm;
    }

    public WitherStormEntity getEntity() {
        return storm;
    }
}

