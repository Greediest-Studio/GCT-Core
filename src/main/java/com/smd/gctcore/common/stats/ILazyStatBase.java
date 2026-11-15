package com.smd.gctcore.common.stats;

import net.minecraft.util.text.ITextComponent;

public interface ILazyStatBase {

    void gctcore$markLazy(byte kind);

    void gctcore$ensureStatName();

    ITextComponent gctcore$createLazyStatName(byte kind);
}
