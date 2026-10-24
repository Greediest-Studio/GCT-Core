package com.smd.gctcore.common.integration.witherstorm;

import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.KillTask;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 每场终战只给在场的每个队伍增加一次击杀进度。 */
public final class WitherStormQuestBridge {
    private static final ResourceLocation STORM = new ResourceLocation("witherstormmod:wither_storm");

    private WitherStormQuestBridge() {}

    public static void credit(List<EntityPlayerMP> players) {
        if (ServerQuestFile.INSTANCE == null) return;
        Set<String> teams = new HashSet<>();
        for (EntityPlayerMP player : players) {
            QuestData data = ServerQuestFile.INSTANCE.getData(player);
            if (data == null || !teams.add(data.getTeamID())) continue;
            for (KillTask task : ServerQuestFile.INSTANCE.collect(KillTask.class)) {
                if (!STORM.equals(task.entity)) continue;
                TaskData<?> progress = data.getTaskData(task);
                if (progress.progress < task.getMaxProgress() && task.quest.canStartTasks(data)) {
                    progress.addProgress(1L);
                }
            }
            data.markDirty();
        }
    }
}
