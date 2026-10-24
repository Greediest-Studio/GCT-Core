package com.wdcftgg.witherstormmod.api.common.event;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

// Compile-time stub only - excluded from packaged jar by build.gradle
@Cancelable
public class CanWitherStormTargetMobEvent extends WitherStormEvent {
    private final EntityLivingBase potentialTarget;

    public CanWitherStormTargetMobEvent(WitherStormEntity storm, EntityLivingBase potentialTarget) {
        super(storm);
        this.potentialTarget = potentialTarget;
    }

    public EntityLivingBase getPotentialTarget() {
        return potentialTarget;
    }
}

