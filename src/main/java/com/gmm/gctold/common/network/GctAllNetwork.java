package com.gmm.gctold.common.network;

import com.smd.gctcore.common.network.GctNetworkHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class GctAllNetwork {
    public static final SimpleNetworkWrapper CHANNEL = GctNetworkHandler.CHANNEL;

    private GctAllNetwork() {
    }

    public static <T extends IMessage, V extends IMessage> void register(
            Class<? extends IMessageHandler<T, V>> handler,
            Class<T> message,
            Side... sides
    ) {
        for (Side side : sides) {
            GctNetworkHandler.registerMessage(handler, message, side);
        }
    }
}
