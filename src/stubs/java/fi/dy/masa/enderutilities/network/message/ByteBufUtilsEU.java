package fi.dy.masa.enderutilities.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;

// Compile-time stub only - excluded from packaged jar by build.gradle
public class ByteBufUtilsEU {

    public static void writeItemStackToBuffer(ByteBuf buf, ItemStack stack) {}

    public static ItemStack readItemStackFromBuffer(ByteBuf buf) throws IOException {
        return null;
    }

    public static void writeNBTTagCompoundToBuffer(ByteBuf buf, NBTTagCompound nbt) {}

    public static NBTTagCompound readNBTTagCompoundFromBuffer(ByteBuf buf) throws IOException {
        return null;
    }
}

