package com.smd.gctcore.common.mixin.whimcraft;

import com.smd.gctcore.gctcore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import java.lang.reflect.Method;

/** Prevents MMCE recipe simulations from creating real essentia demand. */
@Pseudo
@Mixin(targets = "com.xinyihl.whimcraft.common.title.TitleMEAspectInputBus", remap = false)
public abstract class MixinTitleMEAspectInputBus {

    @Unique
    private boolean gctcore$loggedSuccessfulExtraction;

    @Unique
    private boolean gctcore$loggedFailedExtraction;

    @Unique
    private boolean gctcore$loggedInvocationFailure;

    @Unique
    private static Method gctcore$takeAspectFromMEMethod;

    @Shadow
    private AspectList recipeEssentia;

    @Shadow
    public AspectList essentia;

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

    /**
     * Pulls missing essentia immediately when the real per-tick IO runs.
     *
     * <p>WhimCraft normally relies on {@code startCrafting} to register a
     * demand and on its independent 20-tick tile update to fill the cache.
     * MMCE/GuGu can reach the IO callback without that notification, leaving
     * the cache permanently empty. The original consume method has registered
     * the real demand by the time it reads the local cache, so fill that demand
     * here before it decides whether the token can be consumed.</p>
     */
    @Redirect(
            method = "consume(" +
                    "Lcom/warmthdawn/mod/gugu_utils/modularmachenary/requirements/RequirementAspect$RT;" +
                    "Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/api/aspects/AspectList;getAmount(" +
                            "Lthaumcraft/api/aspects/Aspect;)I",
                    ordinal = 1
            ),
            remap = false,
            require = 1
    )
    private int gctcore$fillCacheForRealIO(
            AspectList localEssentia,
            Aspect aspect,
            @Coerce Object requirementToken,
            boolean doOperation
    ) {
        int available = localEssentia.getAmount(aspect);
        if (!doOperation) {
            return available;
        }

        int missing = recipeEssentia.getAmount(aspect) - available;
        if (missing <= 0) {
            return available;
        }

        int extracted = gctcore$takeAspectFromME(aspect, missing);
        if (extracted > 0) {
            localEssentia.add(aspect, extracted);
            available += extracted;
            if (!gctcore$loggedSuccessfulExtraction) {
                gctcore$loggedSuccessfulExtraction = true;
                gctcore.LOGGER.info(
                        "WhimCraft GuGu ME aspect bus compatibility fix extracted {} {} essentia",
                        extracted,
                        aspect.getTag()
                );
            }
        } else if (!gctcore$loggedFailedExtraction) {
            gctcore$loggedFailedExtraction = true;
            gctcore.LOGGER.warn(
                    "WhimCraft GuGu ME aspect bus compatibility fix ran, but AE returned 0/{} {} essentia",
                    missing,
                    aspect.getTag()
            );
        }
        return available;
    }

    @Unique
    private int gctcore$takeAspectFromME(Aspect aspect, int amount) {
        try {
            Method method = gctcore$takeAspectFromMEMethod;
            if (method == null) {
                method = getClass().getMethod(
                        "takeAspectFromME",
                        Aspect.class,
                        Integer.TYPE,
                        Boolean.TYPE
                );
                gctcore$takeAspectFromMEMethod = method;
            }
            Object result = method.invoke(this, aspect, amount, true);
            return result instanceof Number ? ((Number) result).intValue() : 0;
        } catch (ReflectiveOperationException | LinkageError error) {
            if (!gctcore$loggedInvocationFailure) {
                gctcore$loggedInvocationFailure = true;
                gctcore.LOGGER.error(
                        "Failed to invoke WhimCraft ME aspect extraction from the GuGu bus compatibility fix",
                        error
                );
            }
            return 0;
        }
    }
}
