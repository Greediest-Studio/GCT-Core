package com.gmm.gctold.client.gui;

import com.gmm.gctold.common.network.GctAllNetwork;
import net.minecraftforge.fml.relauncher.Side;

public final class GctAllGuiNetwork {
    private GctAllGuiNetwork() {
    }

    public static void registerMessages() {
        GctAllNetwork.register(GuiEarthbound.GUIButtonPressedMessageHandler.class,
                GuiEarthbound.GUIButtonPressedMessage.class, Side.SERVER);
        GctAllNetwork.register(GuiEarthbound.GUISlotChangedMessageHandler.class,
                GuiEarthbound.GUISlotChangedMessage.class, Side.SERVER);

        GctAllNetwork.register(GuiGUISanityAltar.GUIButtonPressedMessageHandler.class,
                GuiGUISanityAltar.GUIButtonPressedMessage.class, Side.SERVER);
        GctAllNetwork.register(GuiGUISanityAltar.GUISlotChangedMessageHandler.class,
                GuiGUISanityAltar.GUISlotChangedMessage.class, Side.SERVER);
    }
}
