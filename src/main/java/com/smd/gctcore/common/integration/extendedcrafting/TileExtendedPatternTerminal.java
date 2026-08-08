package com.smd.gctcore.common.integration.extendedcrafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;

import java.util.List;

public class TileExtendedPatternTerminal extends TileEntity implements IInventory {
    private ExtendedCraftingTier tier = ExtendedCraftingTier.BASIC;
    private NonNullList<ItemStack> grid = NonNullList.withSize(9, ItemStack.EMPTY);
    private ItemStack blankPattern = ItemStack.EMPTY;
    private ItemStack encodedPattern = ItemStack.EMPTY;
    private long modification;

    public TileExtendedPatternTerminal() { }

    TileExtendedPatternTerminal(ExtendedCraftingTier tier) {
        setTier(tier);
    }

    public ExtendedCraftingTier tier() { return tier; }
    public int gridSlots() { return tier.gridSize() * tier.gridSize(); }
    public int blankSlot() { return gridSlots(); }
    public int encodedSlot() { return gridSlots() + 1; }
    public long modification() { return modification; }
    public List<ItemStack> grid() { return grid; }

    public boolean encode() {
        if (world == null || blankPattern.isEmpty() || !encodedPattern.isEmpty()) return false;
        if (blankPattern.getItem() != ExtendedCraftingAutomation.blankPattern(tier)) return false;
        ItemStack encoded = ExtendedPatternData.encode(blankPattern, tier, grid, world);
        if (encoded.isEmpty()) return false;
        blankPattern.shrink(1);
        if (blankPattern.isEmpty()) blankPattern = ItemStack.EMPTY;
        encodedPattern = encoded;
        markDirty();
        return true;
    }

    public void clearGrid() {
        for (int i = 0; i < grid.size(); i++) grid.set(i, ItemStack.EMPTY);
        markDirty();
    }

    @Override public int getSizeInventory() { return gridSlots() + 2; }
    @Override public boolean isEmpty() {
        if (!blankPattern.isEmpty() || !encodedPattern.isEmpty()) return false;
        for (ItemStack stack : grid) if (!stack.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index >= 0 && index < gridSlots()) return grid.get(index);
        if (index == blankSlot()) return blankPattern;
        if (index == encodedSlot()) return encodedPattern;
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = getStackInSlot(index);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = stack.splitStack(count);
        if (stack.isEmpty()) setInventorySlotContents(index, ItemStack.EMPTY); else markDirty();
        return taken;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = getStackInSlot(index);
        setInventorySlotContents(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack value = stack == null ? ItemStack.EMPTY : stack;
        if (index >= 0 && index < gridSlots()) {
            value = value.copy();
            if (!value.isEmpty()) value.setCount(1);
            grid.set(index, value);
        } else if (index == blankSlot()) {
            blankPattern = value;
        } else if (index == encodedSlot()) {
            encodedPattern = value;
        }
        markDirty();
    }

    @Override public String getName() { return "container.gctcore.extended_pattern_terminal"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) {
        return world != null && world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }
    @Override public void openInventory(EntityPlayer player) { }
    @Override public void closeInventory(EntityPlayer player) { }
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == blankSlot() && stack.getItem() == ExtendedCraftingAutomation.blankPattern(tier);
    }
    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) { }
    @Override public int getFieldCount() { return 0; }
    @Override public void clear() { clearGrid(); blankPattern = ItemStack.EMPTY; encodedPattern = ItemStack.EMPTY; markDirty(); }

    @Override
    public void markDirty() {
        modification++;
        super.markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Tier", tier.level());
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < grid.size(); i++) {
            if (!grid.get(i).isEmpty()) {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setInteger("Slot", i);
                entry.setTag("Item", grid.get(i).writeToNBT(new NBTTagCompound()));
                list.appendTag(entry);
            }
        }
        compound.setTag("Grid", list);
        compound.setTag("BlankPattern", blankPattern.writeToNBT(new NBTTagCompound()));
        compound.setTag("EncodedPattern", encodedPattern.writeToNBT(new NBTTagCompound()));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        setTier(ExtendedCraftingTier.byLevel(compound.getInteger("Tier")));
        NBTTagList list = compound.getTagList("Grid", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            int slot = entry.getInteger("Slot");
            if (slot >= 0 && slot < grid.size()) grid.set(slot, new ItemStack(entry.getCompoundTag("Item")));
        }
        blankPattern = new ItemStack(compound.getCompoundTag("BlankPattern"));
        encodedPattern = new ItemStack(compound.getCompoundTag("EncodedPattern"));
    }

    private void setTier(ExtendedCraftingTier tier) {
        this.tier = tier;
        this.grid = NonNullList.withSize(tier.gridSize() * tier.gridSize(), ItemStack.EMPTY);
    }

    public void dropContents() {
        if (world == null) return;
        if (!blankPattern.isEmpty()) InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), blankPattern);
        if (!encodedPattern.isEmpty()) InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), encodedPattern);
    }
}
