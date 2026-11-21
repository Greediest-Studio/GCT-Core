package com.smd.gctcore.common.mixin.lostcities;

import com.smd.gctcore.common.integration.lostcities.LostCitiesStatePalette;
import net.minecraft.util.ObjectIntIdentityMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "mcjty.lostcities.dimensions.world.lost.cityassets.CompiledPalette", remap = false)
public abstract class MixinCompiledPalette {

    @Redirect(
            method = "addPalettes([Lmcjty/lostcities/dimensions/world/lost/cityassets/Palette;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/ObjectIntIdentityMap;get(Ljava/lang/Object;)I",
                    remap = true
            ),
            require = 1,
            remap = false
    )
    private int gctcore$encodeBlockState(ObjectIntIdentityMap<?> map, Object value) {
        return LostCitiesStatePalette.getStateId(map, value);
    }

    @Redirect(
            method = "getStraight(C)Lnet/minecraft/block/state/IBlockState;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/ObjectIntIdentityMap;getByValue(I)Ljava/lang/Object;",
                    remap = true
            ),
            require = 1,
            remap = false
    )
    private Object gctcore$decodeBlockState(ObjectIntIdentityMap<?> map, int id) {
        return LostCitiesStatePalette.getStateById(map, id);
    }
}
