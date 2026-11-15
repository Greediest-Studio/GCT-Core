package com.smd.gctcore.common.mixin.tcomplement;

import com.smd.gctcore.common.integration.jei.MeltingOverrideIndex;
import knightminer.tcomplement.library.IBlacklist;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.smeltery.MeltingRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Pseudo
@Mixin(targets = "knightminer.tcomplement.plugin.jei.MeltingRecipeGetter", remap = false)
public abstract class MixinMeltingRecipeGetter {
    @Shadow
    private static boolean isValid(MeltingRecipe recipe) {
        throw new AssertionError("Mixin shadow");
    }

    @Inject(method = "getMelterRecipes", at = @At("HEAD"), cancellable = true, require = 1)
    private static void gctcore$melter(List<MeltingRecipe> recipes,
                                     CallbackInfoReturnable<List<MeltingRecipe>> cir) {
        cir.setReturnValue(gctcore$filter(recipes, AccessorTCompRegistry.gctcore$meltingOverrides(),
                AccessorTCompRegistry.gctcore$meltingBlacklist()));
    }

    @Inject(method = "getHighOvenRecipes", at = @At("HEAD"), cancellable = true, require = 1)
    private static void gctcore$highOven(List<MeltingRecipe> recipes,
                                       CallbackInfoReturnable<List<MeltingRecipe>> cir) {
        cir.setReturnValue(gctcore$filter(recipes, AccessorTCompRegistry.gctcore$highOvenOverrides(),
                Collections.emptyList()));
    }

    @Unique
    private static List<MeltingRecipe> gctcore$filter(List<MeltingRecipe> recipes,
                                                    List<MeltingRecipe> overrides,
                                                    List<IBlacklist> blacklists) {
        MeltingOverrideIndex index = new MeltingOverrideIndex(overrides);
        List<MeltingRecipe> result = new ArrayList<>(recipes.size() + overrides.size());
        for (MeltingRecipe override : overrides) {
            if (isValid(override)) result.add(override);
        }
        for (MeltingRecipe recipe : recipes) {
            List<ItemStack> inputs = recipe.input.getInputs();
            boolean hidden = false;
            for (IBlacklist blacklist : blacklists) {
                for (ItemStack input : inputs) {
                    if (blacklist.matches(input)) {
                        hidden = true;
                        break;
                    }
                }
                if (hidden) break;
            }
            if (!hidden && !index.matchesAny(inputs)) result.add(recipe);
        }
        return result;
    }
}
