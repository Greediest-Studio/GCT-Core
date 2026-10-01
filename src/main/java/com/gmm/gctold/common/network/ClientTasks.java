package com.gmm.gctold.common.network;

import net.minecraft.world.World;

public final class ClientTasks {
    private static Scheduler scheduler = new NoopScheduler();

    private ClientTasks() {
    }

    public static void setScheduler(Scheduler scheduler) {
        ClientTasks.scheduler = scheduler;
    }

    public static void schedule(Runnable task) {
        scheduler.schedule(task);
    }

    public static World getClientWorld() {
        return scheduler.getClientWorld();
    }

    public interface Scheduler {
        void schedule(Runnable task);

        World getClientWorld();
    }

    private static final class NoopScheduler implements Scheduler {
        @Override
        public void schedule(Runnable task) {
        }

        @Override
        public World getClientWorld() {
            return null;
        }
    }
}
