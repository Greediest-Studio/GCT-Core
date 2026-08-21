package com.smd.gctcore.common.integration.extendedcrafting;

import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import com.smd.gctcore.common.config.GCTCompatConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class TileExtendedMolecularAssembler extends TileExtendedGridMachine implements ITickable, IInventory {
    public static final int ACCELERATION_SLOTS = 6;
    private static final int MINIMUM_CRAFT_TICKS = 5;
    private final NonNullList<ItemStack> inputs = NonNullList.create();
    private final NonNullList<ItemStack> outputs = NonNullList.create();
    private final NonNullList<ItemStack> accelerationCards =
            NonNullList.withSize(ACCELERATION_SLOTS, ItemStack.EMPTY);
    private ItemStack activePattern = ItemStack.EMPTY;
    private int remainingTicks;
    private int batchCrafts;
    private long interfacePos;
    private long lastCompletionTick = Long.MIN_VALUE;

    public TileExtendedMolecularAssembler() { }
    TileExtendedMolecularAssembler(ExtendedCraftingTier tier) { super(tier); }

    @Override protected BlockExtendedCraftingAutomation.Kind machineKind() { return BlockExtendedCraftingAutomation.Kind.ASSEMBLER; }
    @Override protected boolean requiresChannel() { return false; }

    public synchronized boolean isBusy() {
        return isBusyFor(null);
    }

    public synchronized boolean isBusyFor(BlockPos ownerPos) {
        boolean completingThisTick = world != null
                && lastCompletionTick == world.getTotalWorldTime();
        if (completingThisTick) return true;
        boolean hasTask = !activePattern.isEmpty() || remainingTicks > 0
                || !inputs.isEmpty() || !outputs.isEmpty() || batchCrafts > 0;
        if (!hasTask) return false;
        if (ownerPos == null || interfacePos != ownerPos.toLong()) return true;
        return batchCrafts <= 0 || batchCrafts >= getParallelCraftLimit();
    }

    public synchronized boolean canAccept(BlockPos ownerPos, ItemStack pattern) {
        if (pattern.isEmpty() || isBusyFor(ownerPos)) return false;
        return batchCrafts == 0 || samePattern(activePattern, pattern);
    }

    public synchronized boolean start(TileExtendedInterface owner, ItemStack pattern,
                                      NonNullList<ItemStack> taskInputs,
                                      NonNullList<ItemStack> taskOutputs) {
        if (!canAccept(owner.getPos(), pattern) || owner.tier() != tier
                || taskInputs.isEmpty() || taskOutputs.isEmpty()) return false;

        boolean firstCraft = batchCrafts == 0;
        if (firstCraft) {
            activePattern = pattern.copy();
            activePattern.setCount(1);
            remainingTicks = getCraftDurationTicks();
            interfacePos = owner.getPos().toLong();
        }
        appendCopies(taskInputs, inputs);
        appendCopies(taskOutputs, outputs);
        batchCrafts++;
        markDirty();
        return true;
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) return;
        if (remainingTicks > 0 && --remainingTicks <= 0) finishCraft();
    }

    private synchronized void finishCraft() {
        TileEntity owner = world.getTileEntity(BlockPos.fromLong(interfacePos));
        if (owner instanceof TileExtendedInterface
                && ((TileExtendedInterface) owner).tier() == tier
                && ((TileExtendedInterface) owner).receiveAssemblerOutputs(copy(outputs))) {
            clearTask();
            lastCompletionTick = world.getTotalWorldTime();
        } else if (!(owner instanceof TileExtendedInterface)) {
            dropTaskContents();
        } else {
            remainingTicks = 1;
        }
        markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("RemainingTicks", remainingTicks);
        compound.setInteger("BatchCrafts", batchCrafts);
        compound.setLong("InterfacePos", interfacePos);
        compound.setLong("LastCompletionTick", lastCompletionTick);
        compound.setTag("ActivePattern", activePattern.writeToNBT(new NBTTagCompound()));
        compound.setTag("Inputs", writeList(inputs));
        compound.setTag("Outputs", writeList(outputs));
        compound.setTag("AccelerationCards", writeFixedList(accelerationCards));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        remainingTicks = compound.getInteger("RemainingTicks");
        batchCrafts = compound.getInteger("BatchCrafts");
        interfacePos = compound.getLong("InterfacePos");
        lastCompletionTick = compound.hasKey("LastCompletionTick", 4)
                ? compound.getLong("LastCompletionTick") : Long.MIN_VALUE;
        activePattern = new ItemStack(compound.getCompoundTag("ActivePattern"));
        readList(compound.getTagList("Inputs", 10), inputs);
        readList(compound.getTagList("Outputs", 10), outputs);
        readFixedList(compound.getTagList("AccelerationCards", 10), accelerationCards);
    }

    public synchronized void dropContents() {
        if (world == null) return;
        dropTaskContents();
        for (ItemStack stack : accelerationCards) drop(stack);
        clearCards();
    }

    private void dropTaskContents() {
        // Before completion AE2 has supplied the inputs, but the outputs have not been earned yet.
        // Once the timer reaches zero only the finished outputs may be recovered.
        if (remainingTicks > 0) {
            for (ItemStack stack : inputs) drop(stack);
        } else {
            for (ItemStack stack : outputs) drop(stack);
        }
        clearTask();
    }

    private void drop(ItemStack stack) {
        if (!stack.isEmpty()) InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    private void clearTask() {
        inputs.clear();
        outputs.clear();
        activePattern = ItemStack.EMPTY;
        remainingTicks = 0;
        batchCrafts = 0;
        interfacePos = 0L;
    }

    public int getAccelerationCardCount() {
        int count = 0;
        for (ItemStack stack : accelerationCards) if (!stack.isEmpty()) count++;
        return count;
    }

    public int getParallelCraftLimit() {
        return 1 << getAccelerationCardCount();
    }

    public int getBatchCrafts() {
        return batchCrafts;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public int getCraftDurationTicks() {
        return Math.max(MINIMUM_CRAFT_TICKS,
                GCTCompatConfig.extendedCraftingAutomation.assemblerCraftTicks);
    }

    private static boolean samePattern(ItemStack first, ItemStack second) {
        return !first.isEmpty() && !second.isEmpty()
                && ItemStack.areItemsEqual(first, second)
                && ItemStack.areItemStackTagsEqual(first, second);
    }

    private static void appendCopies(List<ItemStack> source, NonNullList<ItemStack> destination) {
        for (ItemStack stack : source) if (!stack.isEmpty()) destination.add(stack.copy());
    }

    private static List<ItemStack> copy(List<ItemStack> source) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : source) result.add(stack.copy());
        return result;
    }

    private static NBTTagList writeList(List<ItemStack> source) {
        NBTTagList list = new NBTTagList();
        for (ItemStack stack : source) list.appendTag(stack.writeToNBT(new NBTTagCompound()));
        return list;
    }

    private static NBTTagList writeFixedList(List<ItemStack> source) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < source.size(); i++) {
            ItemStack stack = source.get(i);
            if (stack.isEmpty()) continue;
            NBTTagCompound tag = stack.writeToNBT(new NBTTagCompound());
            tag.setInteger("Slot", i);
            list.appendTag(tag);
        }
        return list;
    }

    private static void readList(NBTTagList list, NonNullList<ItemStack> destination) {
        destination.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack stack = new ItemStack(list.getCompoundTagAt(i));
            if (!stack.isEmpty()) destination.add(stack);
        }
    }

    private static void readFixedList(NBTTagList list, NonNullList<ItemStack> destination) {
        for (int i = 0; i < destination.size(); i++) destination.set(i, ItemStack.EMPTY);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getInteger("Slot");
            if (slot >= 0 && slot < destination.size()) {
                ItemStack stack = new ItemStack(tag);
                if (!stack.isEmpty() && isAccelerationCard(stack)) {
                    stack.setCount(1);
                    destination.set(slot, stack);
                }
            }
        }
    }

    private static boolean isAccelerationCard(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IUpgradeModule
                && ((IUpgradeModule) stack.getItem()).getType(stack) == Upgrades.SPEED;
    }

    @Override public int getSizeInventory() { return ACCELERATION_SLOTS; }
    @Override public boolean isEmpty() {
        for (ItemStack stack : accelerationCards) if (!stack.isEmpty()) return false;
        return true;
    }
    @Override public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < accelerationCards.size()
                ? accelerationCards.get(index) : ItemStack.EMPTY;
    }
    @Override public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = getStackInSlot(index);
        if (stack.isEmpty() || count <= 0) return ItemStack.EMPTY;
        ItemStack taken = stack.splitStack(Math.min(count, stack.getCount()));
        if (stack.isEmpty()) accelerationCards.set(index, ItemStack.EMPTY);
        markDirty();
        return taken;
    }
    @Override public ItemStack removeStackFromSlot(int index) {
        if (index < 0 || index >= accelerationCards.size()) return ItemStack.EMPTY;
        ItemStack stack = accelerationCards.get(index);
        accelerationCards.set(index, ItemStack.EMPTY);
        markDirty();
        return stack;
    }
    @Override public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < 0 || index >= accelerationCards.size()) return;
        ItemStack value = stack == null ? ItemStack.EMPTY : stack;
        if (!value.isEmpty() && !isAccelerationCard(value)) return;
        if (!value.isEmpty()) {
            value = value.copy();
            value.setCount(1);
        }
        accelerationCards.set(index, value);
        markDirty();
    }
    @Override public String getName() { return "container.gctcore.extended_assembler"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public int getInventoryStackLimit() { return 1; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) {
        return world != null && world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + .5D, pos.getY() + .5D,
                pos.getZ() + .5D) <= 64.0D;
    }
    @Override public void openInventory(EntityPlayer player) { }
    @Override public void closeInventory(EntityPlayer player) { }
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index >= 0 && index < ACCELERATION_SLOTS && isAccelerationCard(stack);
    }
    @Override public int getField(int id) {
        if (id == 0) return remainingTicks;
        if (id == 1) return batchCrafts;
        if (id == 2) return getParallelCraftLimit();
        return 0;
    }
    @Override public void setField(int id, int value) {
        if (id == 0) remainingTicks = value;
        else if (id == 1) batchCrafts = value;
    }
    @Override public int getFieldCount() { return 3; }
    @Override public void clear() { clearCards(); markDirty(); }

    private void clearCards() {
        for (int i = 0; i < accelerationCards.size(); i++) {
            accelerationCards.set(i, ItemStack.EMPTY);
        }
    }
}
