package com.smd.gctcore.common.mixin.whimcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

/** Prevents MMCE recipe simulations from creating real essentia demand. */
@Pseudo
@Mixin(targets = "com.xinyihl.whimcraft.common.title.TitleMEAspectInputBus", remap = false)
public abstract class MixinTitleMEAspectInputBus {

    @Redirect(
            method = "consume(" +
                    "Lcom/warmthdawn/mod/gugu_utils/modularmachenary/requirements/RequirementAspect$RT;" +
                    "Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/api/aspects/AspectList;getAmount(" +
                            "Lthaumcraft/api/aspects/Aspect;)I",
                    ordinal = 0
            ),
            remap = false,
            require = 1
    )
    private int gctcore$registerDemandOnlyForRealIO(
            AspectList recipeEssentia,
            Aspect aspect,
            @Coerce Object requirementToken,
            boolean doOperation
    ) {
        return doOperation ? recipeEssentia.getAmount(aspect) : 1;
    }
}
