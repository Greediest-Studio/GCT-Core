package com.smd.gctcore.common.mixin.raids;

import com.smd.gctcore.common.events.RaidsWorldReferenceCleanup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Detaches unloaded {@link WorldServer} instances from Raids-Backport's {@code WorldDataRaids}
 * stored in Overworld's shared {@code MapStorage}, preventing unloaded dimensions from leaking.
 */
@Pseudo
@Mixin(targets = "net.smileycorp.raids.common.raid.WorldDataRaids", remap = false)
public abstract class MixinWorldDataRaids extends WorldSavedData implements RaidsWorldReferenceCleanup.WorldReference {

    @Shadow(remap = false)
    @Final
    private Map<Integer, Object> raidMap;

    @Shadow(remap = false)
    private WorldServer world;

    @Shadow(remap = false)
    private NBTTagList raidNBT;

    protected MixinWorldDataRaids(String name) {
        super(name);
    }

    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"), remap = false, require = 1)
    private void gctcore$trackWorldDataRaids(String name, CallbackInfo ci) {
        RaidsWorldReferenceCleanup.register(this);
    }

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void gctcore$skipTickWhenDetached(CallbackInfo ci) {
        if (this.world == null) {
            ci.cancel();
        }
    }

    @Inject(method = {"writeToNBT", "func_189551_b"}, at = @At("RETURN"), remap = false, require = 1)
    private void gctcore$preserveDetachedRaidNbt(NBTTagCompound nbt, CallbackInfoReturnable<NBTTagCompound> cir) {
        if (this.world == null && this.raidMap.isEmpty() && this.raidNBT != null) {
            // 维度卸载后若主世界 MapStorage 触发写盘，保留暂存的 raidNBT，避免覆盖未重连维度的袭击存档。
            NBTTagCompound result = cir.getReturnValue();
            if (result != null) {
                result.setTag("Raids", this.raidNBT.copy());
            }
        }
    }

    @Override
    @Unique
    public void gctcore$releaseWorldReferences(WorldServer unloadedWorld) {
        if (this.world != unloadedWorld) {
            return;
        }
        if (!this.raidMap.isEmpty()) {
            // 释放引用前将活跃袭击脱水序列化到 raidNBT；维度下次加载时由 WorldDataRaids#setWorld 自动重建。
            NBTTagCompound snapshot = this.writeToNBT(new NBTTagCompound());
            this.raidNBT = snapshot.getTagList("Raids", Constants.NBT.TAG_COMPOUND);
            this.raidMap.clear();
            this.markDirty();
        }
        this.world = null;
    }
}
