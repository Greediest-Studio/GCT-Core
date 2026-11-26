package com.smd.gctcore.common.network;

import com.smd.gctcore.client.YmirForceOverlay;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** The server owns protection; only its visible state is synchronized to the HUD. */
public final class PacketYmirForce implements IMessage, IMessageHandler<PacketYmirForce, IMessage> {
    private boolean active;
    private boolean shielded;
    private int dimension;

    public PacketYmirForce() {}

    public PacketYmirForce(boolean active, boolean shielded, int dimension) {
        this.active = active;
        this.shielded = shielded;
        this.dimension = dimension;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        active = buf.readBoolean();
        shielded = buf.readBoolean();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(active);
        buf.writeBoolean(shielded);
        buf.writeInt(dimension);
    }

    @Override
    public IMessage onMessage(PacketYmirForce message, MessageContext context) {
        if (context.side.isClient()) ClientHandler.handle(message, context);
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static final class ClientHandler {
        private static void handle(PacketYmirForce message, MessageContext context) {
            Minecraft minecraft = Minecraft.getMinecraft();
            NetHandlerPlayClient connection = context.getClientHandler();
            minecraft.addScheduledTask(() -> {
                // A queued packet from a previous server connection must not revive its old shield.
                if (minecraft.getConnection() == connection) {
                    YmirForceOverlay.update(message.active, message.shielded, message.dimension);
                }
            });
        }
    }
}
