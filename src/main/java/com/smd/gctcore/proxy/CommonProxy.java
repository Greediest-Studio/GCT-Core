package com.smd.gctcore.proxy;

import com.smd.gctcore.common.events.EventHooks;
import com.smd.gctcore.common.events.MoreTconBedrockHandler;
import com.smd.gctcore.common.misc.EntityRegistrar;
import com.smd.gctcore.common.misc.ItemRegistry;
import com.smd.gctcore.common.misc.PotionsItemRegistry;
import com.smd.gctcore.common.world.AirportDim.DimensionTypeAirport;
import com.smd.gctcore.common.world.CrimsonTempleGenerator;
import com.smd.gctcore.common.world.NothingnessDim.DimensionTypeNothingness;
import com.smd.gctcore.common.world.OrderCore.DimensionTypeOrderCore;
import com.smd.gctcore.common.world.ShadowberryCaveGenerator;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        PotionsItemRegistry.init();
        ItemRegistry.init();
        EntityRegistrar.init();
        // 注册事件监听器
        MinecraftForge.EVENT_BUS.register(new EventHooks());
        MinecraftForge.EVENT_BUS.register(new ItemRegistry());
        MinecraftForge.EVENT_BUS.register(new PotionsItemRegistry());
        MinecraftForge.EVENT_BUS.register(new EntityRegistrar());
        // MoreTcon 基岩挖掘限制，仅在 moretcon 存在时注册
        if (Loader.isModLoaded("moretcon")) {
            MinecraftForge.EVENT_BUS.register(new MoreTconBedrockHandler());
        }

        DimensionManager.registerDimension(114514, DimensionTypeAirport.Airport);
        DimensionManager.registerDimension(-114514, DimensionTypeNothingness.nothingness);
        DimensionManager.registerDimension(103, DimensionTypeOrderCore.ordercore);

        //注册世界生成器
        GameRegistry.registerWorldGenerator(new CrimsonTempleGenerator(), 0);
        GameRegistry.registerWorldGenerator(new ShadowberryCaveGenerator(), 0);
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }


}
