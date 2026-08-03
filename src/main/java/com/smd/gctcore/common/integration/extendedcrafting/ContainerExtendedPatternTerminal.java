package com.smd.gctcore.common.integration.extendedcrafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerExtendedPatternTerminal extends Container {
    public static final int GRID_LEFT = 10;
    public static final int GRID_TOP = 22;
    public static final int BLANK_TOP = 30;
    public static final int PREVIEW_TOP = 66;
    public static final int ENCODED_TOP = 102;
    private final TileExtendedPatternTerminal terminal;
    private final InventoryCraftResult preview = new InventoryCraftResult();
    private long lastModification = -1;
    private final int playerStart;
    private final int blankContainerSlot;
    private final int previewContainerSlot;
    private final int encodedContainerSlot;

    public ContainerExtendedPatternTerminal(InventoryPlayer player, TileExtendedPatternTerminal terminal) {
        this.terminal = terminal;
        ExtendedCraftingTier tier = terminal.tier();
        int size = tier.gridSize();
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                addSlotToContainer(new Slot(terminal, column + row * size,
                        GRID_LEFT + column * 18, GRID_TOP + row * 18));
            }
        }
        playerStart = inventorySlots.size();
        int playerLeft = layoutPlayerLeft(tier);
        int playerTop = layoutPlayerTop(tier);
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlotToContainer(new Slot(player, column + row * 9 + 9,
                    playerLeft + column * 18, playerTop + row * 18));
        for (int column = 0; column < 9; column++)
            addSlotToContainer(new Slot(player, column, playerLeft + column * 18, playerTop + 58));
        int sideX = layoutSideX(tier);
        blankContainerSlot = inventorySlots.size();
        addSlotToContainer(new Slot(terminal, terminal.blankSlot(), sideX, BLANK_TOP) {
            @Override public boolean isItemValid(ItemStack stack) {
                return stack.getItem() == ExtendedCraftingAutomation.blankPattern(terminal.tier());
            }
        });
        previewContainerSlot = inventorySlots.size();
        addSlotToContainer(new Slot(preview, 0, sideX, PREVIEW_TOP) {
            @Override public boolean isItemValid(ItemStack stack) { return false; }
            @Override public boolean canTakeStack(EntityPlayer playerIn) { return false; }
        });
        encodedContainerSlot = inventorySlots.size();
        addSlotToContainer(new Slot(terminal, terminal.encodedSlot(), sideX, ENCODED_TOP) {
            @Override public boolean isItemValid(ItemStack stack) { return false; }
        });
    }

    public TileExtendedPatternTerminal terminal() { return terminal; }
    public int blankContainerSlot() { return blankContainerSlot; }
    public int previewContainerSlot() { return previewContainerSlot; }
    public int encodedContainerSlot() { return encodedContainerSlot; }

    public static int layoutWidth(ExtendedCraftingTier tier) {
        return Math.max(176, layoutSideX(tier) + 50);
    }
    public static int layoutSideX(ExtendedCraftingTier tier) { return GRID_LEFT + tier.gridSize() * 18 + 20; }
    public static int layoutPlayerLeft(ExtendedCraftingTier tier) { return (layoutWidth(tier) - 162) / 2; }
    public static int layoutPlayerTop(ExtendedCraftingTier tier) {
        int gridBottom = GRID_TOP + tier.gridSize() * 18;
        int processBottom = ENCODED_TOP + 18;
        return Math.max(gridBottom, processBottom) + 16;
    }
    public static int layoutHeight(ExtendedCraftingTier tier) { return layoutPlayerTop(tier) + 82; }

    @Override public boolean canInteractWith(EntityPlayer playerIn) { return terminal.isUsableByPlayer(playerIn); }

    @Override
    public void detectAndSendChanges() {
        refreshPreview();
        super.detectAndSendChanges();
    }

    public void refreshPreview() {
        if (lastModification == terminal.modification()) return;
        lastModification = terminal.modification();
        ExtendedCraftingRecipeBridge.Match match = ExtendedCraftingRecipeBridge.findMatch(
                terminal.tier(), terminal.grid(), terminal.getWorld());
        preview.setInventorySlotContents(0, match == null ? ItemStack.EMPTY : match.output());
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
        if (slotId >= 0 && slotId < terminal.gridSlots()) {
            ItemStack held = player.inventory.getItemStack();
            ItemStack ghost = held.isEmpty() ? ItemStack.EMPTY : held.copy();
            if (!ghost.isEmpty()) ghost.setCount(1);
            inventorySlots.get(slotId).putStack(ghost);
            return held;
        }
        return super.slotClick(slotId, dragType, clickType, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= inventorySlots.size()) return ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack()) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack();
        ItemStack copy = stack.copy();
        int playerEnd = playerStart + 36;
        if (index == encodedContainerSlot) {
            if (!mergeItemStack(stack, playerStart, playerEnd, true)) return ItemStack.EMPTY;
        } else if (index >= playerStart && index < playerEnd) {
            if (stack.getItem() != ExtendedCraftingAutomation.blankPattern(terminal.tier())
                    || !mergeItemStack(stack, blankContainerSlot, blankContainerSlot + 1, false)) return ItemStack.EMPTY;
        } else if (index == blankContainerSlot) {
            if (!mergeItemStack(stack, playerStart, playerEnd, true)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY); else slot.onSlotChanged();
        return copy;
    }
}
