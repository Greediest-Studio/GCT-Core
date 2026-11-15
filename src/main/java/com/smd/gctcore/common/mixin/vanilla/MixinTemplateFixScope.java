package com.smd.gctcore.common.mixin.vanilla;

import com.smd.gctcore.common.util.TemplateFixScope;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.IFixType;
import net.minecraft.world.gen.structure.template.TemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TemplateManager.class)
public abstract class MixinTemplateFixScope {
    @Redirect(method = "readTemplateFromStream", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/datafix/DataFixer;process(Lnet/minecraft/util/datafix/IFixType;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;"), require = 1)
    private NBTTagCompound gctcore$fixTemplate(DataFixer fixer, IFixType type, NBTTagCompound compound) {
        return TemplateFixScope.process(fixer, type, compound);
    }
}
