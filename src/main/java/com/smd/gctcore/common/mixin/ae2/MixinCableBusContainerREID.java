package com.smd.gctcore.common.mixin.ae2;

import appeng.api.parts.IPart;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEPartLocation;
import appeng.parts.CableBusContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

/**
 * Makes AE2's raw CableBus part stream use the same extended item-ID encoding as REID.
 *
 * <p>REID changes ItemStack packets from a short item ID to a VarInt, but AE2 serializes
 * CableBus parts directly to a ByteBuf. Part items above the vanilla ID limit otherwise
 * wrap or truncate on the client, leaving the server-side part and collision behind.</p>
 */
@Mixin(CableBusContainer.class)
public abstract class MixinCableBusContainerREID {

    @Inject(
            method = "writeToStream(Lio/netty/buffer/ByteBuf;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void gctcore$writeExtendedPartIds(ByteBuf data, CallbackInfo ci) throws IOException {
        CableBusContainer container = (CableBusContainer) (Object) this;
        int sides = 0;

        for (int ordinal = 0; ordinal < AEPartLocation.values().length; ordinal++) {
            if (container.getPart(AEPartLocation.fromOrdinal(ordinal)) != null) {
                sides |= 1 << ordinal;
            }
        }

        data.writeByte(sides);

        for (int ordinal = 0; ordinal < AEPartLocation.values().length; ordinal++) {
            IPart part = container.getPart(AEPartLocation.fromOrdinal(ordinal));
            if (part == null) {
                continue;
            }

            ItemStack stack = part.getItemStack(PartItemStack.NETWORK);
            gctcore$writeVarInt(data, Item.getIdFromItem(stack.getItem()));
            data.writeShort(stack.getItemDamage());
            part.writeToStream(data);
        }

        container.getFacadeContainer().writeToStream(data);
        ci.cancel();
    }

    @Inject(
            method = "readFromStream(Lio/netty/buffer/ByteBuf;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void gctcore$readExtendedPartIds(
            ByteBuf data,
            CallbackInfoReturnable<Boolean> cir
    ) throws IOException {
        CableBusContainer container = (CableBusContainer) (Object) this;
        int sides = data.readUnsignedByte();
        boolean updateBlock = false;

        for (int ordinal = 0; ordinal < AEPartLocation.values().length; ordinal++) {
            AEPartLocation side = AEPartLocation.fromOrdinal(ordinal);
            if ((sides & 1 << ordinal) != 0) {
                IPart part = container.getPart(side);
                int itemId = gctcore$readVarInt(data);
                int damage = data.readShort();
                Item item = Item.getItemById(itemId);

                if (item == null) {
                    throw new IOException("Unknown CableBus part item ID " + itemId);
                }

                ItemStack current = part == null
                        ? ItemStack.EMPTY
                        : part.getItemStack(PartItemStack.NETWORK);

                if (!current.isEmpty()
                        && current.getItem() == item
                        && current.getItemDamage() == damage) {
                    if (part.readFromStream(data)) {
                        updateBlock = true;
                    }
                    continue;
                }

                container.removePart(side, false);
                AEPartLocation addedSide = container.addPart(
                        new ItemStack(item, 1, damage), side, null, null);

                if (addedSide == null) {
                    throw new IOException("Unable to reconstruct CableBus part item ID " + itemId);
                }

                part = container.getPart(addedSide);
                if (part == null) {
                    throw new IOException("CableBus part was not present after adding item ID " + itemId);
                }
                part.readFromStream(data);
            } else if (container.getPart(side) != null) {
                container.removePart(side, false);
            }
        }

        if (container.getFacadeContainer().readFromStream(data)) {
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(updateBlock);
        }
    }

    private static void gctcore$writeVarInt(ByteBuf data, int value) {
        while ((value & 0xFFFFFF80) != 0) {
            data.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
        data.writeByte(value);
    }

    private static int gctcore$readVarInt(ByteBuf data) throws IOException {
        int value = 0;
        int bytes = 0;
        int current;

        do {
            current = data.readUnsignedByte();
            value |= (current & 0x7F) << bytes++ * 7;
            if (bytes > 5) {
                throw new IOException("CableBus part item ID VarInt is too large");
            }
        } while ((current & 0x80) != 0);

        return value;
    }
}
