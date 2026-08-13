package com.smd.gctcore.common.mixin.mmceparallelequalizer;

import github.kasuminova.mmce.common.event.machine.MachineEvent;
import github.kasuminova.mmce.common.event.recipe.FactoryRecipeFinishEvent;
import github.kasuminova.mmce.common.event.recipe.FactoryRecipeStartEvent;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.tiles.TileFactoryController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Distributes the equalizer's otherwise discarded remainder over the first
 * factory thread slots while preserving the configured total parallelism.
 */
@Pseudo
@Mixin(targets = "com.gingeryj.mmceparallelequalizer.common.tile.TileParallelEqualizerHatch", remap = false)
public abstract class MixinTileParallelEqualizerHatch {

    @Unique
    private static final Map<ActiveMachineRecipe, Boolean> gctcore$initializedRecipes = new WeakHashMap<>();

    /**
     * Replaces the add-on's event handler. The original handler assigns only
     * {@code floor(maxParallelism / threadSlots)} to every slot and drops the
     * remainder.
     */
    @Inject(method = "onMachineEvent", at = @At("HEAD"), cancellable = true, require = 1)
    private void gctcore$distributeRemainder(MachineEvent event, CallbackInfo ci) {
        ci.cancel();

        if (event instanceof FactoryRecipeFinishEvent) {
            ActiveMachineRecipe activeRecipe = ((FactoryRecipeFinishEvent) event).getActiveRecipe();
            if (activeRecipe != null) {
                gctcore$forgetRecipe(activeRecipe);
            }
            return;
        }

        if (!(event instanceof FactoryRecipeStartEvent)) {
            return;
        }

        FactoryRecipeStartEvent startEvent = (FactoryRecipeStartEvent) event;
        RecipeCraftingContext context = startEvent.getContext();
        ActiveMachineRecipe activeRecipe = startEvent.getActiveRecipe();
        if (context == null || activeRecipe == null
                || !(startEvent.getController() instanceof TileFactoryController)) {
            return;
        }

        TileFactoryController factory = (TileFactoryController) startEvent.getController();
        int quota = gctcore$getQuota(factory, startEvent.getFactoryRecipeThread());
        if (quota < 1 || !gctcore$rememberRecipe(activeRecipe)) {
            return;
        }

        context.setParallelism(Math.min(Math.max(1, activeRecipe.getParallelism()), quota));
    }

    @Unique
    private static int gctcore$getQuota(TileFactoryController factory, FactoryRecipeThread recipeThread) {
        if (recipeThread == null) {
            return -1;
        }

        int normalSlots = Math.max(0, factory.getMaxThreads());
        Map<String, FactoryRecipeThread> coreThreads = factory.getCoreRecipeThreads();
        int coreSlots = coreThreads == null ? 0 : coreThreads.size();
        int threadSlots = Math.max(1, normalSlots + coreSlots);
        int maxParallelism = Math.max(1, factory.getMaxParallelism());
        int slotIndex = gctcore$getSlotIndex(factory, recipeThread, normalSlots, coreThreads);
        if (slotIndex < 0 || slotIndex >= threadSlots) {
            return -1;
        }

        int baseQuota = maxParallelism / threadSlots;
        int remainder = maxParallelism % threadSlots;
        return Math.max(1, baseQuota + (slotIndex < remainder ? 1 : 0));
    }

    @Unique
    private static int gctcore$getSlotIndex(TileFactoryController factory,
                                             FactoryRecipeThread recipeThread,
                                             int normalSlots,
                                             Map<String, FactoryRecipeThread> coreThreads) {
        if (!recipeThread.isCoreThread()) {
            List<FactoryRecipeThread> normalThreads = factory.getFactoryRecipeThreadList();
            return normalThreads == null ? -1 : normalThreads.indexOf(recipeThread);
        }

        if (coreThreads == null) {
            return -1;
        }

        int coreIndex = 0;
        for (FactoryRecipeThread coreThread : coreThreads.values()) {
            if (coreThread == recipeThread) {
                return normalSlots + coreIndex;
            }
            coreIndex++;
        }
        return -1;
    }

    @Unique
    private static boolean gctcore$rememberRecipe(ActiveMachineRecipe activeRecipe) {
        synchronized (gctcore$initializedRecipes) {
            return gctcore$initializedRecipes.put(activeRecipe, Boolean.TRUE) == null;
        }
    }

    @Unique
    private static void gctcore$forgetRecipe(ActiveMachineRecipe activeRecipe) {
        synchronized (gctcore$initializedRecipes) {
            gctcore$initializedRecipes.remove(activeRecipe);
        }
    }
}
