package com.smd.gctcore.common.mixin.endertweaker;

import com.enderio.core.common.util.NNList;
import com.smd.gctcore.misc.recipe.GCTRecipeInput;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crazypants.enderio.base.recipe.IRecipeInput;
import net.minecraft.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import shadows.endertweaker.RecipeUtils;

@Mixin(value = RecipeUtils.class, remap = false)
public abstract class MixinRecipeUtils {

    /**
     * @author smd
     * @reason 修复 endertweaker 报错
     */
    @Overwrite
    public static NNList<IRecipeInput> toEIOInputsNN(IIngredient[] inputs) {
        NNList<IRecipeInput> ret = new NNList<>();
        for (IIngredient input : inputs) {
            Ingredient mcIngredient = CraftTweakerMC.getIngredient(input);
            int amount = input.getAmount();
            ret.add(new GCTRecipeInput(mcIngredient, amount));
        }
        return ret;
    }
}