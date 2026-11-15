package com.smd.gctcore.common.mixin.tcomplement;

import knightminer.tcomplement.library.IBlacklist;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import slimeknights.tconstruct.library.smeltery.MeltingRecipe;

import java.util.List;

@Pseudo
@Mixin(targets = "knightminer.tcomplement.library.TCompRegistry", remap = false)
public interface AccessorTCompRegistry {
    @Accessor("meltingOverrides")
    static List<MeltingRecipe> gctcore$meltingOverrides() {
        throw new AssertionError("Mixin accessor");
    }

    @Accessor("highOvenOverrides")
    static List<MeltingRecipe> gctcore$highOvenOverrides() {
        throw new AssertionError("Mixin accessor");
    }

    @Accessor("meltingBlacklist")
    static List<IBlacklist> gctcore$meltingBlacklist() {
        throw new AssertionError("Mixin accessor");
    }
}
