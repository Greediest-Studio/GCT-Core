package com.smd.gctcore.common.integration.mmce;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;

public interface MMCE_CraftingRequester extends ICraftingRequester {

    void gctcore$setCraftingLink(ICraftingLink link);
}
