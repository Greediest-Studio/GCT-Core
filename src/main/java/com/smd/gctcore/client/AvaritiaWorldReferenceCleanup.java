package com.smd.gctcore.client;

import com.smd.gctcore.Tags;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class AvaritiaWorldReferenceCleanup {

    // 模型重载后旧渲染器也应能回收，清理注册表自身不持有强引用。
    private static final Set<WorldReference> RENDERERS = Collections.newSetFromMap(new WeakHashMap<>());

    private AvaritiaWorldReferenceCleanup() {
    }

    public static void register(WorldReference renderer) {
        RENDERERS.add(renderer);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            for (WorldReference renderer : RENDERERS) {
                renderer.gctcore$clearWorldReferences(event.getWorld());
            }
        }
    }

    public interface WorldReference {
        void gctcore$clearWorldReferences(World world);
    }
}
