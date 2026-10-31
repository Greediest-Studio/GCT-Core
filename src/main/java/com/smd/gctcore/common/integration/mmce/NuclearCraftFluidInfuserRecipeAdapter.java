package com.smd.gctcore.common.integration.mmce;

import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import nc.recipe.BasicRecipe;
import nc.recipe.NCRecipes;
import nc.recipe.ingredient.IChanceFluidIngredient;
import nc.recipe.ingredient.IChanceItemIngredient;
import nc.recipe.ingredient.IFluidIngredient;
import nc.recipe.ingredient.IItemIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Exposes NuclearCraft fluid infuser (fluid injector) recipes to MMCE machines. */
public final class NuclearCraftFluidInfuserRecipeAdapter extends RecipeAdapter {
    public static final ResourceLocation ADAPTER_NAME = new ResourceLocation("gctcore", "nuclearcraft_fluid_infuser");

    private static final int PROCESSING_TIME = 8 * 20;
    private static final long ENERGY_PER_TICK = 80L;

    public NuclearCraftFluidInfuserRecipeAdapter() {
        super(ADAPTER_NAME);
    }

    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation machineName,
                                                       List<RecipeModifier> modifiers,
                                                       List<ComponentRequirement<?, ?>> additionalRequirements,
                                                       Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                       List<String> tooltip) {
        List<MachineRecipe> recipes = new ArrayList<>();
        resetIncId();
        if (NCRecipes.infuser == null) {
            return recipes;
        }

        int processingTime = Math.max(1, Math.round(RecipeModifier.applyModifiers(
                modifiers, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, PROCESSING_TIME, false)));
        for (BasicRecipe source : NCRecipes.infuser.getRecipeList()) {
            if (source == null) {
                continue;
            }

            ResourceLocation recipeName = new ResourceLocation("gctcore", "nuclearcraft_fluid_infuser_" + incId++);
            MachineRecipe recipe = createRecipeShell(recipeName, machineName, processingTime, 0, false);
            addItems(recipe, IOType.INPUT, source.getItemIngredients());
            addFluids(recipe, IOType.INPUT, source.getFluidIngredients());
            addItems(recipe, IOType.OUTPUT, source.getItemProducts());
            addFluids(recipe, IOType.OUTPUT, source.getFluidProducts());
            recipe.addRequirement(new RequirementEnergy(IOType.INPUT, ENERGY_PER_TICK));
            addAdditionalRequirements(recipe, additionalRequirements, eventHandlers, tooltip);
            recipes.add(recipe);
        }
        return recipes;
    }

    private static void addItems(MachineRecipe recipe, IOType ioType, List<IItemIngredient> ingredients) {
        if (ingredients == null) {
            return;
        }
        for (IItemIngredient ingredient : ingredients) {
            if (ingredient == null) {
                continue;
            }
            ItemStack stack = ingredient.getStack();
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            RequirementItem requirement = new RequirementItem(ioType, stack.copy());
            if (ioType == IOType.OUTPUT && ingredient instanceof IChanceItemIngredient) {
                requirement.setChance(((IChanceItemIngredient) ingredient).getChancePercent() / 100F);
            }
            recipe.addRequirement(requirement);
        }
    }

    private static void addFluids(MachineRecipe recipe, IOType ioType, List<IFluidIngredient> ingredients) {
        if (ingredients == null) {
            return;
        }
        for (IFluidIngredient ingredient : ingredients) {
            if (ingredient == null) {
                continue;
            }
            FluidStack stack = ingredient.getStack();
            if (stack == null || stack.amount <= 0) {
                continue;
            }
            RequirementFluid requirement = new RequirementFluid(ioType, stack.copy());
            if (ioType == IOType.OUTPUT && ingredient instanceof IChanceFluidIngredient) {
                requirement.setChance(((IChanceFluidIngredient) ingredient).getChancePercent() / 100F);
            }
            recipe.addRequirement(requirement);
        }
    }
}
