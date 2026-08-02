package com.smd.gctcore.common.integration.extendedcrafting;

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

public class TileExtendedMolecularAssembler extends TileExtendedGridMachine implements ITickable {
    private static final int CRAFT_TICKS = 20;
    private final NonNullList<ItemStack> inputs = NonNullList.create();
    private final NonNullList<ItemStack> outputs = NonNullList.create();
    private int remainingTicks;
    private long interfacePos;

    public TileExtendedMolecularAssembler() { }
    TileExtendedMolecularAssembler(ExtendedCraftingTier tier) { super(tier); }

    @Override protected BlockExtendedCraftingAutomation.Kind machineKind() { return BlockExtendedCraftingAutomation.Kind.ASSEMBLER; }
    @Override protected boolean requiresChannel() { return false; }

    public boolean isBusy() {
        return remainingTicks > 0 || !outputs.isEmpty();
    }

    public boolean start(TileExtendedInterface owner, NonNullList<ItemStack> taskInputs,
                         NonNullList<ItemStack> taskOutputs) {
        if (isBusy() || owner.tier() != tier || taskOutputs.isEmpty()) return false;
        copyInto(taskInputs, inputs);
        copyInto(taskOutputs, outputs);
        remainingTicks = CRAFT_TICKS;
        interfacePos = owner.getPos().toLong();
        markDirty();
        return true;
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) return;
        if (remainingTicks > 0 && --remainingTicks <= 0) finishCraft();
    }

    private void finishCraft() {
        TileEntity owner = world.getTileEntity(BlockPos.fromLong(interfacePos));
        if (owner instanceof TileExtendedInterface
                && ((TileExtendedInterface) owner).tier() == tier
                && ((TileExtendedInterface) owner).receiveAssemblerOutputs(copy(outputs))) {
            inputs.clear();
            outputs.clear();
        } else if (!(owner instanceof TileExtendedInterface)) {
            dropContents();
            inputs.clear();
            outputs.clear();
        } else {
            remainingTicks = 1;
        }
        markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("RemainingTicks", remainingTicks);
        compound.setLong("InterfacePos", interfacePos);
        compound.setTag("Inputs", writeList(inputs));
        compound.setTag("Outputs", writeList(outputs));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        remainingTicks = compound.getInteger("RemainingTicks");
        interfacePos = compound.getLong("InterfacePos");
        readList(compound.getTagList("Inputs", 10), inputs);
        readList(compound.getTagList("Outputs", 10), outputs);
    }

    public void dropContents() {
        if (world == null) return;
        // Before completion AE2 has supplied the inputs, but the outputs have not been earned yet.
        // Once the timer reaches zero only the finished outputs may be recovered.
        if (remainingTicks > 0) {
            for (ItemStack stack : inputs) drop(stack);
        } else {
            for (ItemStack stack : outputs) drop(stack);
        }
        inputs.clear();
        outputs.clear();
    }

    private void drop(ItemStack stack) {
        if (!stack.isEmpty()) InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    private static void copyInto(List<ItemStack> source, NonNullList<ItemStack> destination) {
        destination.clear();
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

    private static void readList(NBTTagList list, NonNullList<ItemStack> destination) {
        destination.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack stack = new ItemStack(list.getCompoundTagAt(i));
            if (!stack.isEmpty()) destination.add(stack);
        }
    }
}
