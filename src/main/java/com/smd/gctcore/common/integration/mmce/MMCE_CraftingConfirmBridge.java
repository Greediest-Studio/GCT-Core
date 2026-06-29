package com.smd.gctcore.common.integration.mmce;

public interface MMCE_CraftingConfirmBridge {

    void gctcore$setRequester(MMCE_CraftingRequester requester);

    MMCE_CraftingRequester gctcore$getRequester();
}
