package com.wdcftgg.witherstormmod.api.common.event;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;

// Compile-time stub only - excluded from packaged jar by build.gradle
public final class WitherStormVictoryEvent extends WitherStormEvent {
    private final EntityPlayerMP killer;
    private final List<EntityPlayerMP> participants;
    private boolean handlesLoot;

    public WitherStormVictoryEvent(WitherStormEntity storm, EntityPlayerMP killer, List<EntityPlayerMP> participants) {
        super(storm);
        this.killer = killer;
        this.participants = participants;
    }

    public EntityPlayerMP getKiller() {
        return killer;
    }

    public List<EntityPlayerMP> getParticipants() {
        return participants;
    }

    public void handleLoot() {
        handlesLoot = true;
    }

    public boolean handlesLoot() {
        return handlesLoot;
    }
}

