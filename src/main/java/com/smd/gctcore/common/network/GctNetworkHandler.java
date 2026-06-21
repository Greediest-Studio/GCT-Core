package com.smd.gctcore.common.network;

import com.smd.gctcore.Tags;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class GctNetworkHandler {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);

    private static int packetId = 0;

    public static void init() {
        CHANNEL.registerMessage(PacketMMCEBuilderConfig.class, PacketMMCEBuilderConfig.class, packetId++, Side.SERVER);
        CHANNEL.registerMessage(PacketNilfheimErosion.class, PacketNilfheimErosion.class, packetId++, Side.CLIENT);
    }
}
