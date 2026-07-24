package com.smd.gctcore.common.network;

import com.smd.gctcore.common.integration.mmce.MMCE_BuilderConfig;
import com.smd.gctcore.common.items.MMCE_BuilderTool;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketMMCEBuilderConfig implements IMessage, IMessageHandler<PacketMMCEBuilderConfig, IMessage> {

    private int slot;
    private boolean useAeItems;
    private boolean useAeFluids;
    private boolean craftMissing;
    private boolean disassembleMode;
    private int dynamicLength;
    private String attachmentModule;

    public PacketMMCEBuilderConfig() {
    }

    public PacketMMCEBuilderConfig(int slot, boolean useAeItems, boolean useAeFluids,
                                   boolean craftMissing, boolean disassembleMode, int dynamicLength) {
        this(slot, useAeItems, useAeFluids, craftMissing, disassembleMode, dynamicLength, "");
    }

    public PacketMMCEBuilderConfig(int slot, boolean useAeItems, boolean useAeFluids, boolean craftMissing,
                                   boolean disassembleMode, int dynamicLength, String attachmentModule) {
        this.slot = slot;
        this.useAeItems = useAeItems;
        this.useAeFluids = useAeFluids;
        this.craftMissing = craftMissing;
        this.disassembleMode = disassembleMode;
        this.dynamicLength = dynamicLength;
        this.attachmentModule = attachmentModule;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slot = buf.readInt();
        useAeItems = buf.readBoolean();
        useAeFluids = buf.readBoolean();
        craftMissing = buf.readBoolean();
        disassembleMode = buf.readBoolean();
        dynamicLength = buf.readInt();
        attachmentModule = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(slot);
        buf.writeBoolean(useAeItems);
        buf.writeBoolean(useAeFluids);
        buf.writeBoolean(craftMissing);
        buf.writeBoolean(disassembleMode);
        buf.writeInt(dynamicLength);
        ByteBufUtils.writeUTF8String(buf, attachmentModule == null ? "" : attachmentModule);
    }

    @Override
    public IMessage onMessage(PacketMMCEBuilderConfig message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> apply(player, message));
        return null;
    }

    private static void apply(EntityPlayerMP player, PacketMMCEBuilderConfig message) {
        ItemStack stack = findToolStack(player, message.slot);
        if (stack.isEmpty() || !(stack.getItem() instanceof MMCE_BuilderTool)) {
            return;
        }
        MMCE_BuilderConfig.setUseAeItems(stack, message.useAeItems);
        MMCE_BuilderConfig.setUseAeFluids(stack, message.useAeFluids);
        MMCE_BuilderConfig.setCraftMissing(stack, message.craftMissing);
        MMCE_BuilderConfig.setDisassembleMode(stack, message.disassembleMode);
        MMCE_BuilderConfig.setDynamicLength(stack, message.dynamicLength);
        MMCE_BuilderConfig.setAttachmentModule(stack, message.attachmentModule);
    }

    private static ItemStack findToolStack(EntityPlayerMP player, int slot) {
        if (slot >= 0 && slot < player.inventory.mainInventory.size()) {
            ItemStack stack = player.inventory.mainInventory.get(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof MMCE_BuilderTool) {
                return stack;
            }
        }
        for (EnumHand hand : EnumHand.values()) {
            ItemStack stack = player.getHeldItem(hand);
            if (!stack.isEmpty() && stack.getItem() instanceof MMCE_BuilderTool) {
                return stack;
            }
        }
        for (ItemStack stack : player.inventory.mainInventory) {
            if (!stack.isEmpty() && stack.getItem() instanceof MMCE_BuilderTool) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
