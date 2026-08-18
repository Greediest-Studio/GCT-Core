package com.smd.gctcore.common.mixin.whimcraft;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.api.aspects.Aspect;

/**
 * Fixes WhimCraft 0.1.4 reporting a simulated partial extraction as real.
 *
 * <p>The original method first simulates a request. If AE can only satisfy
 * part of it, the method returns immediately and never performs the modulated
 * extraction. If AE can satisfy all of it, the method performs a second call.
 * Make the first call authoritative and turn that now-redundant second call
 * into a no-op.</p>
 */
@Pseudo
@Mixin(targets = "com.xinyihl.whimcraft.common.title.base.TitleMEAspectBus", remap = false)
public abstract class MixinTitleMEAspectBus {

    @Unique
    private static final String GCTCORE$EXTRACT_ITEMS =
            "Lappeng/api/storage/IMEMonitor;extractItems(" +
            "Lappeng/api/storage/data/IAEStack;" +
            "Lappeng/api/config/Actionable;" +
            "Lappeng/api/networking/security/IActionSource;)" +
            "Lappeng/api/storage/data/IAEStack;";

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(
            method = "takeAspectFromME(Lthaumcraft/api/aspects/Aspect;IZ)I",
            at = @At(value = "INVOKE", target = GCTCORE$EXTRACT_ITEMS, ordinal = 0),
            remap = false,
            require = 1
    )
    private IAEStack gctcore$performRequestedExtraction(
            IMEMonitor monitor,
            IAEStack request,
            Actionable ignoredAction,
            IActionSource source,
            Aspect aspect,
            int amount,
            boolean doOperation
    ) {
        return monitor.extractItems(
                request,
                doOperation ? Actionable.MODULATE : Actionable.SIMULATE,
                source
        );
    }

    @SuppressWarnings("rawtypes")
    @Redirect(
            method = "takeAspectFromME(Lthaumcraft/api/aspects/Aspect;IZ)I",
            at = @At(value = "INVOKE", target = GCTCORE$EXTRACT_ITEMS, ordinal = 1),
            remap = false,
            // GuGu Utils 0.9.3 may have already collapsed this to one call.
            require = 0
    )
    private IAEStack gctcore$skipDuplicateExtraction(
            IMEMonitor monitor,
            IAEStack extracted,
            Actionable ignoredAction,
            IActionSource source
    ) {
        return extracted;
    }
}
