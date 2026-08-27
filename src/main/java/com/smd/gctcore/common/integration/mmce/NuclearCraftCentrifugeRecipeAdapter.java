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

/** Exposes NuclearCraft Overhauled centrifuge recipes to MMCE machines. */
public final class NuclearCraftCentrifugeRecipeAdapter extends RecipeAdapter {
    public static final ResourceLocation ADAPTER_NAME = new ResourceLocation("gctcore", "nuclearcraft_centrifuge");

    /** NuclearCraft's centrifuge has a 30 s base time; this adapter runs it at one tenth of that. */
    private static final int BASE_PROCESS_TIME = 3 * 20;
    private static final long ENERGY_PER_TICK = 150L;

    public NuclearCraftCentrifugeRecipeAdapter() {
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
        if (NCRecipes.centrifuge == null) {
            return recipes;
        }

        for (BasicRecipe source : NCRecipes.centrifuge.getRecipeList()) {
            int unmodifiedTime = Math.max(1, (int) Math.round(source.getBaseProcessTime(BASE_PROCESS_TIME)));
            int processingTime = Math.max(1, Math.round(RecipeModifier.applyModifiers(
                    modifiers, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, unmodifiedTime, false)));
            ResourceLocation recipeName = new ResourceLocation("gctcore", "nuclearcraft_centrifuge_" + incId++);
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
        for (IItemIngredient ingredient : ingredients) {
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
        for (IFluidIngredient ingredient : ingredients) {
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
