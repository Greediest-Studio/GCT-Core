package com.smd.gctcore;

import com.smd.gctcore.events.EventHooks;
import com.smd.gctcore.events.MiningSpeedHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.smd.gctcore.proxy.ClientProxy;
import com.smd.gctcore.world.AirportDim.DimensionTypeAirport;
import com.smd.gctcore.world.NothingnessDim.DimensionTypeNothingness;
import com.smd.gctcore.world.OrderCore.DimensionTypeOrderCore;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = "gctcore", name = "Gct Core", version = "1.0.16")
public class gctcore {

    public static final String MODID = "gctcore";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new EventHooks());
        MinecraftForge.EVENT_BUS.register(new MiningSpeedHandler());
        // MoreTcon 基岩挖掘限制，仅在 moretcon 存在时注册，避免开发环境 NoClassDefFoundError
        if (Loader.isModLoaded("moretcon")) {
            MinecraftForge.EVENT_BUS.register(new com.smd.gctcore.events.MoreTconBedrockHandler());
        }

        // 注册维度
        DimensionManager.registerDimension(114514, DimensionTypeAirport.Airport);
        DimensionManager.registerDimension(-114514, DimensionTypeNothingness.nothingness);
        DimensionManager.registerDimension(103, DimensionTypeOrderCore.ordercore);
        
        // 客户端专用初始化
        if (event.getSide() == Side.CLIENT) {
            ClientProxy clientProxy = new ClientProxy();
            clientProxy.preInit();
            ClientRegistry.registerKeyBinding(MiningSpeedHandler.KEY_MINING_SPEED_ADD);
            ClientRegistry.registerKeyBinding(MiningSpeedHandler.KEY_MINING_SPEED_MINUS);
        }

        // Register structure generators
        GameRegistry.registerWorldGenerator(new com.smd.gctcore.world.CrimsonTempleGenerator(), 0);
        GameRegistry.registerWorldGenerator(new com.smd.gctcore.world.ShadowberryCaveGenerator(), 0);
    }
}
