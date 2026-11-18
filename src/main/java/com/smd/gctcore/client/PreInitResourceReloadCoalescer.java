package com.smd.gctcore.client;

import com.smd.gctcore.gctcore;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 合并 PreInit 阶段触发的多次 Minecraft / FML 资源包重载请求，
 * 在 {@code Loader.preinitializeMods()} 结束前统一执行一次，减少重复的语言、音效与 Patchouli 全书重刷。
 */
public final class PreInitResourceReloadCoalescer {

    private static boolean active;
    private static boolean pendingFullReload;
    private static int deferredCount;
    private static final List<Predicate<IResourceType>> PENDING_SELECTIVE = new ArrayList<>();

    private PreInitResourceReloadCoalescer() {
    }

    public static void beginPreInit() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || !mc.isCallingFromMinecraftThread()) {
            return;
        }
        active = true;
        pendingFullReload = false;
        deferredCount = 0;
        PENDING_SELECTIVE.clear();
    }

    public static boolean deferFullReload() {
        if (!active) {
            return false;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || !mc.isCallingFromMinecraftThread()) {
            return false;
        }
        pendingFullReload = true;
        deferredCount++;
        return true;
    }

    public static boolean deferSelectiveReload(Predicate<IResourceType> filter) {
        if (!active || filter == null) {
            return false;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || !mc.isCallingFromMinecraftThread()) {
            return false;
        }
        if (!pendingFullReload) {
            PENDING_SELECTIVE.add(filter);
        }
        deferredCount++;
        return true;
    }

    public static void endPreInitAndFlush() {
        if (!active) {
            return;
        }
        boolean full = pendingFullReload;
        int count = deferredCount;
        List<Predicate<IResourceType>> selective = PENDING_SELECTIVE.isEmpty()
                ? null
                : new ArrayList<>(PENDING_SELECTIVE);
        active = false;
        pendingFullReload = false;
        deferredCount = 0;
        PENDING_SELECTIVE.clear();

        if (full) {
            gctcore.LOGGER.info("[GCTCore-PreInitReload] Coalesced {} PreInit resource reload call(s) into 1 full reload.", count);
            Minecraft.getMinecraft().refreshResources();
        } else if (selective != null) {
            gctcore.LOGGER.info("[GCTCore-PreInitReload] Coalesced {} PreInit selective resource reload call(s) into 1.", count);
            Predicate<IResourceType> combined = type -> {
                for (int i = 0, size = selective.size(); i < size; i++) {
                    if (selective.get(i).test(type)) {
                        return true;
                    }
                }
                return false;
            };
            FMLClientHandler.instance().refreshResources(combined);
        }
    }
}
