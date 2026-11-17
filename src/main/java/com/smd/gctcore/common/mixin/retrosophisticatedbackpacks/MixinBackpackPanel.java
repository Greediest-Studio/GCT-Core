package com.smd.gctcore.common.mixin.retrosophisticatedbackpacks;

import com.cleanroommc.modularui.value.sync.ISyncRegistrar;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel", remap = false)
public abstract class MixinBackpackPanel {

    @Redirect(
            method = "modifyPlayerSlot$Retro_Sophisticated_Backpacks",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/cleanroommc/modularui/value/sync/PanelSyncManager;itemSlot(" +
                            "Ljava/lang/String;ILcom/cleanroommc/modularui/widgets/slot/ModularSlot;)" +
                            "Lcom/cleanroommc/modularui/value/sync/ISyncRegistrar;"
            ),
            remap = false,
            require = 1
    )
    private ISyncRegistrar<?> gctcore$lockExistingPlayerSlot(
            PanelSyncManager syncManager,
            String name,
            int slotIndex,
            ModularSlot lockedSlot
    ) {
        // 玩家物品栏已经绑定；修改现有槽位，避免 MUI-UF 拒绝同键覆盖。
        SyncHandler existing = syncManager.findSyncHandlerNullable(name, slotIndex);
        if (existing instanceof ItemSlotSH) {
            ((ItemSlotSH) existing).getSlot().accessibility(false, false);
            return syncManager;
        }

        // 副手等尚未绑定的槽位继续注册，同时禁止插入和取出，避免排序移动背包。
        return syncManager.itemSlot(name, slotIndex, lockedSlot.accessibility(false, false));
    }
}
