package com.smd.gctcore.common.mixin.gctmobs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityLockableLoot;

/**
 * Fixes the KabalahBuilder output-slot duplication exploit.
 *
 * <p>Automation pipes access TileEntity inventories via Forge's {@code IItemHandler} capability,
 * which is wrapped by {@code InvWrapper}. {@code InvWrapper.extractItem} calls
 * {@code setInventorySlotContents} directly — it never triggers the GUI slot's {@code onTake}
 * callback and therefore bypasses the normal input-consumption logic entirely.
 *
 * <p>The vanilla GUI path, on the other hand, calls {@code decrStackSize} via
 * {@code Container.slotClick → Slot.decrStackSize → IInventory.decrStackSize}, and then
 * fires {@code onTake} which sends a packet that invokes
 * {@code ProcedureProKabalanBuilderTake} (concurrently cancelled by
 * {@link MixinProcedureProKabalanBuilderTake}).
 *
 * <p>This mixin hooks <em>both</em> paths with a per-instance boolean flag
 * ({@code gctcore$inputsConsumed}) to guarantee inputs are consumed exactly once per
 * output-slot cycle:
 * <ol>
 *   <li>{@code setInventorySlotContents} — catches {@code InvWrapper}-based pipe extraction.</li>
 *   <li>{@code decrStackSize} — catches direct GUI slot clicks (and any other caller that
 *       uses the {@code IInventory} API rather than {@code IItemHandler}).</li>
 *   <li>When slot 13 receives a new (non-empty) item the flag is reset, so the next
 *       extraction cycle will consume inputs again.</li>
 * </ol>
 */
@Pseudo
@Mixin(targets = "net.mcreator.gctmobs.block.BlockKabalahBuilderBlock$TileEntityCustom", remap = false)
public abstract class MixinTileEntityKabalahBuilder extends TileEntityLockableLoot {

    /**
     * Tracks whether inputs have already been consumed for the current output item.
     * Prevents double-consumption when both code paths happen to fire in the same cycle.
     */
    @Unique
    private boolean gctcore$inputsConsumed = false;

    /**
     * Intercepts {@code setInventorySlotContents} — the method called by
     * {@code InvWrapper.extractItem} when a pipe pulls items from the TileEntity.
     *
     * <ul>
     *   <li>If slot 13 is being given a <em>non-empty</em> item (recipe produced output),
     *       reset the consumed flag so the next extraction will consume inputs.</li>
     *   <li>If slot 13 is being set to <em>empty</em> (full extraction by a pipe),
     *       consume one item from each input slot 0-9, provided inputs haven't already
     *       been consumed this cycle.</li>
     * </ul>
     */
    @Inject(method = "setInventorySlotContents", at = @At("HEAD"))
    private void gctcore$onSetOutputSlot(int index, ItemStack stack, CallbackInfo ci) {
        if (index != 13) return;

        if (stack != null && !stack.isEmpty()) {
            // A new output item was placed in slot 13 — reset the flag for the next cycle.
            gctcore$inputsConsumed = false;
            return;
        }

        // Slot 13 is being set to empty.
        // Only act on the server, and only if slot 13 actually had something.
        if (this.world == null || this.world.isRemote) return;
        if (getStackInSlot(13).isEmpty()) return;
        if (gctcore$inputsConsumed) return;

        gctcore$inputsConsumed = true;
        for (int i = 0; i <= 9; i++) {
            decrStackSize(i, 1);
        }
    }

    /**
     * Intercepts {@code decrStackSize} — the method called by the vanilla
     * {@code Container.slotClick} path when a player takes items from slot 13 via the GUI.
     *
     * <p>Fires <em>after</em> the parent method has updated the internal item list.
     * Only consumes inputs when the slot has been fully emptied and the flag is clear.
     */
    @Inject(method = "decrStackSize", at = @At("RETURN"))
    private void gctcore$onDecrOutputSlot(int index, int count, CallbackInfoReturnable<ItemStack> cir) {
        if (index != 13) return;
        if (this.world == null || this.world.isRemote) return;

        ItemStack taken = cir.getReturnValue();
        if (taken == null || taken.isEmpty()) return;   // nothing was actually taken
        if (!getStackInSlot(13).isEmpty()) return;      // slot not yet fully emptied
        if (gctcore$inputsConsumed) return;

        gctcore$inputsConsumed = true;
        for (int i = 0; i <= 9; i++) {
            decrStackSize(i, 1);
        }
    }
}
