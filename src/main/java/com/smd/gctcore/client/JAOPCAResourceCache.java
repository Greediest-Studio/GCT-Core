package com.smd.gctcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/** 同一轮资源加载期间，资源存在性不变；缺失资源也只查询一次。 */
public final class JAOPCAResourceCache {

    private static final Map<ResourceLocation, Boolean> CACHE = new HashMap<>();

    private JAOPCAResourceCache() {
    }

    public static boolean hasResource(ResourceLocation location) {
        return CACHE.computeIfAbsent(location, JAOPCAResourceCache::resourceExists);
    }

    private static boolean resourceExists(ResourceLocation location) {
        try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(location)) {
            return resource != null;
        } catch (Exception ignored) {
            // 保持 JAOPCA 的行为：资源读取失败时使用默认纹理。
            return false;
        }
    }

    public static void clear() {
        CACHE.clear();
    }
}
