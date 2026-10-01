package com.gmm.gctold.misc.registry;

import com.gmm.gctold.GctAllGuiHandler;
import com.smd.gctcore.gctcore;
import com.gmm.gctold.common.commands.GctAllCommands;
import com.gmm.gctold.common.data.GctAllVariableEvents;
import com.gmm.gctold.common.entity.GctAllEntities;
import com.gmm.gctold.common.events.SanityEvents;
import com.gmm.gctold.common.events.StarlandDaylightKick;
import com.gmm.gctold.client.gui.GctAllGuiNetwork;
import com.gmm.gctold.common.items.crafting.GctAllRecipes;
import com.gmm.gctold.common.network.GctAllMessages;
import com.gmm.gctold.common.world.biome.GctAllBiomes;
import com.gmm.gctold.common.world.dimension.GctAllDimensions;
import com.gmm.gctold.common.world.structure.GctAllStructureGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class GctAllLifecycle {
    private GctAllLifecycle() {
    }

    public static void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new GctAllStructureGenerator(), 5);
        NetworkRegistry.INSTANCE.registerGuiHandler(gctcore.INSTANCE, new GctAllGuiHandler());

        GctAllContent.preInit(event);
        GctAllTileEntities.register();
        GctAllDimensions.registerDimensions();
        GctAllMessages.register();
        GctAllGuiNetwork.registerMessages();
        MinecraftForge.EVENT_BUS.register(new GctAllVariableEvents());
        MinecraftForge.EVENT_BUS.register(new SanityEvents());
        MinecraftForge.EVENT_BUS.register(new StarlandDaylightKick());
    }

    public static void init(FMLInitializationEvent event) {
        GctAllBiomes.init();
        GctAllOreDictionary.register();
        GctAllRecipes.registerSmelting();
        GctAllEntities.init(event);
    }

    public static void serverLoad(FMLServerStartingEvent event) {
        GctAllCommands.register(event);
    }
}
