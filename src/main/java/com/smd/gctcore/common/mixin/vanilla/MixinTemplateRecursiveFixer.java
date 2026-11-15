package com.smd.gctcore.common.mixin.vanilla;

import com.smd.gctcore.common.util.TemplateFixScope;
import net.minecraft.util.datafix.IDataFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.minecraftforge.common.util.CompoundDataFixer$1", remap = false)
public abstract class MixinTemplateRecursiveFixer {
    @ModifyArg(method = {"process", "func_188251_a"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/datafix/IDataWalker;process(Lnet/minecraft/util/datafix/IDataFixer;Lnet/minecraft/nbt/NBTTagCompound;I)Lnet/minecraft/nbt/NBTTagCompound;",
            remap = true), index = 0, require = 1)
    private IDataFixer gctcore$templateFixer(IDataFixer fixer) {
        return TemplateFixScope.wrap(fixer);
    }
}
