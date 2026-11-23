package com.smd.gctcore.common.mixin.hei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "mezz.jei.render.IngredientListBatchRenderer", remap = false)
public abstract class MixinIngredientListBatchRenderer {

    @WrapOperation(method = "renderImpl", at = {
            @At(value = "INVOKE", target = "Lmezz/jei/render/ItemStackFastRenderer;isAnimated()Z"),
            @At(value = "INVOKE", target = "Lmezz/jei/render/IngredientRenderer;isAnimated()Z"),
            @At(value = "INVOKE", target = "Lmezz/jei/render/CollapsedGroupRenderer;isAnimated()Z")
    }, require = 6, remap = false)
    private boolean gctcore$skipUnneededAnimationCheck(@Coerce Object renderer, Operation<Boolean> original,
            Minecraft minecraft, @Coerce Enum<?> pass) {
        // ALL 会绘制所有物品，无需读取纹理帧；避免触发 LoliASM 已释放纹理的重新加载。
        return "ALL".equals(pass.name()) || original.call(renderer);
    }
}
