package com.smd.gctcore.common.network;

import com.smd.gctcore.common.clientstate.NilfheimErosionClientState;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketNilfheimErosion implements IMessage, IMessageHandler<PacketNilfheimErosion, IMessage> {
    private float progress;
    private boolean active;

    public PacketNilfheimErosion() {
    }

    public PacketNilfheimErosion(float progress, boolean active) {
        this.progress = progress;
        this.active = active;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        progress = buf.readFloat();
        active = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(progress);
        buf.writeBoolean(active);
    }

    @Override
    public IMessage onMessage(PacketNilfheimErosion message, MessageContext ctx) {
        handleClient(message);
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static void handleClient(PacketNilfheimErosion message) {
        Minecraft.getMinecraft().addScheduledTask(() -> NilfheimErosionClientState.set(message.progress, message.active));
    }
}
