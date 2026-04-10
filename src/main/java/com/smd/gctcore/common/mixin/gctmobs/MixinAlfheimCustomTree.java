package com.smd.gctcore.common.mixin.gctmobs;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
    targets = {
        "net.mcreator.gctmobs.world.biome.BiomeAlfheimForest$CustomTree",
        "net.mcreator.gctmobs.world.biome.BiomeAlfheimPlain$CustomTree",
        "net.mcreator.gctmobs.world.biome.BiomeAlfheimPlainHill$CustomTree"
    },
    remap = false
)
public abstract class MixinAlfheimCustomTree {

    @Inject(method = "addVines", at = @At("HEAD"), cancellable = true)
    private void gctcore$skipVinePlacement(World world, BlockPos pos, CallbackInfo ci) {
        // Cancel vine placement to keep Alfheim trees vine-free
        ci.cancel();
    }
}
