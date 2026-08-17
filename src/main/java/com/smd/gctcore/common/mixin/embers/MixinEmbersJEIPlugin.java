package com.smd.gctcore.common.mixin.embers;

import mezz.jei.api.IModRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Pseudo
@Mixin(targets = "teamroots.embers.compat.jei.EmbersJEIPlugin", remap = false)
public abstract class MixinEmbersJEIPlugin {

    private static final String DAWNSTONE_ANVIL_CATEGORY = "embers.dawnstone_anvil";

    @Redirect(
            method = "register(Lmezz/jei/api/IModRegistry;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/IModRegistry;addRecipes(Ljava/util/List;Ljava/lang/String;)V"
            ),
            remap = false,
            require = 1
    )
    private void gctcore$skipDawnstoneAnvilRecipes(
            IModRegistry registry,
            List<?> recipes,
            String category
    ) {
        if (!DAWNSTONE_ANVIL_CATEGORY.equals(category)) {
            registry.addRecipes(recipes, category);
        }
    }
}
