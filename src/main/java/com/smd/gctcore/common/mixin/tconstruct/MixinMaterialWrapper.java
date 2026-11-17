package com.smd.gctcore.common.mixin.tconstruct;

import com.smd.gctcore.common.integration.jei.TConMaterialRecipeIndex;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.smeltery.MeltingRecipe;

import java.util.List;

@Pseudo
@Mixin(targets = "slimeknights.tconstruct.plugin.jei.material.MaterialWrapper", remap = false)
public abstract class MixinMaterialWrapper {
    @Shadow @Final
    private Material material;

    // 原方法继续执行流体匹配和原料获取，只缩小需要扫描的配方集合。
    @Redirect(method = "getInputs", at = @At(value = "INVOKE",
            target = "Lslimeknights/tconstruct/library/TinkerRegistry;getAllMeltingRecipies()Ljava/util/List;"), require = 1)
    private List<MeltingRecipe> gctcore$findMaterialRecipes() {
        return TConMaterialRecipeIndex.getRecipes(material.getFluid());
    }
}
