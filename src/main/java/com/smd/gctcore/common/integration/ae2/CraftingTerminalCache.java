package com.smd.gctcore.common.integration.ae2;

import net.minecraft.item.crafting.IRecipe;

import javax.annotation.Nullable;

/** 普通和无线合成终端共用当前配方及一次批量合成的状态。 */
public interface CraftingTerminalCache {

    @Nullable
    IRecipe gctcore$getCachedRecipe();

    boolean gctcore$isBatchCrafting();

    void gctcore$setBatchCrafting(boolean crafting);
}
