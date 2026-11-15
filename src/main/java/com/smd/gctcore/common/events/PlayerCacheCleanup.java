package com.smd.gctcore.common.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.function.Predicate;

/** 清理模组的玩家缓存；按实例和世界匹配，避免误删另一侧或其他玩家的状态。 */
public final class PlayerCacheCleanup {

    private final Map<EntityPlayer, ?> players;

    private PlayerCacheCleanup(Map<EntityPlayer, ?> players) {
        this.players = players;
    }

    public static void register(Map<EntityPlayer, ?> players) {
        MinecraftForge.EVENT_BUS.register(new PlayerCacheCleanup(players));
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        removePlayers(player -> player.world == event.getWorld());
    }

    @SubscribeEvent
    public void onPlayerLogout(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event) {
        removePlayers(player -> player == event.player);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        removePlayers(player -> player == event.getOriginal());
    }

    @SubscribeEvent
    public void onPlayerJoinWorld(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer joined = (EntityPlayer) event.getEntity();
        // 客户端重生先入世界再恢复 entityId，因此也要识别已移除的旧玩家。
        // Map.put 只换值，不会替换 equals 相等的旧键。
        removePlayers(player -> player != joined && player.world == event.getWorld()
                && (player.isDead || player.getEntityId() == joined.getEntityId()));
    }

    private void removePlayers(Predicate<EntityPlayer> predicate) {
        // Spice of Life 的 HashMap 由客户端和内建服务端共享，读写也在对应 Mixin 中加锁。
        synchronized (players) {
            players.keySet().removeIf(predicate);
        }
    }
}
