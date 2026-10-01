package com.gmm.gctold.proxy;

import com.smd.gctcore.Tags;
import com.gmm.gctold.common.network.ClientTasks;
import com.gmm.gctold.misc.registry.GctAllClientLifecycle;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        ClientTasks.setScheduler(new MinecraftClientScheduler());
        super.preInit(event);
        GctAllClientLifecycle.preInit(event, Tags.MOD_ID);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        GctAllClientLifecycle.init(event);
    }

}
