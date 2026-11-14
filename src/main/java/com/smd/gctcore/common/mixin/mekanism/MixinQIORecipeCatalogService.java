package com.smd.gctcore.common.mixin.mekanism;

import mekanism.qioprocessing.common.content.workbench.QIOWorkbenchConfiguration;
import mekanism.qioprocessing.common.planning.QIORecipeCatalogService;
import mekanism.qioprocessing.common.planning.QIOWorkbenchRecipeCatalog;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lazily initializes QIORecipeCatalogService on first snapshot/state access after
 * MekanismQIOProcessing.serverStarted skips the eager startup scan.
 */
@Mixin(value = QIORecipeCatalogService.class, remap = false)
public abstract class MixinQIORecipeCatalogService {

    @Shadow
    private QIOWorkbenchRecipeCatalog.Snapshot snapshot;

    @Shadow
    private World world;

    @Shadow
    public abstract void refresh(World world);

    @Inject(
            method = "getSnapshot()Lmekanism/qioprocessing/common/planning/QIOWorkbenchRecipeCatalog$Snapshot;",
            at = @At("HEAD"),
            remap = false
    )
    private void gctcore$lazyInitSnapshot(CallbackInfoReturnable<QIOWorkbenchRecipeCatalog.Snapshot> cir) {
        this.gctcore$ensureInitializedForSnapshot();
    }

    @Inject(
            method = "getState()Lmekanism/qioprocessing/common/planning/QIORecipeCatalogService$State;",
            at = @At("HEAD"),
            remap = false
    )
    private void gctcore$lazyInitState(CallbackInfoReturnable<QIORecipeCatalogService.State> cir) {
        this.gctcore$ensureInitializedForSnapshot();
    }

    @Inject(
            method = "getSnapshot(Lmekanism/qioprocessing/common/content/workbench/QIOWorkbenchConfiguration;)Lmekanism/qioprocessing/common/planning/QIOWorkbenchRecipeCatalog$Snapshot;",
            at = @At("HEAD"),
            remap = false
    )
    private void gctcore$lazyInitConfiguredSnapshot(
            QIOWorkbenchConfiguration configuration,
            CallbackInfoReturnable<QIOWorkbenchRecipeCatalog.Snapshot> cir
    ) {
        this.gctcore$ensureInitializedForConfig();
    }

    @Unique
    private void gctcore$ensureInitializedForSnapshot() {
        if (this.snapshot == null) {
            World targetWorld = this.world != null ? this.world : DimensionManager.getWorld(0);
            if (targetWorld != null) {
                this.refresh(targetWorld);
            }
        }
    }

    @Unique
    private void gctcore$ensureInitializedForConfig() {
        if (this.snapshot == null && this.world == null) {
            World overworld = DimensionManager.getWorld(0);
            if (overworld != null) {
                this.refresh(overworld);
            }
        }
    }
}
