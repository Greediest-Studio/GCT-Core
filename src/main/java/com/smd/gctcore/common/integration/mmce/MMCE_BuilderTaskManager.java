package com.smd.gctcore.common.integration.mmce;

import com.smd.gctcore.common.util.MMCEBuilderUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MMCE_BuilderTaskManager {

    private static final List<ConfigurableMachineAssembly> TASKS = new ArrayList<>();

    public static void addTask(ConfigurableMachineAssembly task) {
        TASKS.add(task);
    }

    public static boolean hasTask(BlockPos pos) {
        for (ConfigurableMachineAssembly task : TASKS) {
            if (task.getCtrlPos().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase == TickEvent.Phase.START || player.world.isRemote) {
            return;
        }

        long worldTime = player.world.getTotalWorldTime();
        UUID playerId = player.getGameProfile().getId();
        Iterator<ConfigurableMachineAssembly> iterator = TASKS.iterator();
        while (iterator.hasNext()) {
            ConfigurableMachineAssembly task = iterator.next();
            if (!playerId.equals(task.getPlayer().getGameProfile().getId())) {
                continue;
            }
            if (task.isControllerInvalid()) {
                iterator.remove();
                task.reportMissingMaterials();
                MMCEBuilderUtils.sendTranslation(player, "message.gctcore.mmce_builder.cancelled");
                continue;
            }
            if (worldTime % task.getTickInterval() != 0) {
                continue;
            }
            for (int i = 0; i < task.getOperationsPerTick() && !task.isCompleted(); i++) {
                task.assembly(true);
            }
            if (task.isCompleted()) {
                iterator.remove();
                task.reportMissingMaterials();
                MMCEBuilderUtils.sendTranslation(player, "message.gctcore.mmce_builder.success");
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.player.getGameProfile().getId();
        TASKS.removeIf(task -> playerId.equals(task.getPlayer().getGameProfile().getId()));
    }
}
