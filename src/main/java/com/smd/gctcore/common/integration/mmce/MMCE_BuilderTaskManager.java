package com.smd.gctcore.common.integration.mmce;

import com.smd.gctcore.common.util.MMCEBuilderUtils;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MMCE_BuilderTaskManager {

    private static final List<MMCE_BuilderTask> TASKS = new ArrayList<>();

    public static void addTask(MMCE_BuilderTask task) {
        TASKS.add(task);
    }

    public static boolean hasTask(World world, BlockPos pos) {
        for (MMCE_BuilderTask task : TASKS) {
            if (task.getWorld().provider.getDimension() == world.provider.getDimension() && task.getCtrlPos().equals(pos)) {
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
        Iterator<MMCE_BuilderTask> iterator = TASKS.iterator();
        while (iterator.hasNext()) {
            MMCE_BuilderTask task = iterator.next();
            if (!playerId.equals(task.getPlayer().getGameProfile().getId())) {
                continue;
            }
            if (task.isControllerInvalid()) {
                iterator.remove();
                task.report();
                MMCEBuilderUtils.sendTranslation(player, task.getCancelledMessageKey());
                continue;
            }
            if (worldTime % task.getTickInterval() != 0) {
                continue;
            }
            task.beginBatch();
            try {
                for (int i = 0; i < task.getOperationsPerTick() && !task.isCompleted(); i++) {
                    task.tick();
                }
            } finally {
                task.endBatch();
            }
            if (task.isCompleted()) {
                iterator.remove();
                task.report();
                MMCEBuilderUtils.sendTranslation(player, task.getSuccessMessageKey());
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START || event.world.isRemote) {
            return;
        }
        for (TileEntity tile : event.world.loadedTileEntityList) {
            if (tile instanceof TileMultiblockMachineController) {
                MMCE_ControllerDebug.checkTickState(event.world, tile.getPos(), (TileMultiblockMachineController) tile);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.player.getGameProfile().getId();
        TASKS.removeIf(task -> playerId.equals(task.getPlayer().getGameProfile().getId()));
    }
}
