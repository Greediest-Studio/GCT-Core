package com.smd.gctcore.common.events;

import com.smd.gctcore.common.world.TimeLockedDimensions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

/**
 * 玩家进入时间锁定维度（或登录时已在该维度）时，把存档里的日期同步给客户端。
 */
public class TimeLockedSyncHandler {

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        sync(event.player);
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        sync(event.player);
    }

    private static void sync(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }

        World world = player.world;
        if (world == null || world.isRemote) {
            return;
        }

        TimeLockedDimensions.syncToPlayers(world);
    }
}
