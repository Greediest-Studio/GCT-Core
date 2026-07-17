package com.smd.gctcore.common.mixin.hei;

import java.util.stream.Stream;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.plugins.vanilla.anvil.AnvilRecipeMaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AnvilRecipeMaker.class, remap = false)
public abstract class MixinAnvilRecipeMaker {

    @Inject(
            method = "getBookEnchantmentRecipes(Lmezz/jei/api/ingredients/IIngredientRegistry;)Ljava/util/stream/Stream;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private static void gctcore$disableBookEnchantmentRecipes(
            IIngredientRegistry ingredientRegistry,
            CallbackInfoReturnable<Stream<IRecipeWrapper>> cir
    ) {
        cir.setReturnValue(Stream.empty());
    }
}