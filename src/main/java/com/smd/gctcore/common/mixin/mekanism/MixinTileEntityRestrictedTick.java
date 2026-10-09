package com.smd.gctcore.common.mixin.mekanism;

import com.smd.gctcore.common.integration.mekanism.TimeAcceleratedUpdateAccess;
import mekanism.common.tile.base.TileEntityRestrictedTick;
import net.minecraft.util.ITickable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Allows a known time accelerator to request one additional Mekanism tick.
 */
@Mixin(value = TileEntityRestrictedTick.class, remap = false)
public abstract class MixinTileEntityRestrictedTick implements TimeAcceleratedUpdateAccess {

    @Shadow
    private long lastUpdateWorldTick;

    @Override
    public void gct$timeAcceleratedUpdate() {
        // Keep Mekanism's normal one-tick guard for ordinary callers.
        lastUpdateWorldTick = Long.MIN_VALUE;
        try {
            for (java.lang.reflect.Method method : ITickable.class.getMethods()) {
                if (method.getParameterTypes().length == 0) {
                    method.invoke(this);
                    return;
                }
            }
            throw new NoSuchMethodException("ITickable tick method");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to invoke tickable update", e);
        }
    }
}
