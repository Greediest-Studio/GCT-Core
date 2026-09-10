package com.smd.gctcore.common.world;

import com.smd.gctcore.common.network.GctNetworkHandler;
import com.smd.gctcore.common.network.PacketTimeLockedSync;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

public final class TimeLockedDimensions {

    private TimeLockedDimensions() {
    }

    public static WorldProviderLockedTime getProvider(World world) {
        if (world == null) {
            return null;
        }
        return world.provider instanceof WorldProviderLockedTime ? (WorldProviderLockedTime) world.provider : null;
    }

    public static boolean shiftDays(World world, int days) {
        WorldProviderLockedTime provider = getProvider(world);
        if (provider == null || world.isRemote || days == 0) {
            return false;
        }

        int current = provider.getDayOffset();
        int target = Math.max(0, current + days);
        if (target == current) {
            return false;
        }

        provider.setDayOffset(target);
        syncToPlayers(world);
        return true;
    }

    public static void syncToPlayers(World world) {
        WorldProviderLockedTime provider = getProvider(world);
        if (provider == null || world.isRemote) {
            return;
        }

        PacketTimeLockedSync packet = new PacketTimeLockedSync(world.provider.getDimension(), provider.getDayOffset());
        for (EntityPlayer player : world.playerEntities) {
            if (player instanceof EntityPlayerMP) {
                GctNetworkHandler.CHANNEL.sendTo(packet, (EntityPlayerMP) player);
            }
        }
    }
}
