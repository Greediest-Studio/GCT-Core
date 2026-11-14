package com.smd.gctcore.common.mixin.hei;

import com.smd.gctcore.common.integration.jei.HeiHashCacheHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackHelper;
import mezz.jei.startup.StackHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers HEI's item hashCache for post-load cleanup and skips inserting new
 * ItemStack keys once JEI startup/reload finishes.
 */
@Mixin(value = ItemStackHelper.class, remap = false)
public abstract class MixinItemStackHelper {

    @Shadow
    @Final
    private Object2IntMap<ItemStack> hashCache;

    @Inject(method = "<init>(Lmezz/jei/startup/StackHelper;)V", at = @At("RETURN"), remap = false)
    private void gctcore$registerHashCache(StackHelper stackHelper, CallbackInfo ci) {
        HeiHashCacheHelper.registerItemHashCache(this.hashCache);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(
            method = "getHash(Lnet/minecraft/item/ItemStack;)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/objects/Object2IntMap;put(Ljava/lang/Object;I)I"
            ),
            remap = false
    )
    private int gctcore$skipPostLoadCachePut(Object2IntMap map, Object key, int value) {
        if (HeiHashCacheHelper.isCachingActive()) {
            return map.put(key, value);
        }
        return 0;
    }
}
