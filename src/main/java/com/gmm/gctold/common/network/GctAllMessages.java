package com.gmm.gctold.common.network;

import com.gmm.gctold.common.data.GctAllVariables;
import net.minecraftforge.fml.relauncher.Side;

public final class GctAllMessages {
    private GctAllMessages() {
    }

    public static void register() {
        GctAllNetwork.register(GctAllVariables.WorldSavedDataSyncMessageHandler.class,
                GctAllVariables.WorldSavedDataSyncMessage.class, Side.SERVER, Side.CLIENT);
    }
}
