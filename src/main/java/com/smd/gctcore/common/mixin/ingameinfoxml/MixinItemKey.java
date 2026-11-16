package com.smd.gctcore.common.mixin.ingameinfoxml;

import com.smd.gctcore.client.igi.IGIItemKeySnapshot;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.github.lunatrius.ingameinfo.client.gui.overlay.InfoItem$ItemKey", remap = false)
public abstract class MixinItemKey implements IGIItemKeySnapshot {

    @Shadow(remap = false)
    @Final
    @Mutable
    private NBTTagCompound tagCompound;

    @Redirect(method = "<init>(Lnet/minecraft/item/ItemStack;Z)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/nbt/NBTTagCompound;copy()Lnet/minecraft/nbt/NBTTagCompound;"),
            remap = true, require = 1)
    private NBTTagCompound gctcore$borrowLookupNbt(NBTTagCompound tag) {
        // 临时查找键不会保存到缓存，直接比较当前 NBT 即可。
        return tag;
    }

    @Override
    @Unique
    public void gctcore$freezeNbtSnapshot() {
        if (tagCompound != null) {
            // 快照与原 NBT 的值和哈希相同，原 ItemKey 的 hashCode 仍然有效。
            tagCompound = tagCompound.copy();
        }
    }
}
