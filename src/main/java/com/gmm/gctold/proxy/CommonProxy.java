package com.gmm.gctold.proxy;

import com.gmm.gctold.misc.registry.GctAllLifecycle;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {
    static {
        FluidRegistry.enableUniversalBucket();
    }

    public void preInit(FMLPreInitializationEvent event) {
        GctAllLifecycle.preInit(event);
    }

    public void init(FMLInitializationEvent event) {
        GctAllLifecycle.init(event);
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    public void serverLoad(FMLServerStartingEvent event) {
        GctAllLifecycle.serverLoad(event);
    }
}
