package com.smd.gctcore.common.integration.mmce;

import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Registers the adapter during MMCE's recipe-adapter registry event. */
public final class BonsaiTreesRecipeAdapterRegistry {
    @SubscribeEvent
    public void register(RegistryEvent.Register<RecipeAdapter> event) {
        if (Loader.isModLoaded("bonsaitrees")) {
            event.getRegistry().register(new BonsaiTreesRecipeAdapter());
        }
    }
}
