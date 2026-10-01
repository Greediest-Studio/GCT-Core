package com.smd.gctcore.common.network;

import com.smd.gctcore.Tags;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class GctNetworkHandler {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);

    private static int packetId = 0;

    public static void init() {
        CHANNEL.registerMessage(PacketNilfheimErosion.class, PacketNilfheimErosion.class, packetId++, Side.CLIENT);
        CHANNEL.registerMessage(PacketExtendedPatternTerminal.Handler.class, PacketExtendedPatternTerminal.class, packetId++, Side.SERVER);
        CHANNEL.registerMessage(PacketTimeLockedSync.class, PacketTimeLockedSync.class, packetId++, Side.CLIENT);
    }

    public static <T extends IMessage, V extends IMessage> void registerMessage(
            Class<? extends IMessageHandler<T, V>> handler,
            Class<T> message,
            Side side
    ) {
        CHANNEL.registerMessage(handler, message, packetId++, side);
    }
}
