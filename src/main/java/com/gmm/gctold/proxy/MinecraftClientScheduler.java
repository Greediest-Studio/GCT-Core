package com.gmm.gctold.proxy;

import com.gmm.gctold.common.network.ClientTasks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class MinecraftClientScheduler implements ClientTasks.Scheduler {
    @Override
    public void schedule(Runnable task) {
        Minecraft.getMinecraft().addScheduledTask(task);
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().player.world;
    }
}
