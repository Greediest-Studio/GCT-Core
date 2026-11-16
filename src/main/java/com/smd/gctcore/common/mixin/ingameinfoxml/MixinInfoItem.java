package com.smd.gctcore.common.mixin.ingameinfoxml;

import com.smd.gctcore.client.igi.IGIItemKeySnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Pseudo
@Mixin(targets = "com.github.lunatrius.ingameinfo.client.gui.overlay.InfoItem", remap = false)
public abstract class MixinInfoItem {

    @Redirect(method = {"getCachedItemStack", "getCachedTexture"},
            at = @At(value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
            remap = false, require = 2)
    private static Object gctcore$storeNbtSnapshot(Map<Object, Object> cache, Object key, Object value) {
        // 两个缓存都保留独立快照，后续原物品的 NBT 修改不会改变已保存的键。
        ((IGIItemKeySnapshot) key).gctcore$freezeNbtSnapshot();
        return cache.put(key, value);
    }
}
