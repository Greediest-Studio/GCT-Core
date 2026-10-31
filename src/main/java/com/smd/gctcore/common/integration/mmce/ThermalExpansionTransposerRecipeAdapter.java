package com.smd.gctcore.common.integration.mmce;

import cofh.thermalexpansion.util.managers.machine.TransposerManager;
import cofh.thermalexpansion.util.managers.machine.TransposerManager.TransposerRecipe;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Exposes Thermal Expansion Fluid Transposer fill recipes to MMCE machines. */
public final class ThermalExpansionTransposerRecipeAdapter extends RecipeAdapter {
    public static final ResourceLocation ADAPTER_NAME = new ResourceLocation("gctcore", "thermal_expansion_transposer");

    private static final int PROCESSING_TIME = 8 * 20;
    private static final long ENERGY_PER_TICK = 80L;

    public ThermalExpansionTransposerRecipeAdapter() {
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

        TransposerRecipe[] sourceRecipes = TransposerManager.getFillRecipeList();
        if (sourceRecipes == null) {
            return recipes;
        }

        int processingTime = Math.max(1, Math.round(RecipeModifier.applyModifiers(
                modifiers, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, PROCESSING_TIME, false)));

        for (TransposerRecipe source : sourceRecipes) {
            if (source == null) {
                continue;
            }

            ItemStack input = source.getInput();
            ItemStack output = source.getOutput();
            if (input == null || input.isEmpty() || output == null || output.isEmpty()
                    || source.getFluid() == null || source.getFluid().amount <= 0) {
                continue;
            }

            // The generic Transposer handler can fill any empty Forge fluid
            // container. Those container recipes are intentionally omitted;
            // this adapter is for material-transformation recipes only.
            if (isFillableEmptyContainer(input)) {
                continue;
            }

            ResourceLocation recipeName = new ResourceLocation(
                    "gctcore", "thermal_expansion_transposer_" + incId++);
            MachineRecipe recipe = createRecipeShell(recipeName, machineName, processingTime, 0, false);
            recipe.addRequirement(new RequirementItem(IOType.INPUT, input.copy()));
            recipe.addRequirement(new RequirementFluid(IOType.INPUT, source.getFluid().copy()));
            recipe.addRequirement(new RequirementItem(IOType.OUTPUT, output.copy()));
            recipe.addRequirement(new RequirementEnergy(IOType.INPUT, ENERGY_PER_TICK));
            addAdditionalRequirements(recipe, additionalRequirements, eventHandlers, tooltip);
            recipes.add(recipe);
        }
        return recipes;
    }

    private static boolean isFillableEmptyContainer(ItemStack stack) {
        IFluidHandler handler = FluidUtil.getFluidHandler(stack);
        if (handler == null) {
            return false;
        }
        IFluidTankProperties[] properties = handler.getTankProperties();
        if (properties == null || properties.length == 0) {
            return false;
        }
        for (IFluidTankProperties property : properties) {
            if (property == null || !property.canFill()) {
                return false;
            }
            if (property.getContents() != null && property.getContents().amount > 0) {
                return false;
            }
        }
        return true;
    }
}
