package com.smd.gctcore.common.mixin.ae2;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.container.implementations.ContainerCraftConfirm;
import com.smd.gctcore.common.integration.mmce.MMCE_CraftingConfirmBridge;
import com.smd.gctcore.common.integration.mmce.MMCE_CraftingRequester;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ContainerCraftConfirm.class)
public abstract class MixinContainerCraftConfirm implements MMCE_CraftingConfirmBridge {

    @Unique
    private MMCE_CraftingRequester gctcore$requester;

    @Override
    public void gctcore$setRequester(MMCE_CraftingRequester requester) {
        this.gctcore$requester = requester;
    }

    @Override
    public MMCE_CraftingRequester gctcore$getRequester() {
        return gctcore$requester;
    }

    @Redirect(
            method = "startJob",
            at = @At(value = "INVOKE", target = "Lappeng/api/networking/crafting/ICraftingGrid;submitJob(Lappeng/api/networking/crafting/ICraftingJob;Lappeng/api/networking/crafting/ICraftingRequester;Lappeng/api/networking/crafting/ICraftingCPU;ZLappeng/api/networking/security/IActionSource;)Lappeng/api/networking/crafting/ICraftingLink;"),
            remap = false
    )
    private ICraftingLink gctcore$submitMmceJob(ICraftingGrid craftingGrid, ICraftingJob job, ICraftingRequester originalRequester, ICraftingCPU cpu, boolean prioritizePower, IActionSource source) {
        ICraftingLink link = craftingGrid.submitJob(job, gctcore$requester == null ? originalRequester : gctcore$requester, cpu, prioritizePower, source);
        if (gctcore$requester != null && link != null) {
            gctcore$requester.gctcore$setCraftingLink(link);
        }
        return link;
    }
}
