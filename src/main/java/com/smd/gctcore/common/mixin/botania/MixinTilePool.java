package com.smd.gctcore.common.mixin.botania;

import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.PoolVariant;
import vazkii.botania.common.block.tile.mana.TilePool;

/**
 * 将神话魔力池（FABULOUS 变体）的魔力容量从 1,000,000 修改为 5,000,000。
 * 注入点在 update() 方法的 HEAD，在原始初始化（manaCap == -1 时）之前执行。
 * 若检测到当前为 FABULOUS 变体，则提前设定 manaCap = 5,000,000，
 * 使原始的 if (manaCap == -1) 块跳过，从而避免被覆盖为 1,000,000。
 */
@Mixin(value = TilePool.class, remap = false)
public abstract class MixinTilePool extends TileEntity {

    @Shadow
    public int manaCap;

    @Inject(method = "update()V", at = @At("HEAD"), remap = false)
    private void gctcore$initFabulousManaCap(CallbackInfo ci) {
        if (this.manaCap == -1) {
            PoolVariant variant = this.world.getBlockState(this.pos)
                    .getValue(BotaniaStateProps.POOL_VARIANT);
            if (variant == PoolVariant.FABULOUS) {
                this.manaCap = 5000000;
            }
        }
    }
}
