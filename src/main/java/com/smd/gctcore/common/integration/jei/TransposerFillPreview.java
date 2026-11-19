package com.smd.gctcore.common.integration.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public final class TransposerFillPreview {
    private static final int EXAMPLES = 4;

    private TransposerFillPreview() {}

    public static boolean write(IIngredients display, @Nullable IFocus<?> focus, List<List<ItemStack>> inputs,
                                List<List<ItemStack>> outputs, List<List<FluidStack>> fluids) {
        if (inputs.size() != 1 || outputs.size() != 1 || fluids.size() != 1) return false;
        List<ItemStack> empty = inputs.get(0);
        List<ItemStack> filled = outputs.get(0);
        List<FluidStack> liquids = fluids.get(0);
        int size = liquids.size();
        if (size == 0 || size != filled.size() || size != empty.size()) return false;

        int start = 0;
        int count = Math.min(size, EXAMPLES);
        if (focus != null && focus.getMode() == IFocus.Mode.INPUT && focus.getValue() instanceof FluidStack) {
            FluidStack target = (FluidStack) focus.getValue();
            while (start < size && !target.isFluidEqual(liquids.get(start))) start++;
            count = 1;
        } else if (focus != null && focus.getMode() == IFocus.Mode.OUTPUT && focus.getValue() instanceof ItemStack) {
            ItemStack target = (ItemStack) focus.getValue();
            while (start < size) {
                ItemStack candidate = filled.get(start);
                if (ItemStack.areItemsEqual(target, candidate) && ItemStack.areItemStackTagsEqual(target, candidate)) break;
                start++;
            }
            count = 1;
        }
        // JEI may match by a custom subtype rather than exact NBT. Keep its original behavior on a miss.
        if (start == size) return false;

        // Each wrapper probes one container. Its output and fluid rows must use the same indices.
        display.setInput(VanillaTypes.ITEM, empty.get(start));
        display.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(filled.subList(start, start + count)));
        display.setInputLists(VanillaTypes.FLUID, Collections.singletonList(liquids.subList(start, start + count)));
        return true;
    }
}
