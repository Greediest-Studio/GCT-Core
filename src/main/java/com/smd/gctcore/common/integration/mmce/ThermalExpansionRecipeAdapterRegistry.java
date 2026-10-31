package com.smd.gctcore.common.integration.mmce;

import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Registers Thermal Expansion recipe adapters when Thermal Expansion is present. */
public final class ThermalExpansionRecipeAdapterRegistry {
    @SubscribeEvent
    public void register(RegistryEvent.Register<RecipeAdapter> event) {
        if (Loader.isModLoaded("thermalexpansion")) {
            event.getRegistry().register(new ThermalExpansionTransposerRecipeAdapter());
        }
    }
}
