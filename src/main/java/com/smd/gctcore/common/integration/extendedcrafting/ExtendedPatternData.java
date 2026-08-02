package com.smd.gctcore.common.integration.extendedcrafting;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public final class ExtendedPatternData {
    private static final String TAG_PATTERN = "ExtendedCraftingPattern";
    private static final String TAG_TIER = "Tier";
    private static final String TAG_GRID = "Grid";
    private static final String TAG_INPUTS = "Inputs";
    private static final String TAG_OUTPUT = "Output";
    private static final String TAG_EXTRA_OUTPUTS = "ExtraOutputs";

    private ExtendedPatternData() {
    }

    public static ItemStack encode(ItemStack blank, ExtendedCraftingTier tier, List<ItemStack> grid, World world) {
        ExtendedCraftingRecipeBridge.Match match = ExtendedCraftingRecipeBridge.findMatch(tier, grid, world);
        if (match == null) {
            return ItemStack.EMPTY;
        }
        ItemStack encoded = new ItemStack(ExtendedCraftingAutomation.encodedPattern(tier));
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger(TAG_TIER, tier.level());
        data.setTag(TAG_GRID, writeFixedGrid(grid));
        data.setTag(TAG_INPUTS, writeList(compress(grid)));
        data.setTag(TAG_OUTPUT, match.output().writeToNBT(new NBTTagCompound()));
        data.setTag(TAG_EXTRA_OUTPUTS, writeList(compress(match.remaining())));
        root.setTag(TAG_PATTERN, data);
        encoded.setTagCompound(root);
        return encoded;
    }

    public static boolean isEncoded(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemEncodedExtendedPattern
                && stack.hasTagCompound() && stack.getTagCompound().hasKey(TAG_PATTERN, 10);
    }

    public static ExtendedCraftingTier tier(ItemStack stack) {
        if (!isEncoded(stack)) {
            return ExtendedCraftingTier.BASIC;
        }
        return ExtendedCraftingTier.byLevel(data(stack).getInteger(TAG_TIER));
    }

    public static boolean isValid(ItemStack stack, World world) {
        if (!isEncoded(stack)) {
            return false;
        }
        if (((ItemEncodedExtendedPattern) stack.getItem()).tier() != tier(stack)) {
            return false;
        }
        ExtendedCraftingRecipeBridge.Match match = ExtendedCraftingRecipeBridge.findMatch(tier(stack), readGrid(stack), world);
        return match != null && sameStack(readOutput(stack), match.output());
    }

    public static List<ItemStack> readGrid(ItemStack stack) {
        ExtendedCraftingTier tier = tier(stack);
        List<ItemStack> grid = new ArrayList<>(tier.gridSize() * tier.gridSize());
        for (int i = 0; i < tier.gridSize() * tier.gridSize(); i++) {
            grid.add(ItemStack.EMPTY);
        }
        if (!isEncoded(stack)) {
            return grid;
        }
        NBTTagList list = data(stack).getTagList(TAG_GRID, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            int slot = entry.getInteger("Slot");
            if (slot >= 0 && slot < grid.size()) {
                grid.set(slot, new ItemStack(entry.getCompoundTag("Item")));
            }
        }
        return grid;
    }

    public static List<ItemStack> readInputs(ItemStack stack) {
        return readList(stack, TAG_INPUTS);
    }

    public static ItemStack readOutput(ItemStack stack) {
        return isEncoded(stack) ? new ItemStack(data(stack).getCompoundTag(TAG_OUTPUT)) : ItemStack.EMPTY;
    }

    public static List<ItemStack> readOutputs(ItemStack stack) {
        List<ItemStack> outputs = new ArrayList<>();
        ItemStack primary = readOutput(stack);
        if (!primary.isEmpty()) {
            outputs.add(primary);
        }
        outputs.addAll(readList(stack, TAG_EXTRA_OUTPUTS));
        return outputs;
    }

    private static NBTTagCompound data(ItemStack stack) {
        return stack.getTagCompound().getCompoundTag(TAG_PATTERN);
    }

    private static List<ItemStack> readList(ItemStack stack, String key) {
        List<ItemStack> result = new ArrayList<>();
        if (!isEncoded(stack)) {
            return result;
        }
        NBTTagList list = data(stack).getTagList(key, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack item = new ItemStack(list.getCompoundTagAt(i));
            if (!item.isEmpty()) {
                result.add(item);
            }
        }
        return result;
    }

    private static NBTTagList writeFixedGrid(List<ItemStack> grid) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < grid.size(); i++) {
            ItemStack stack = grid.get(i);
            if (stack != null && !stack.isEmpty()) {
                ItemStack one = stack.copy();
                one.setCount(1);
                NBTTagCompound entry = new NBTTagCompound();
                entry.setInteger("Slot", i);
                entry.setTag("Item", one.writeToNBT(new NBTTagCompound()));
                list.appendTag(entry);
            }
        }
        return list;
    }

    private static NBTTagList writeList(List<ItemStack> stacks) {
        NBTTagList list = new NBTTagList();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                list.appendTag(stack.writeToNBT(new NBTTagCompound()));
            }
        }
        return list;
    }

    private static List<ItemStack> compress(List<ItemStack> source) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack sourceStack : source) {
            if (sourceStack == null || sourceStack.isEmpty()) {
                continue;
            }
            ItemStack stack = sourceStack.copy();
            ItemStack existing = findStack(result, stack);
            if (existing == null) {
                result.add(stack);
            } else {
                existing.grow(stack.getCount());
            }
        }
        return result;
    }

    private static ItemStack findStack(List<ItemStack> stacks, ItemStack wanted) {
        for (ItemStack stack : stacks) {
            if (ItemHandlerHelper.canItemStacksStack(stack, wanted)) {
                return stack;
            }
        }
        return null;
    }

    private static boolean sameStack(ItemStack first, ItemStack second) {
        return !first.isEmpty() && !second.isEmpty() && first.getCount() == second.getCount()
                && ItemHandlerHelper.canItemStacksStack(first, second);
    }
}
