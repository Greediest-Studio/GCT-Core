package com.smd.gctcore.common.mixin.whimcraft;

import com.xinyihl.whimcraft.common.integration.adapter.tc6.AdapterTC6Crucible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IThaumcraftRecipe;

import java.util.List;
import java.util.Map;

@Mixin(value = AdapterTC6Crucible.class, remap = false)
public class MixinAdapterTC6Crucible {

    @Inject(method = "lambda$createRecipesFor$2", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gctcore$skipRecipeWithInvalidCatalyst(
            List<?> modifiers,
            ResourceLocation machineName,
            Map<?, ?> pcbMap,
            List<?> recipes,
            ResourceLocation recipeName,
            IThaumcraftRecipe tcRecipe,
            CallbackInfo ci
    ) {
        if (!(tcRecipe instanceof CrucibleRecipe)) {
            return;
        }

        Ingredient catalyst = ((CrucibleRecipe) tcRecipe).getCatalyst();
        if (catalyst == null) {
            return;
        }

        ItemStack[] stacks = catalyst.getMatchingStacks();
        if (gctcore$isInvalidCatalystStacks(stacks)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean gctcore$isInvalidCatalystStacks(ItemStack[] stacks) {
        if (stacks == null || stacks.length == 0) {
            return true;
        }

        for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
