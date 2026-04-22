package com.smd.gctcore.common.mixin.moretcon;

import com.existingeevee.moretcon.compat.jei.JeiInformationContainer;
import mezz.jei.api.IModRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(JeiInformationContainer.class)
public class MixinJeiInformationContainer {

    /**
     * @author Gct-Core
     * @reason 删除描述
     */
    @Overwrite(remap = false)
    public void onRun(IModRegistry r) {
    }
}
