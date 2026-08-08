package com.smd.gctcore.common.integration.extendedcrafting;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ExtendedCraftingPatternDetails implements ICraftingPatternDetails {
    private final ItemStack pattern;
    private final IAEItemStack[] inputs;
    private final IAEItemStack[] outputs;
    private int priority;

    public ExtendedCraftingPatternDetails(ItemStack pattern, World world) {
        this.pattern = pattern.copy();
        this.inputs = toAeStacks(ExtendedPatternData.readInputs(pattern));
        this.outputs = ExtendedPatternData.isValid(pattern, world)
                ? toAeStacks(ExtendedPatternData.readOutputs(pattern)) : new IAEItemStack[0];
    }

    @Override public ItemStack getPattern() { return pattern.copy(); }
    @Override public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) { return true; }
    @Override public boolean isCraftable() { return false; }
    @Override public IAEItemStack[] getInputs() { return copy(inputs); }
    @Override public IAEItemStack[] getCondensedInputs() { return copy(inputs); }
    @Override public IAEItemStack[] getOutputs() { return copy(outputs); }
    @Override public IAEItemStack[] getCondensedOutputs() { return copy(outputs); }
    @Override public boolean canSubstitute() { return false; }
    @Override public ItemStack getOutput(InventoryCrafting craftingInv, World world) {
        return ExtendedPatternData.isValid(pattern, world) ? ExtendedPatternData.readOutput(pattern) : ItemStack.EMPTY;
    }
    @Override public int getPriority() { return priority; }
    @Override public void setPriority(int priority) { this.priority = priority; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ExtendedCraftingPatternDetails)) return false;
        ItemStack other = ((ExtendedCraftingPatternDetails) object).pattern;
        return ItemStack.areItemsEqual(pattern, other) && ItemStack.areItemStackTagsEqual(pattern, other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern.getItem(), pattern.getMetadata(), pattern.getTagCompound());
    }

    private static IAEItemStack[] toAeStacks(List<ItemStack> source) {
        List<IAEItemStack> result = new ArrayList<>();
        IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
        for (ItemStack stack : source) {
            IAEItemStack aeStack = channel.createStack(stack);
            if (aeStack != null) result.add(aeStack);
        }
        return result.toArray(new IAEItemStack[result.size()]);
    }

    private static IAEItemStack[] copy(IAEItemStack[] source) {
        IAEItemStack[] result = new IAEItemStack[source.length];
        for (int i = 0; i < source.length; i++) {
            result[i] = source[i] == null ? null : source[i].copy();
        }
        return result;
    }
}
