package com.smd.gctcore.common.mixin.vanilla;

import net.minecraft.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Ingredient.class)
public interface IngredientInvoker {

    @Invoker(value = "invalidate", remap = false)
    void gctcore$invalidate();
}
