package com.gmm.gctold.common.commands;

import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public final class GctAllCommands {
    private GctAllCommands() {
    }

    public static void register(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandGamemode0());
        event.registerServerCommand(new CommandGamemode1());
        event.registerServerCommand(new CommandGamemode2());
        event.registerServerCommand(new CommandGamemode3());
    }
}
