package com.smd.gctcore.common.network;

import com.smd.gctcore.common.world.WorldProviderLockedTime;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketTimeLockedSync implements IMessage, IMessageHandler<PacketTimeLockedSync, IMessage> {

    private int dimensionId;
    private int dayOffset;

    public PacketTimeLockedSync() {
    }

    public PacketTimeLockedSync(int dimensionId, int dayOffset) {
        this.dimensionId = dimensionId;
        this.dayOffset = dayOffset;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dimensionId = buf.readInt();
        dayOffset = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimensionId);
        buf.writeInt(dayOffset);
    }

    @Override
    public IMessage onMessage(PacketTimeLockedSync message, MessageContext ctx) {
        handleClient(message);
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static void handleClient(PacketTimeLockedSync message) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            World world = Minecraft.getMinecraft().world;
            if (world == null || world.provider.getDimension() != message.dimensionId) {
                return;
            }
            if (world.provider instanceof WorldProviderLockedTime) {
                ((WorldProviderLockedTime) world.provider).setDayOffset(message.dayOffset);
            }
        });
    }
}
