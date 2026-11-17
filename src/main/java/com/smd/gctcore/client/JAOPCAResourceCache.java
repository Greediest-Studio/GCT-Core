package com.smd.gctcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 同一轮资源加载期间，资源存在性不变；缺失资源也只查询一次。 */
public final class JAOPCAResourceCache {

    private static final Map<ResourceLocation, Boolean> CACHE = new ConcurrentHashMap<>();

    private JAOPCAResourceCache() {
    }

    public static boolean hasResource(ResourceLocation location) {
        if (location == null) {
            return false;
        }
        Boolean cached = CACHE.get(location);
        if (cached != null) {
            return cached;
        }
        boolean exists = resourceExists(location);
        CACHE.putIfAbsent(location, exists);
        return exists;
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
