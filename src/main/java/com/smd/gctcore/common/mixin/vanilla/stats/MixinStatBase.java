package com.smd.gctcore.common.mixin.vanilla.stats;

import com.smd.gctcore.common.stats.ILazyStatBase;
import com.smd.gctcore.common.stats.LazyStatNameHelper;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatBase.class)
public abstract class MixinStatBase implements ILazyStatBase {

    @Shadow
    @Final
    public String statId;

    @Shadow
    @Final
    @Mutable
    private ITextComponent statName;

    @Unique
    private byte gctcore$lazyKind;

    @Override
    public void gctcore$markLazy(byte kind) {
        this.gctcore$lazyKind = kind;
        this.statName = null;
    }

    @Override
    public void gctcore$ensureStatName() {
        if (this.statName == null && this.gctcore$lazyKind != LazyStatNameHelper.KIND_NONE) {
            synchronized (this) {
                if (this.statName == null && this.gctcore$lazyKind != LazyStatNameHelper.KIND_NONE) {
                    this.statName = ((ILazyStatBase) this).gctcore$createLazyStatName(this.gctcore$lazyKind);
                    this.gctcore$lazyKind = LazyStatNameHelper.KIND_NONE;
                }
            }
        }
    }

    @Override
    public ITextComponent gctcore$createLazyStatName(byte kind) {
        return new TextComponentString(this.statId);
    }

    @Inject(method = "registerStat", at = @At("HEAD"))
    private void gctcore$beforeRegisterStat(CallbackInfoReturnable<StatBase> cir) {
        StatBase existing = StatList.getOneShotStat(this.statId);
        if (existing != null) {
            ((ILazyStatBase) existing).gctcore$ensureStatName();
            this.gctcore$ensureStatName();
        }
    }

    @Inject(method = "getStatName", at = @At("HEAD"))
    private void gctcore$beforeGetStatName(CallbackInfoReturnable<ITextComponent> cir) {
        this.gctcore$ensureStatName();
    }

    @Inject(method = "toString", at = @At("HEAD"))
    private void gctcore$beforeToString(CallbackInfoReturnable<String> cir) {
        this.gctcore$ensureStatName();
    }
}
