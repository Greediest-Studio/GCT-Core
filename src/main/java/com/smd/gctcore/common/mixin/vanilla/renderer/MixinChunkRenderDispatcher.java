package com.smd.gctcore.common.mixin.vanilla.renderer;

import com.smd.gctcore.common.config.GCTMixinConfig;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 限制 {@link ChunkRenderDispatcher} 预分配的渲染构造器数量上限。
 * <p>
 * 注入点位于 {@code Queues.newArrayBlockingQueue} 调用之前，确保在 LoliASM 改写
 * {@code countRenderBuilders} 之后、实际分配队列与 {@code RegionRenderCacheBuilder} 之前生效。
 */
@Mixin(ChunkRenderDispatcher.class)
public abstract class MixinChunkRenderDispatcher {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    @Mutable
    private int countRenderBuilders;

    @Inject(
            method = "<init>(I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/Queues;newArrayBlockingQueue(I)Ljava/util/concurrent/ArrayBlockingQueue;",
                    remap = false
            )
    )
    private void gctcore$capRenderBuilders(int countRenderBuildersIn, CallbackInfo ci) {
        int cap = GCTMixinConfig.maxChunkRenderBuilders;
        if (cap > 0 && this.countRenderBuilders > cap) {
            int previousCount = this.countRenderBuilders;
            this.countRenderBuilders = cap;
            LOGGER.info("GCT-Core capped ChunkRenderDispatcher builders: {} -> {}", previousCount, this.countRenderBuilders);
        }
    }
}
