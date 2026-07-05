package com.smd.gctcore.proxy;

import com.smd.gctcore.common.events.EventHooks;
import com.smd.gctcore.common.events.MoreTconBedrockHandler;
import com.smd.gctcore.common.events.NilfheimErosionHandler;
import com.smd.gctcore.common.integration.WorldDimensionIntegrations;
import com.smd.gctcore.common.integration.astralsorcery.RadiantQuartzLiquefaction;
import com.smd.gctcore.common.integration.botania.DaisyPlacer;
import com.smd.gctcore.common.integration.top.GctTopPlugin;
import com.smd.gctcore.common.network.GctNetworkHandler;
import com.smd.gctcore.common.util.MaterialRenderingDebugHelper;
import com.smd.gctcore.misc.*;
import com.smd.gctcore.common.integration.mmce.MMCE_BuilderTaskManager;
import com.smd.gctcore.common.world.AirportDim.DimensionTypeAirport;
import com.smd.gctcore.common.world.CrimsonTempleGenerator;
import com.smd.gctcore.common.world.NilfheimDim.DimensionTypeNilfheim;
import com.smd.gctcore.common.world.NothingnessDim.DimensionTypeNothingness;
import com.smd.gctcore.common.world.OrderCore.DimensionTypeOrderCore;
import com.smd.gctcore.common.world.ShadowberryCaveGenerator;
import com.smd.gctcore.common.world.biome.nilfheim.NilfheimBiomes;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        GctNetworkHandler.init();
        PotionsItemRegistry.init();
        BlockRegistry.init();
        NilfheimBiomes.init();
        ItemRegistry.init();
        EntityRegistrar.init();
        BlockRegistry.registerTileEntities();
        // 注册事件监听器
        MinecraftForge.EVENT_BUS.register(new EventHooks());
        MinecraftForge.EVENT_BUS.register(new NilfheimErosionHandler());
        MinecraftForge.EVENT_BUS.register(new BlockRegistry());
        MinecraftForge.EVENT_BUS.register(new ItemRegistry());
        MinecraftForge.EVENT_BUS.register(new PotionsItemRegistry());
        MinecraftForge.EVENT_BUS.register(new SoundRegistry());
        MinecraftForge.EVENT_BUS.register(new EntityRegistrar());
        MinecraftForge.EVENT_BUS.register(new MMCE_BuilderTaskManager());
        if(Mods.BOT.isLoading()){
            MinecraftForge.EVENT_BUS.register(new DaisyPlacer());
        }
        // MoreTcon 基岩挖掘限制，仅在 moretcon 存在时注册
        if (Mods.MORETCON.isLoading()) {
            MinecraftForge.EVENT_BUS.register(new MoreTconBedrockHandler());
        }

        DimensionManager.registerDimension(114514, DimensionTypeAirport.Airport);
        DimensionManager.registerDimension(-114514, DimensionTypeNothingness.nothingness);
        DimensionManager.registerDimension(103, DimensionTypeOrderCore.ordercore);
        DimensionManager.registerDimension(NilfheimBiomes.DIMENSION_ID, DimensionTypeNilfheim.NILFHEIM);

        //注册世界生成器
        GameRegistry.registerWorldGenerator(new CrimsonTempleGenerator(), 0);
        GameRegistry.registerWorldGenerator(new ShadowberryCaveGenerator(), 0);
    }

    public void init(FMLInitializationEvent event) {
        NilfheimRecipes.init();
        WorldDimensionIntegrations.init();
        if (Mods.TOP.isLoading()) {
            FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", GctTopPlugin.class.getName());
        }
        if (Mods.AS.isLoading()) {
            RadiantQuartzLiquefaction.init();
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
        MaterialRenderingDebugHelper.logMaterialShaderFixSummary();
    }
}
