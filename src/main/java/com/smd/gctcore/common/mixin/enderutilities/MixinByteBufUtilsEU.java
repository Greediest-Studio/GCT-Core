package com.smd.gctcore.common.mixin.enderutilities;

import fi.dy.masa.enderutilities.network.message.ByteBufUtilsEU;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

/**
 * Fixes Ender Utilities inventory sync packets dropping item icons for item IDs >= 32768.
 *
 * <p>EU's ByteBufUtilsEU originally uses 16-bit short to serialize item IDs over network.
 * When RoughlyEnoughIDs (or JEID) extends item IDs beyond 32767, writing (short) itemId
 * overflows into negative numbers, causing readItemStackFromBuffer to treat the slot as
 * empty and GUI slot rendering to omit the icon.</p>
 */
@Mixin(value = ByteBufUtilsEU.class, remap = false)
public abstract class MixinByteBufUtilsEU {

    @Inject(
            method = "writeItemStackToBuffer(Lio/netty/buffer/ByteBuf;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void gctcore$writeExtendedItemStack(ByteBuf buf, ItemStack stack, CallbackInfo ci) {
        if (stack.isEmpty()) {
            buf.writeInt(-1);
            ci.cancel();
            return;
        }

        buf.writeInt(Item.getIdFromItem(stack.getItem()));
        buf.writeShort(stack.getItemDamage());
        buf.writeInt(stack.getCount());

        NBTTagCompound nbt = null;
        if (stack.getItem().isDamageable() || stack.getItem().getShareTag()) {
            nbt = stack.getItem().getNBTShareTag(stack);
        }

        ByteBufUtilsEU.writeNBTTagCompoundToBuffer(buf, nbt);
        ci.cancel();
    }

    @Inject(
            method = "readItemStackFromBuffer(Lio/netty/buffer/ByteBuf;)Lnet/minecraft/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void gctcore$readExtendedItemStack(
            ByteBuf buf,
            CallbackInfoReturnable<ItemStack> cir
    ) throws IOException {
        int itemId = buf.readInt();
        if (itemId < 0) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        short itemDamage = buf.readShort();
        int count = buf.readInt();
        Item item = Item.getItemById(itemId);
        ItemStack stack = item != null ? new ItemStack(item, count, itemDamage) : ItemStack.EMPTY;
        NBTTagCompound nbt = ByteBufUtilsEU.readNBTTagCompoundFromBuffer(buf);
        if (!stack.isEmpty()) {
            stack.setTagCompound(nbt);
        }
        cir.setReturnValue(stack);
    }
}

