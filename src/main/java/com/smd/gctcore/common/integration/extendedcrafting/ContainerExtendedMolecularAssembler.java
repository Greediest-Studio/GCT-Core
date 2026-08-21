package com.smd.gctcore.common.integration.extendedcrafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerExtendedMolecularAssembler extends Container {
    public static final int CARD_LEFT = 61;
    public static final int CARD_TOP = 20;
    public static final int PLAYER_LEFT = 8;
    public static final int PLAYER_TOP = 102;
    public static final int WIDTH = 176;
    public static final int HEIGHT = 184;

    private final TileExtendedMolecularAssembler tile;
    private int lastRemainingTicks = -1;
    private int lastBatchCrafts = -1;
    private int lastParallelLimit = -1;

    public ContainerExtendedMolecularAssembler(InventoryPlayer player,
                                               TileExtendedMolecularAssembler tile) {
        this.tile = tile;
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 3; column++) {
                addSlotToContainer(new Slot(tile, column + row * 3,
                        CARD_LEFT + column * 18, CARD_TOP + row * 18) {
                    @Override public boolean isItemValid(ItemStack stack) {
                        return tile.isItemValidForSlot(getSlotIndex(), stack);
                    }
                    @Override public int getSlotStackLimit() { return 1; }
                });
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(new Slot(player, column + row * 9 + 9,
                        PLAYER_LEFT + column * 18, PLAYER_TOP + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlotToContainer(new Slot(player, column,
                    PLAYER_LEFT + column * 18, PLAYER_TOP + 58));
        }
    }

    public TileExtendedMolecularAssembler tile() { return tile; }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUsableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int remaining = tile.getField(0);
        int batch = tile.getField(1);
        int limit = tile.getField(2);
        for (IContainerListener listener : listeners) {
            if (remaining != lastRemainingTicks) listener.sendWindowProperty(this, 0, remaining);
            if (batch != lastBatchCrafts) listener.sendWindowProperty(this, 1, batch);
            if (limit != lastParallelLimit) listener.sendWindowProperty(this, 2, limit);
        }
        lastRemainingTicks = remaining;
        lastBatchCrafts = batch;
        lastParallelLimit = limit;
    }

    @Override
    public void updateProgressBar(int id, int data) {
        tile.setField(id, data);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= inventorySlots.size()) return ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack()) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack();
        ItemStack copy = stack.copy();
        int cardEnd = TileExtendedMolecularAssembler.ACCELERATION_SLOTS;
        if (index < cardEnd) {
            if (!mergeItemStack(stack, cardEnd, inventorySlots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!tile.isItemValidForSlot(0, stack)
                    || !mergeItemStack(stack, 0, cardEnd, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY); else slot.onSlotChanged();
        return copy;
    }
}
