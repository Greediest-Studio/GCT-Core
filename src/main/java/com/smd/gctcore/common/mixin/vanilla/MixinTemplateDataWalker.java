package com.smd.gctcore.common.mixin.vanilla;

import com.smd.gctcore.common.util.TemplateBlockDataFixes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IFixType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.gen.structure.template.Template$2")
public abstract class MixinTemplateDataWalker {
    @Redirect(method = "process", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/datafix/IDataFixer;process(Lnet/minecraft/util/datafix/IFixType;Lnet/minecraft/nbt/NBTTagCompound;I)Lnet/minecraft/nbt/NBTTagCompound;"),
            require = 2)
    private NBTTagCompound gctcore$fixTemplateNbt(IDataFixer fixer, IFixType type,
                                                NBTTagCompound compound, int version) {
        return TemplateBlockDataFixes.process(fixer, type, compound, version);
    }
}
