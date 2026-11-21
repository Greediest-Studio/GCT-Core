package com.smd.gctcore.common.mixin.lostcities;

import com.smd.gctcore.common.integration.lostcities.LostCitiesStatePalette;
import net.minecraft.util.ObjectIntIdentityMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "mcjty.lostcities.dimensions.world.LostCityChunkGenerator", remap = false)
public abstract class MixinLostCityChunkGenerator {

    @Redirect(
            method = {
                    "getHeightmap(II)Lmcjty/lostcities/dimensions/world/ChunkHeightmap;",
                    "getChunkPrimer(IIZ)Lnet/minecraft/world/chunk/ChunkPrimer;"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/ObjectIntIdentityMap;get(Ljava/lang/Object;)I",
                    remap = true
            ),
            require = 2,
            remap = false
    )
    private int gctcore$encodeBlockState(ObjectIntIdentityMap<?> map, Object value) {
        return LostCitiesStatePalette.getStateId(map, value);
    }
}
