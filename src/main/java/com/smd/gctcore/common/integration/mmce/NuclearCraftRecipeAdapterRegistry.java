package com.smd.gctcore.common.integration.mmce;

import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Registers NuclearCraft recipe adapters when NuclearCraft is present. */
public final class NuclearCraftRecipeAdapterRegistry {
    @SubscribeEvent
    public void register(RegistryEvent.Register<RecipeAdapter> event) {
        if (Loader.isModLoaded("nuclearcraft")) {
            event.getRegistry().register(new NuclearCraftCentrifugeRecipeAdapter());
        }
    }
}
