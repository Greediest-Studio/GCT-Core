package com.smd.gctcore.common.mixin.vanilla.stats;

import com.smd.gctcore.common.stats.ILazyStatBase;
import com.smd.gctcore.common.stats.LazyStatNameHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatCrafting;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatCrafting.class)
public abstract class MixinStatCrafting implements ILazyStatBase {

    @Shadow
    @Final
    private Item item;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gctcore$onConstruct(String statPrefix, String statSuffix, ITextComponent statNameIn, Item itemIn, CallbackInfo ci) {
        byte kind = LazyStatNameHelper.detectLazyKind(statNameIn);
        if (kind != LazyStatNameHelper.KIND_NONE) {
            this.gctcore$markLazy(kind);
        }
    }

    @Override
    public ITextComponent gctcore$createLazyStatName(byte kind) {
        String key = LazyStatNameHelper.getTranslationKey(kind);
        if (key == null || this.item == null) {
            return new TextComponentString(((StatBase) (Object) this).statId);
        }
        return new TextComponentTranslation(key, new ItemStack(this.item).getTextComponent());
    }
}
