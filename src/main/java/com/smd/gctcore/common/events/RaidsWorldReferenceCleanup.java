package com.smd.gctcore.common.events;

import com.smd.gctcore.Tags;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Releases {@link WorldServer} references held by Raids-Backport's {@code WorldDataRaids}
 * entries in Overworld's shared {@code MapStorage} when a dimension unloads.
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class RaidsWorldReferenceCleanup {

    private static final Set<WorldReference> REFERENCES =
            Collections.newSetFromMap(new WeakHashMap<>());

    private RaidsWorldReferenceCleanup() {
    }

    public static void register(WorldReference reference) {
        if (reference != null) {
            synchronized (REFERENCES) {
                REFERENCES.add(reference);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        World world = event.getWorld();
        if (!(world instanceof WorldServer)) {
            return;
        }
        List<WorldReference> snapshot;
        synchronized (REFERENCES) {
            if (REFERENCES.isEmpty()) {
                return;
            }
            snapshot = new ArrayList<>(REFERENCES);
        }
        WorldServer unloadedWorld = (WorldServer) world;
        for (WorldReference reference : snapshot) {
            if (reference != null) {
                reference.gctcore$releaseWorldReferences(unloadedWorld);
            }
        }
    }

    public interface WorldReference {
        void gctcore$releaseWorldReferences(WorldServer unloadedWorld);
    }
}
