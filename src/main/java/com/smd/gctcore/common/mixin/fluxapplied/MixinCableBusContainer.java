package com.smd.gctcore.common.mixin.fluxapplied;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartModel;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.parts.CableBusContainer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CableBusContainer.class)
public abstract class MixinCableBusContainer {

    private static final String FLUX_PART_PACKAGE = "com.flux_applied.part.";

    @Inject(method = "getRenderState", at = @At("RETURN"), remap = false)
    private void gctcore$stabilizeFluxPartRenderState(
            CallbackInfoReturnable<CableBusRenderState> cir
    ) {
        CableBusContainer container = (CableBusContainer) (Object) this;
        CableBusRenderState renderState = cir.getReturnValue();

        for (EnumFacing facing : EnumFacing.values()) {
            IPart part = container.getPart(facing);
            if (part == null || !part.getClass().getName().startsWith(FLUX_PART_PACKAGE)) {
                continue;
            }

            renderState.getPartFlags().put(facing, gctcore$getModelFlag(part));
        }
    }

    private static long gctcore$getModelFlag(IPart part) {
        long flag = part.getClass().getName().hashCode();
        IPartModel model = part.getStaticModels();
        if (model != null) {
            flag = 31L * flag + (model.requireCableConnection() ? 1L : 0L);
            for (ResourceLocation resource : model.getModels()) {
                flag = 31L * flag + resource.hashCode();
            }
        }
        return flag;
    }
}
