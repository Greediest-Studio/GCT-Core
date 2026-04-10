package com.smd.gctcore.common.mixin.gctmobs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.ResourceLocation;

@Pseudo
@Mixin(targets = "net.mcreator.gctmobs.world.WorldStarland$GenLayerBiomesCustom", remap = false)
public class MixinRemovePiscesOcean {
    @Inject(method = "getInts", at = @At("RETURN"), cancellable = true)
    private void onGetInts(int x, int z, int width, int depth, CallbackInfoReturnable<int[]> cir) {
        int[] ints = cir.getReturnValue();
        if (ints == null) return;
        try {
            Biome pisces = (Biome)Biome.REGISTRY.getObject(new ResourceLocation("gct_mobs:pisces_ocean"));
            if (pisces == null) return;
            int piscesId = Biome.getIdForBiome(pisces);
            for (int i = 0; i < ints.length; i++) {
                if (ints[i] == piscesId) {
                    ints[i] = getReplacementBiomeId(piscesId);
                }
            }
            cir.setReturnValue(ints);
        } catch (Throwable ignored) {}
    }

    private int getReplacementBiomeId(int bannedId) {
        for (int id = 0; id < Biome.REGISTRY.getKeys().size(); id++) {
            Biome b = Biome.getBiome(id);
            if (b != null && Biome.getIdForBiome(b) != bannedId) return Biome.getIdForBiome(b);
        }
        return 1;
    }
}
