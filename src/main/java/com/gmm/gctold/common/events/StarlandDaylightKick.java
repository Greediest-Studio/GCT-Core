package com.gmm.gctold.common.events;

import com.gmm.gctold.common.world.dimension.WorldStarland;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class StarlandDaylightKick {
    private static final long DAY_LENGTH = 24000L;
    private static final long DAY_END = 12000L;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) {
            return;
        }
        if (!(event.player instanceof EntityPlayerMP) || event.player.dimension != WorldStarland.DIMID) {
            return;
        }
        if (!isDaytime(event.player.world.getWorldTime()) || event.player.isRiding() || event.player.isBeingRidden()) {
            return;
        }

        TeleporterDirect.transferToSpawn((EntityPlayerMP) event.player, 0);
    }

    private static boolean isDaytime(long worldTime) {
        return worldTime % DAY_LENGTH <= DAY_END;
    }
}
