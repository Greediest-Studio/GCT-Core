package com.gmm.gctold.common.data;

import com.gmm.gctold.common.network.GctAllNetwork;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class GctAllVariableEvents {
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote && event.player instanceof EntityPlayerMP) {
            WorldSavedData mapData = GctAllVariables.MapVariables.get(event.player.world);
            WorldSavedData worldData = GctAllVariables.WorldVariables.get(event.player.world);
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            GctAllNetwork.CHANNEL.sendTo(new GctAllVariables.WorldSavedDataSyncMessage(0, mapData), player);
            GctAllNetwork.CHANNEL.sendTo(new GctAllVariables.WorldSavedDataSyncMessage(1, worldData), player);
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.player.world.isRemote && event.player instanceof EntityPlayerMP) {
            WorldSavedData worldData = GctAllVariables.WorldVariables.get(event.player.world);
            GctAllNetwork.CHANNEL.sendTo(new GctAllVariables.WorldSavedDataSyncMessage(1, worldData),
                    (EntityPlayerMP) event.player);
        }
    }
}
