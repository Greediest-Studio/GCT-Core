package com.smd.gctcore.common.mixin.lostcities;

import com.smd.gctcore.common.integration.lostcities.LostCitiesStatePalette;
import net.minecraft.util.ObjectIntIdentityMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "mcjty.lostcities.dimensions.world.terraingen.IslandTerrainGenerator", remap = false)
public abstract class MixinIslandTerrainGenerator {

    @Redirect(
            method = "genBiomeTerrain(Lnet/minecraft/world/biome/Biome;Lnet/minecraft/world/chunk/ChunkPrimer;IIDLmcjty/lostcities/dimensions/world/terraingen/LostCitiesTerrainGenerator;)V",
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
}
