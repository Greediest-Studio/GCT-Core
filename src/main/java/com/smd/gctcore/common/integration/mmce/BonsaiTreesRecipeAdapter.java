package com.smd.gctcore.common.integration.mmce;

import com.smd.gctcore.gctcore;
import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * MMCE adapter for the original Bonsai Trees tree-type registry.
 *
 * <p>The adapter deliberately reads {@code IBonsaiTreeType#getDrops()} rather
 * than the CraftTweaker drop-modification registry.  Bonsai Trees is optional,
 * so its classes are resolved reflectively and GCT-Core does not hard-link to
 * the mod when Bonsai Trees is absent.</p>
 */
public final class BonsaiTreesRecipeAdapter extends RecipeAdapter {
    public static final ResourceLocation ADAPTER_NAME = new ResourceLocation("gctcore", "bonsaitrees");
    private static final int PROCESSING_TIME = 100;

    public BonsaiTreesRecipeAdapter() {
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

        Object bonsai = getStaticField("org.dave.bonsaitrees.BonsaiTrees", "instance");
        Object typeRegistry = bonsai == null ? null : getField(bonsai, "typeRegistry");
        if (typeRegistry == null) {
            gctcore.LOGGER.warn("Bonsai Trees is loaded, but its tree-type registry is unavailable");
            return recipes;
        }

        Iterable<?> treeTypes = invoke(typeRegistry, "getAllTypes");
        if (treeTypes == null) {
            gctcore.LOGGER.warn("Could not read the Bonsai Trees tree-type registry");
            return recipes;
        }

        int processingTime = Math.max(1, Math.round(RecipeModifier.applyModifiers(
                modifiers, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, PROCESSING_TIME, false)));

        for (Object treeType : treeTypes) {
            try {
                ItemStack sapling = copyStack((ItemStack) invoke(treeType, "getExampleStack"), 1);
                if (sapling.isEmpty()) {
                    continue;
                }

                ItemStack log = null;
                List<ItemStack> byproducts = new ArrayList<>();
                Iterable<?> drops = invoke(treeType, "getDrops");
                if (drops != null) {
                    for (Object drop : drops) {
                        ItemStack candidate = (ItemStack) getField(drop, "stack");
                        if (candidate == null || candidate.isEmpty() || candidate.getItem() == sapling.getItem()) {
                            continue;
                        }

                        ResourceLocation itemName = candidate.getItem().getRegistryName();
                        String path = itemName == null ? "" : itemName.getPath().toLowerCase();
                        if (log == null && (path.contains("log") || path.contains("wood"))) {
                            log = copyStack(candidate, 4);
                        } else if (!path.contains("log") && !path.contains("wood") && !path.contains("stick")) {
                            ItemStack byproduct = copyStack(candidate, 1);
                            if (!containsStack(byproducts, byproduct)) {
                                byproducts.add(byproduct);
                            }
                        }
                    }
                }

                // A valid bonsai recipe always has a log and at least one
                // non-sapling byproduct. Every distinct byproduct is emitted.
                if (log == null || byproducts.isEmpty()) {
                    continue;
                }

                Object rawTreeName = invoke(treeType, "getName");
                String treeName = rawTreeName instanceof String ? (String) rawTreeName : "unknown";
                String suffix = sanitize(treeName);
                ResourceLocation recipeName = new ResourceLocation("gctcore", "bonsaitrees_" + suffix + "_" + incId++);
                MachineRecipe recipe = createRecipeShell(recipeName, machineName, processingTime, 0, false);

                // Chance 0 keeps the sapling in the input bus, while the
                // requirement itself is still used to select the recipe.
                RequirementItem saplingInput = new RequirementItem(IOType.INPUT, sapling);
                saplingInput.setChance(0.0F);
                saplingInput.setParallelizeUnaffected(true);
                recipe.addRequirement(saplingInput);
                recipe.addRequirement(new RequirementItem(IOType.OUTPUT, log));
                recipe.addRequirement(new RequirementItem(IOType.OUTPUT, sapling));
                recipe.addRequirement(new RequirementItem(IOType.OUTPUT, new ItemStack(Items.STICK, 8, 0)));
                for (ItemStack byproduct : byproducts) {
                    recipe.addRequirement(new RequirementItem(IOType.OUTPUT, byproduct));
                }
                recipes.add(recipe);
            } catch (Throwable error) {
                // One malformed integration must not prevent all other trees from loading.
                gctcore.LOGGER.warn("Failed to create an MMCE recipe for a Bonsai Trees tree type", error);
            }
        }
        gctcore.LOGGER.info("Created {} Bonsai Trees recipes for MMCE machine {}", recipes.size(), machineName);
        return recipes;
    }

    private static String sanitize(String value) {
        String sanitized = value == null ? "unknown" : value.toLowerCase().replace(':', '_').replace('/', '_');
        return sanitized.replaceAll("[^a-z0-9_.-]", "_");
    }

    private static ItemStack copyStack(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    private static boolean containsStack(List<ItemStack> stacks, ItemStack candidate) {
        for (ItemStack stack : stacks) {
            if (ItemStack.areItemsEqual(stack, candidate) && ItemStack.areItemStackTagsEqual(stack, candidate)) {
                return true;
            }
        }
        return false;
    }

    private static Object getStaticField(String className, String fieldName) {
        try {
            Class<?> type = Class.forName(className);
            return type.getField(fieldName).get(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object getField(Object target, String fieldName) {
        if (target == null) {
            return null;
        }
        try {
            Field field = target.getClass().getField(fieldName);
            return field.get(target);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(Object target, String methodName, Object... arguments) {
        if (target == null) {
            return null;
        }
        try {
            for (Method method : target.getClass().getMethods()) {
                if (method.getName().equals(methodName) && method.getParameterTypes().length == arguments.length) {
                    return (T) method.invoke(target, arguments);
                }
            }
        } catch (Throwable ignored) {
            // Optional integration; treat unavailable methods as no data.
        }
        return null;
    }
}
