package com.smd.gctcore.common.network;

import com.smd.gctcore.common.integration.extendedcrafting.ContainerExtendedPatternTerminal;
import com.smd.gctcore.common.integration.extendedcrafting.TileExtendedPatternTerminal;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class PacketExtendedPatternTerminal implements IMessage {
    public static final byte ENCODE = 0;
    public static final byte CLEAR = 1;
    public static final byte TRANSFER = 2;
    private byte action;
    private final Map<Integer, ItemStack> ghosts = new HashMap<>();

    public PacketExtendedPatternTerminal() { }
    public PacketExtendedPatternTerminal(byte action) { this.action = action; }
    public PacketExtendedPatternTerminal(Map<Integer, ItemStack> ghosts) {
        this.action = TRANSFER;
        this.ghosts.putAll(ghosts);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = buf.readByte();
        int count = buf.readUnsignedShort();
        for (int i = 0; i < count; i++) ghosts.put(buf.readUnsignedShort(), ByteBufUtils.readItemStack(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(action);
        buf.writeShort(ghosts.size());
        for (Map.Entry<Integer, ItemStack> entry : ghosts.entrySet()) {
            buf.writeShort(entry.getKey());
            ByteBufUtils.writeItemStack(buf, entry.getValue());
        }
    }

    public static final class Handler implements IMessageHandler<PacketExtendedPatternTerminal, IMessage> {
        @Override
        public IMessage onMessage(PacketExtendedPatternTerminal message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> handle(player, message));
            return null;
        }

        private static void handle(EntityPlayerMP player, PacketExtendedPatternTerminal message) {
            if (!(player.openContainer instanceof ContainerExtendedPatternTerminal)) return;
            ContainerExtendedPatternTerminal container = (ContainerExtendedPatternTerminal) player.openContainer;
            TileExtendedPatternTerminal terminal = container.terminal();
            if (message.action == ENCODE) terminal.encode();
            else if (message.action == CLEAR) terminal.clearGrid();
            else if (message.action == TRANSFER) {
                terminal.clearGrid();
                for (Map.Entry<Integer, ItemStack> entry : message.ghosts.entrySet()) {
                    if (entry.getKey() >= 0 && entry.getKey() < terminal.gridSlots())
                        terminal.setInventorySlotContents(entry.getKey(), entry.getValue());
                }
            }
            container.detectAndSendChanges();
        }
    }
}
