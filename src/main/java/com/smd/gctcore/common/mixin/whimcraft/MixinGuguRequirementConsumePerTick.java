package com.smd.gctcore.common.mixin.whimcraft;

import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Lets GuGu's per-tick aspect requirement start before the WhimCraft ME bus
 * has filled its local cache.
 *
 * <p>WhimCraft registers real essentia demand from its startCrafting callback.
 * GuGu Utils 0.9.2 otherwise probes the empty cache first, rejects the recipe
 * (and every parallelism candidate), and therefore never reaches that
 * callback.</p>
 */
@Pseudo
@Mixin(
        targets = "com.warmthdawn.mod.gugu_utils.modularmachenary.requirements.basic.RequirementConsumePerTick",
        remap = false
)
public abstract class MixinGuguRequirementConsumePerTick {

    @Unique
    private static final String GCTCORE$ASPECT_REQUIREMENT =
            "com.warmthdawn.mod.gugu_utils.modularmachenary.requirements.RequirementAspect";

    @Unique
    private static final String GCTCORE$WHIMCRAFT_INPUT_BUS =
            "com.xinyihl.whimcraft.common.title.TitleMEAspectInputBus";

    @Inject(
            method = "canStartCrafting(" +
                    "Ljava/util/List;" +
                    "Lhellfirepvp/modularmachinery/common/crafting/helper/RecipeCraftingContext;)" +
                    "Lhellfirepvp/modularmachinery/common/crafting/helper/CraftCheck;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void gctcore$allowDeferredAspectInput(
            List<ProcessingComponent<?>> components,
            RecipeCraftingContext context,
            CallbackInfoReturnable<CraftCheck> cir
    ) {
        if (gctcore$isAspectRequirementWithInput(components)) {
            cir.setReturnValue(CraftCheck.success());
        }
    }

    @Inject(
            method = "getMaxParallelism(" +
                    "Ljava/util/List;" +
                    "Lhellfirepvp/modularmachinery/common/crafting/helper/RecipeCraftingContext;I)I",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void gctcore$allowDeferredAspectParallelism(
            List<ProcessingComponent<?>> components,
            RecipeCraftingContext context,
            int maxParallelism,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (gctcore$isAspectRequirementWithInput(components)) {
            cir.setReturnValue(Math.max(0, maxParallelism));
        }
    }

    @Unique
    private boolean gctcore$isAspectRequirementWithInput(List<ProcessingComponent<?>> components) {
        if (!GCTCORE$ASPECT_REQUIREMENT.equals(getClass().getName()) || components == null) {
            return false;
        }

        for (ProcessingComponent<?> component : components) {
            if (component == null) {
                continue;
            }
            Object holder = component.getProvidedComponent();
            if (holder == null) {
                continue;
            }
            try {
                Field consumableField = holder.getClass().getField("consumable");
                Object consumable = consumableField.get(holder);
                if (consumable != null &&
                        GCTCORE$WHIMCRAFT_INPUT_BUS.equals(consumable.getClass().getName())) {
                    return true;
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Not GuGu's CraftingResourceHolder, so it cannot be this bus.
            }
        }
        return false;
    }
}
