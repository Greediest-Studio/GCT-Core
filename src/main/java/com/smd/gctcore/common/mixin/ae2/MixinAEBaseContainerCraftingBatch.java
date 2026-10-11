package com.smd.gctcore.common.mixin.ae2;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotCraftingTerm;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.util.inv.WrapperInvItemHandler;
import com.smd.gctcore.common.integration.ae2.CraftingTerminalCache;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AEBaseContainer.class, remap = false)
public abstract class MixinAEBaseContainerCraftingBatch {

    @Redirect(
            method = "doAction",
            at = @At(value = "INVOKE", target = "Lappeng/container/slot/SlotCraftingTerm;doClick(Lappeng/helpers/InventoryAction;Lnet/minecraft/entity/player/EntityPlayer;)V"),
            require = 1
    )
    private void gctcore$batchCrafting(SlotCraftingTerm slot, InventoryAction action, EntityPlayer player) {
        if (!((Object) this instanceof CraftingTerminalCache)
                || (action != InventoryAction.CRAFT_SHIFT && action != InventoryAction.CRAFT_STACK)) {
            slot.doClick(action, player);
            return;
        }

        CraftingTerminalCache cache = (CraftingTerminalCache) this;
        if (cache.gctcore$isBatchCrafting()) {
            slot.doClick(action, player);
            return;
        }

        AEBaseContainer terminal = (AEBaseContainer) (Object) this;
        IInventory matrix = new WrapperInvItemHandler(
                ((IContainerCraftingPacket) terminal).getInventoryByName("crafting"));
        // 先按真实输入和玩家当前选择刷新，再开始复用配方。
        terminal.onCraftMatrixChanged(matrix);
        cache.gctcore$setBatchCrafting(true);
        try {
            slot.doClick(action, player);
        } finally {
            cache.gctcore$setBatchCrafting(false);
            // 提前结束或抛出异常也必须解除批次状态，并保存真实材料。
            terminal.onCraftMatrixChanged(matrix);
            terminal.detectAndSendChanges();
        }
    }
}
