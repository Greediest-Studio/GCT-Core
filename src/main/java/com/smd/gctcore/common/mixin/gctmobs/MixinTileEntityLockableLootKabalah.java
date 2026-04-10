package com.smd.gctcore.common.mixin.gctmobs;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes the KabalahBuilder output-slot duplication exploit by targeting
 * {@link TileEntityLockableLoot} — the superclass in which {@code decrStackSize}
 * and {@code setInventorySlotContents} are actually <em>declared</em>.
 *
 * <h3>Why the previous approach failed</h3>
 * <p>{@code BlockKabalahBuilderBlock.TileEntityCustom} does NOT override
 * {@code decrStackSize} or {@code setInventorySlotContents}; both are inherited
 * from {@code TileEntityLockableLoot}. Mixin injection requires the method to be
 * present in the target class's own bytecode — injecting into a subclass for an
 * inherited (non-overriding) method silently produces zero injection points and the
 * handler is never called at runtime.
 *
 * <h3>Two extraction paths</h3>
 * <ul>
 *   <li><b>GUI (player click):</b> {@code Container.slotClick} → {@code Slot.decrStackSize}
 *       → {@code IInventory.decrStackSize} → caught by
 *       {@link #gctcore$kabalahOnDecrSlot}.</li>
 *   <li><b>Pipe/hopper (automation):</b> {@code IItemHandler.extractItem} via Forge's
 *       {@code InvWrapper} → {@code IInventory.setInventorySlotContents(slot, EMPTY)}
 *       → caught by {@link #gctcore$kabalahOnSetSlot}.</li>
 * </ul>
 *
 * <h3>Double-consumption prevention</h3>
 * <p>The {@code @Unique} boolean {@code gctcore$kabalahInputsConsumed} is set to
 * {@code true} the first time inputs are consumed for a given output item, and
 * reset to {@code false} when a new (non-empty) item is placed into slot 13 by
 * {@code ProcedureProKabalahBuilderRecipe}. This guarantees exactly one input
 * consumption per output cycle regardless of which code path fires first.
 *
 * <p>{@code ProcedureProKabalanBuilderTake} is cancelled by
 * {@link MixinProcedureProKabalanBuilderTake} to prevent double-deduction on the
 * GUI path (which would otherwise consume inputs a second time via the network
 * packet mechanism).
 *
 * <h3>Runtime guard</h3>
 * <p>Because this mixin targets the common base class, every {@code TileEntityLockableLoot}
 * in the game carries the injected code. The first line of each handler compares
 * {@code this.getClass().getName()} against the fully-qualified inner-class name of
 * {@code TileEntityCustom} and returns immediately for all other tile entities,
 * so the overhead on unrelated inventories is a single string comparison per call.
 */
@Mixin(TileEntityLockableLoot.class)
public abstract class MixinTileEntityLockableLootKabalah {

    // -----------------------------------------------------------------------
    // Shadowed methods — declared abstract here; resolved to the real
    // TileEntityLockableLoot / TileEntity implementations at runtime.
    // -----------------------------------------------------------------------

    @Shadow public abstract ItemStack getStackInSlot(int index);

    /**
     * Shadow of {@code TileEntityLockableLoot.decrStackSize}.
     * Called from the inject handlers (for inputs 0-9) — those recursive calls
     * are harmlessly short-circuited by the {@code index != 13} guard.
     */
    @Shadow public abstract ItemStack decrStackSize(int index, int count);

    // -----------------------------------------------------------------------
    // Unique state
    // -----------------------------------------------------------------------

    /** Per-instance flag: have inputs already been consumed for this output item? */
    @Unique
    private boolean gctcore$kabalahInputsConsumed = false;

    // -----------------------------------------------------------------------
    // Target class identifier (inner class uses '$' separator)
    // -----------------------------------------------------------------------
    @Unique
    private static final String KABALAH_TE =
            "net.mcreator.gctmobs.block.BlockKabalahBuilderBlock$TileEntityCustom";

    // -----------------------------------------------------------------------
    // Injection: setInventorySlotContents (HEAD)
    // Catches the automation-pipe path (InvWrapper.extractItem → setInventorySlotContents).
    // -----------------------------------------------------------------------

    @Inject(method = "setInventorySlotContents", at = @At("HEAD"))
    private void gctcore$kabalahOnSetSlot(int index, ItemStack stack, CallbackInfo ci) {
        // Quick type-guard — bail out for all TileEntityLockableLoot that are NOT the KabalahBuilder
        if (!this.getClass().getName().equals(KABALAH_TE)) return;
        if (index != 13) return;

        if (stack != null && !stack.isEmpty()) {
            // A new output item is being placed by the recipe procedure → reset for next cycle.
            gctcore$kabalahInputsConsumed = false;
            return;
        }

        // stack is empty (or null) → a pipe is clearing slot 13.
        World world = ((TileEntity) (Object) this).getWorld();
        if (world == null || world.isRemote) return;

        // Only act on a "non-empty → empty" transition.
        // At HEAD the slot still holds the old value, so check it's currently non-empty.
        if (getStackInSlot(13).isEmpty()) return;

        if (gctcore$kabalahInputsConsumed) return;
        gctcore$kabalahInputsConsumed = true;

        for (int i = 0; i <= 9; i++) {
            decrStackSize(i, 1);
        }
    }

    // -----------------------------------------------------------------------
    // Injection: decrStackSize (RETURN)
    // Catches the GUI-player path (Container.slotClick → Slot.decrStackSize).
    // -----------------------------------------------------------------------

    @Inject(method = "decrStackSize", at = @At("RETURN"))
    private void gctcore$kabalahOnDecrSlot(int index, int count,
            CallbackInfoReturnable<ItemStack> cir) {
        // Quick type-guard
        if (!this.getClass().getName().equals(KABALAH_TE)) return;
        if (index != 13) return;

        World world = ((TileEntity) (Object) this).getWorld();
        if (world == null || world.isRemote) return;

        // Only act if something was actually taken out.
        ItemStack taken = cir.getReturnValue();
        if (taken == null || taken.isEmpty()) return;

        // Only act when the slot is now fully empty (handles stacked output correctly).
        if (!getStackInSlot(13).isEmpty()) return;

        if (gctcore$kabalahInputsConsumed) return;
        gctcore$kabalahInputsConsumed = true;

        for (int i = 0; i <= 9; i++) {
            decrStackSize(i, 1);
        }
    }
}
