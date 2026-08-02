package com.smd.gctcore.common.integration.extendedcrafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerExtendedInterface extends Container {
    private final TileExtendedInterface tile;

    public ContainerExtendedInterface(InventoryPlayer player, TileExtendedInterface tile) {
        this.tile = tile;
        for (int row = 0; row < 4; row++) for (int column = 0; column < 9; column++)
            addSlotToContainer(new Slot(tile, column + row * 9, 8 + column * 18, 18 + row * 18) {
                @Override public boolean isItemValid(ItemStack stack) {
                    return ExtendedPatternData.isEncoded(stack) && ExtendedPatternData.tier(stack) == tile.tier();
                }
            });
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlotToContainer(new Slot(player, column + row * 9 + 9, 8 + column * 18, 104 + row * 18));
        for (int column = 0; column < 9; column++)
            addSlotToContainer(new Slot(player, column, 8 + column * 18, 162));
    }

    public TileExtendedInterface tile() { return tile; }
    @Override public boolean canInteractWith(EntityPlayer playerIn) { return tile.isUsableByPlayer(playerIn); }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= inventorySlots.size()) return ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack()) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack();
        ItemStack copy = stack.copy();
        if (index < TileExtendedInterface.PATTERN_SLOTS) {
            if (!mergeItemStack(stack, TileExtendedInterface.PATTERN_SLOTS, inventorySlots.size(), true)) return ItemStack.EMPTY;
        } else if (ExtendedPatternData.isEncoded(stack) && ExtendedPatternData.tier(stack) == tile.tier()) {
            if (!mergeItemStack(stack, 0, TileExtendedInterface.PATTERN_SLOTS, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY); else slot.onSlotChanged();
        return copy;
    }
}
