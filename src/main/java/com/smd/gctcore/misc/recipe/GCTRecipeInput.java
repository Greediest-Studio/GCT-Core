package com.smd.gctcore.misc.recipe;

import crazypants.enderio.base.recipe.IRecipeInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class GCTRecipeInput implements IRecipeInput {

    private final Ingredient ingredient;
    private final int initialAmount;
    private int remainingAmount;
    private final int slot;
    private final float multiplier;

    public GCTRecipeInput(Ingredient ingredient, int amount) {
        this(ingredient, amount, -1, 1.0F);
    }

    public GCTRecipeInput(Ingredient ingredient, int amount, int slot, float multiplier) {
        this.ingredient = ingredient;
        this.initialAmount = amount;
        this.remainingAmount = amount;
        this.slot = slot;
        this.multiplier = multiplier;
    }

    @Nonnull
    @Override
    public IRecipeInput copy() {
        GCTRecipeInput copy = new GCTRecipeInput(ingredient, initialAmount, slot, multiplier);
        copy.remainingAmount = this.remainingAmount;
        return copy;
    }

    @Override
    public boolean isFluid() {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getInput() {
        ItemStack[] stacks = ingredient.getMatchingStacks();
        if (stacks.length == 0) return ItemStack.EMPTY;
        ItemStack stack = stacks[0].copy();
        stack.setCount(initialAmount);
        return stack;
    }

    @Override
    public FluidStack getFluidInput() {
        return null;
    }

    @Override
    public float getMulitplier() {
        return multiplier;
    }

    @Override
    public int getSlotNumber() {
        return slot;
    }

    @Override
    public boolean isInput(@Nonnull ItemStack test) {
        return ingredient.apply(test);
    }

    @Override
    public boolean isInput(FluidStack test) {
        return false;
    }

    @Override
    public ItemStack[] getEquivelentInputs() {
        ItemStack[] stacks = ingredient.getMatchingStacks();
        ItemStack[] copies = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            copies[i] = stacks[i].copy();
            copies[i].setCount(initialAmount);
        }
        return copies;
    }

    @Override
    public boolean isValid() {
        return ingredient.getMatchingStacks().length > 0 && initialAmount > 0;
    }

    @Override
    public void shrinkStack(int count) {
        this.remainingAmount -= count;
        if (this.remainingAmount < 0) this.remainingAmount = 0;
    }

    @Override
    public int getStackSize() {
        return this.remainingAmount;
    }
}