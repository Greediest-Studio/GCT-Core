package com.smd.gctcore.common.integration.jei;

import com.smd.gctcore.common.integration.extendedcrafting.ContainerExtendedPatternTerminal;
import com.smd.gctcore.common.network.GctNetworkHandler;
import com.smd.gctcore.common.network.PacketExtendedPatternTerminal;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExtendedPatternTerminalTransferHandler
        implements IRecipeTransferHandler<ContainerExtendedPatternTerminal> {
    @Override public Class<ContainerExtendedPatternTerminal> getContainerClass() { return ContainerExtendedPatternTerminal.class; }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(ContainerExtendedPatternTerminal container, IRecipeLayout layout,
                                               EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        if (!doTransfer) return null;
        Map<Integer, ItemStack> ghosts = new HashMap<>();
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry
                : layout.getItemStacks().getGuiIngredients().entrySet()) {
            IGuiIngredient<ItemStack> ingredient = entry.getValue();
            if (!ingredient.isInput()) continue;
            List<ItemStack> displayed = ingredient.getAllIngredients();
            if (!displayed.isEmpty() && !displayed.get(0).isEmpty()) {
                int slot = entry.getKey() - 1;
                if (slot >= 0 && slot < container.terminal().gridSlots()) ghosts.put(slot, displayed.get(0).copy());
            }
        }
        GctNetworkHandler.CHANNEL.sendToServer(new PacketExtendedPatternTerminal(ghosts));
        return null;
    }
}
