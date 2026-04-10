package com.smd.gctcore.common.mixin.enderio;

import com.enderio.core.common.util.NNList;
import crazypants.enderio.base.recipe.BasicManyToOneRecipe;
import crazypants.enderio.base.recipe.IRecipe;
import crazypants.enderio.base.recipe.IManyToOneRecipe;
import crazypants.enderio.base.recipe.MachineRecipeInput;
import crazypants.enderio.base.recipe.Recipe;
import crazypants.enderio.base.recipe.RecipeLevel;
import crazypants.enderio.base.recipe.alloysmelter.AlloyRecipeManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nonnull;

@Mixin(value = AlloyRecipeManager.class, remap = false)
public abstract class MixinAlloyRecipeManager {

    @Unique
    private final NNList<IManyToOneRecipe> gctcore$recipes = new NNList<>();

    @Shadow(remap = false)
    private void addJEIIntegration(@Nonnull IManyToOneRecipe recipe) {
    }

    /**
     * Convert a plain {@link Recipe} into an {@link IManyToOneRecipe} and add it.
     *
     * @param recipe the recipe to convert and add, must not be null
     * @author shiver
     * @reason Convert vanilla Recipe instances into IManyToOneRecipe and add to manager
     */
    @Overwrite(remap = false)
    private void addDedupedRecipe(@Nonnull Recipe recipe) {
        addRecipe(new BasicManyToOneRecipe(recipe));
    }

    /**
     * Add an {@link IManyToOneRecipe} to the internal recipe list and register
     * it with JEI integration.
     *
     * @param recipe the recipe to add, must not be null
     * @author shiver
     * @reason Maintain internal recipe list and ensure JEI integration is registered
     */
    @Overwrite(remap = false)
    private void addRecipe(@Nonnull IManyToOneRecipe recipe) {
        gctcore$recipes.add(recipe);
        addJEIIntegration(recipe);
    }

    /**
     * Find a matching recipe for the given machine level and inputs.
     *
     * @param machineLevel the machine's recipe level, must not be null
     * @param inputs the list of machine inputs to match against, must not be null
     * @return a matching {@link IRecipe} or {@code null} if none found
     * @author shiver
     * @reason Search the internal recipe list for a recipe matching provided inputs
     */
    @Overwrite(remap = false)
    public IRecipe getRecipeForInputs(@Nonnull RecipeLevel machineLevel, @Nonnull NNList<MachineRecipeInput> inputs) {
        for (IManyToOneRecipe recipe : gctcore$recipes) {
            if (machineLevel.canMake(recipe.getRecipeLevel()) && recipe.isInputForRecipe(inputs)) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Check whether the given machine input is valid for any recipe at the
     * specified machine level.
     *
     * @param machineLevel the machine's recipe level, must not be null
     * @param input the machine input to validate, must not be null
     * @return {@code true} if the input is valid for at least one recipe
     * @author smd
     * @reason Validate a machine input against recipes maintained by this mixin
     */
    @Overwrite(remap = false)
    public boolean isValidInput(@Nonnull RecipeLevel machineLevel, @Nonnull MachineRecipeInput input) {
        for (IManyToOneRecipe recipe : gctcore$recipes) {
            if (machineLevel.canMake(recipe.getRecipeLevel())) {
                for (crazypants.enderio.base.recipe.IRecipeInput ri : recipe.getInputs()) {
                    if (ri.isInput(input.item)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determine if the provided item stack components represent a valid recipe
     * for the given machine level.
     *
     * @param machineLevel the machine's recipe level, must not be null
     * @param input the item stack components to check, must not be null
     * @return {@code true} if the components form a valid recipe
     * @author shiver
     * @reason Delegate component validation to the internal recipe list
     */
    @Overwrite(remap = false)
    public boolean isValidRecipeComponents(@Nonnull RecipeLevel machineLevel, @Nonnull NNList<ItemStack> input) {
        for (IManyToOneRecipe recipe : gctcore$recipes) {
            if (machineLevel.canMake(recipe.getRecipeLevel()) && recipe.isValidRecipeComponents(input)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the experience reward for producing the specified output item.
     *
     * @param output the output item stack to query, must not be null
     * @return experience value associated with the output, or 0 if unknown
     * @author shiver
     * @reason Retrieve experience value from internal recipe outputs
     */
    @Overwrite(remap = false)
    public float getExperienceForOutput(@Nonnull ItemStack output) {
        for (IManyToOneRecipe recipe : gctcore$recipes) {
            if (recipe.getOutput().getItem() == output.getItem() && recipe.getOutput().getItemDamage() == output.getItemDamage()) {
                return recipe.getOutputs()[0].getExperiance();
            }
        }
        return 0;
    }

    /**
     * Get a copy of the currently known many-to-one recipes.
     *
     * @return a non-null list containing the registered recipes
     * @author shiver
     * @reason Provide a defensive copy of the internal recipe list
     */
    @Overwrite(remap = false)
    public @Nonnull NNList<IManyToOneRecipe> getRecipes() {
        NNList<IManyToOneRecipe> result = new NNList<>();
        result.addAll(gctcore$recipes);
        return result;
    }

    /**
     * Rebuild the internal recipe collection or report its current size.
     *
     * @return the number of recipes currently registered
     * @author shiver
     * @reason Return current recipe count; no complex rebuild required here
     */
    @Overwrite(remap = false)
    public int rebuild() {
        return gctcore$recipes.size();
    }
}
