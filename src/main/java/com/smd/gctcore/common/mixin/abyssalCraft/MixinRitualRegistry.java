package com.smd.gctcore.common.mixin.abyssalCraft;

import com.shinoow.abyssalcraft.api.ritual.RitualRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;

@Mixin(RitualRegistry.class)
public abstract class MixinRitualRegistry {

    @Shadow(remap = false)
    @Final
    private final Map<Integer, Integer> configDimToBookType = new HashMap<>();
    @Shadow(remap = false)
    @Final
    private final Map<Integer, Integer> dimToBookType = new HashMap<>();

    /**
     * @author Gct-Core
     * @reason 修复报空
     */
    @Overwrite(remap = false)
    public boolean sameBookType(int dim, int bookType){
        if (!dimToBookType.containsKey(dim) && !configDimToBookType.containsKey(dim)) return false;
        return bookType == (dimToBookType.get(dim) == null ? 0 : dimToBookType.get(dim)) || bookType == (configDimToBookType.get(dim) == null ? 0 : configDimToBookType.get(dim));
    }
}
