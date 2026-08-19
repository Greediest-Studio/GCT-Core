package com.smd.gctcore.proxy;

import com.smd.gctcore.common.events.EventHooks;
import com.smd.gctcore.common.events.MoreTconBedrockHandler;
import com.smd.gctcore.common.events.NilfheimErosionHandler;
import com.smd.gctcore.common.integration.WorldDimensionIntegrations;
import com.smd.gctcore.common.integration.extendedcrafting.ExtendedCraftingAutomation;
import com.smd.gctcore.common.integration.extendedcrafting.ExtendedCraftingGuiHandler;
import com.smd.gctcore.common.tile.blood_altar.BloodAltarFactoryController;
import com.smd.gctcore.common.tile.blood_altar.BloodAltarRecipes;
import com.smd.gctcore.common.integration.astralsorcery.RadiantQuartzLiquefaction;
import com.smd.gctcore.common.integration.botania.DaisyPlacer;
import com.smd.gctcore.common.integration.top.GctTopPlugin;
import com.smd.gctcore.common.network.GctNetworkHandler;
import com.smd.gctcore.common.util.MaterialRenderingDebugHelper;
import com.smd.gctcore.misc.*;
import com.smd.gctcore.common.integration.mmce.MMCE_BuilderTaskManager;
import com.smd.gctcore.common.integration.mmce.BonsaiTreesRecipeAdapterRegistry;
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
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import hellfirepvp.modularmachinery.common.container.ContainerFactoryController;
import com.smd.gctcore.gctcore;

public class CommonProxy implements IGuiHandler {
    public static final int BLOOD_ALTAR_GUI_ID = 210;

    private final ExtendedCraftingGuiHandler extendedCraftingGuiHandler = new ExtendedCraftingGuiHandler();

    public void preInit(FMLPreInitializationEvent event) {
        GctNetworkHandler.init();
        PotionsItemRegistry.init();
        BlockRegistry.init();
        ExtendedCraftingAutomation.init();
        NilfheimBiomes.init();
        // Mekanism upgrade types must exist before upgrade items are constructed
        if (Mods.MEKANISM.isLoading()) {
            com.smd.gctcore.common.integration.mekanism.GctMekanismUpgrades.init();
        }
        ItemRegistry.init();
        EntityRegistrar.init();
        BlockRegistry.registerTileEntities();
        ExtendedCraftingAutomation.registerTileEntities();
        NetworkRegistry.INSTANCE.registerGuiHandler(gctcore.INSTANCE, this);
        // 注册事件监听器
        MinecraftForge.EVENT_BUS.register(EventHooks.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new NilfheimErosionHandler());
        MinecraftForge.EVENT_BUS.register(new BlockRegistry());
        MinecraftForge.EVENT_BUS.register(new ItemRegistry());
        if (ExtendedCraftingAutomation.enabled()) {
            MinecraftForge.EVENT_BUS.register(new ExtendedCraftingAutomation());
        }
        MinecraftForge.EVENT_BUS.register(new PotionsItemRegistry());
        MinecraftForge.EVENT_BUS.register(new SoundRegistry());
        MinecraftForge.EVENT_BUS.register(new EntityRegistrar());
        MinecraftForge.EVENT_BUS.register(new MMCE_BuilderTaskManager());
        MinecraftForge.EVENT_BUS.register(new BonsaiTreesRecipeAdapterRegistry());
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
        BloodAltarRecipes.register();
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

    @Override
    public Object getServerGuiElement(final int id, final EntityPlayer player, final net.minecraft.world.World world,
                                      final int x, final int y, final int z) {
        if (id == BLOOD_ALTAR_GUI_ID) {
            final TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            return tile instanceof BloodAltarFactoryController
                    ? new ContainerFactoryController((BloodAltarFactoryController) tile, player) : null;
        }
        return extendedCraftingGuiHandler.getServerGuiElement(id, player, world, x, y, z);
    }

    @Override
    public Object getClientGuiElement(final int id, final EntityPlayer player, final net.minecraft.world.World world,
                                      final int x, final int y, final int z) {
        return extendedCraftingGuiHandler.getClientGuiElement(id, player, world, x, y, z);
    }
}
