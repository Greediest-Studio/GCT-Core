package com.smd.gctcore.common.mixin.embers;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;

@Pseudo
@Mixin(targets = "teamroots.embers.compat.jei.EmbersJEIPlugin", remap = false)
public abstract class MixinEmbersJEIPlugin {
    @Redirect(
            method = "register(Lmezz/jei/api/IModRegistry;)V",
            at = @At(
                    value = "FIELD",
                    target = "Lteamroots/embers/recipe/RecipeRegistry;dawnstoneAnvilRecipes:Ljava/util/ArrayList;",
                    opcode = Opcodes.GETSTATIC
            ),
            remap = false,
            require = 1
    )
    private static ArrayList<?> gctcore$getEmptyDawnstoneAnvilRecipes() {
        return new ArrayList<>();
    }
}
