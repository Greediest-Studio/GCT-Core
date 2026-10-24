package com.wdcftgg.witherstormmod.api.common.event;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import javax.annotation.Nullable;

// Compile-time stub only - excluded from packaged jar by build.gradle
@Cancelable
public class WitherStormConsumeEvent extends WitherStormEvent {
    private final Entity consumedEntity;

    public WitherStormConsumeEvent(WitherStormEntity storm, @Nullable Entity consumedEntity, int consumedAmount) {
        super(storm);
        this.consumedEntity = consumedEntity;
    }

    @Nullable
    public Entity getConsumedEntity() {
        return consumedEntity;
    }
}

