package com.smd.gctcore.common.mixin.vanilla.renderer;

import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayDeque;
import java.util.ArrayList;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Shadow
    private int renderDistanceChunks;

    /**
     * 拦截 Queues.newArrayDeque() 调用，返回指定初始容量的 ArrayDeque。
     * 容量 = (视距半径 * 2 + 1)^2 + 64，足以容纳所有可见区块而无需扩容。
     */
    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/Queues;newArrayDeque()Ljava/util/ArrayDeque;",
                    remap = false
            )
    )
    private <E> ArrayDeque<E> redirectNewArrayDeque() {
        int diameter = this.renderDistanceChunks * 2 + 1;
        int capacity = diameter * diameter + 64;
        return new ArrayDeque<>(capacity);
    }

    /**
     * 拦截 Lists.newArrayList() 调用，返回指定初始容量的 ArrayList。
     * 使用与队列相同的容量估算。
     */
    @Redirect(
            method = "setupTerrain",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;",
                    remap = false
            )
    )
    private <E> ArrayList<E> redirectNewArrayList() {
        int diameter = this.renderDistanceChunks * 2 + 1;
        int capacity = diameter * diameter + 64;
        return new ArrayList<>(capacity);
    }
}
