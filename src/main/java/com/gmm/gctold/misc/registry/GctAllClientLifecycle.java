package com.gmm.gctold.misc.registry;

import com.gmm.gctold.common.entity.GctAllEntities;
import com.gmm.gctold.client.gui.overlay.OverlayBlueScreenOverlay;
import com.gmm.gctold.client.gui.overlay.OverlaySanityRender;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public final class GctAllClientLifecycle {
    private GctAllClientLifecycle() {
    }

    public static void preInit(FMLPreInitializationEvent event, String modId) {
        OBJLoader.INSTANCE.addDomain(modId);
        GctAllEntities.registerRenderers(event);
    }

    public static void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new OverlaySanityRender.SanityOverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new OverlayBlueScreenOverlay.BlueScreenOverlayRenderer());
    }
}
