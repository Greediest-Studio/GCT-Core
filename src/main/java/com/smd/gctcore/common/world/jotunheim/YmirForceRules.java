package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.common.items.ItemGiantTalisman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSetSlot;

/** Shared height, inventory and traversal rules. Layer indices ascend from lava (0) to cloud (3). */
public final class YmirForceRules {
    public static final int DAMAGE_INTERVAL = 20;
    public static final float DAMAGE_FRACTION = 0.10F;

    private YmirForceRules() { }

    /**
     * Use the lowest permitted island-body blocks, including thickened undersides, never surface
     * heights. Each ward therefore covers its entire island stratum, caves and excavated spaces.
     * Boundaries stay fixed when blocks are mined. Empty gaps belong to the lower stratum.
     */
    public static int layerAtHeight(double y) {
        if (y >= JotunheimTerrainKernel.HeightLayer.CLOUD.minY) return 3;
        if (y >= JotunheimTerrainKernel.HeightLayer.SURFACE.minY) return 2;
        if (y >= JotunheimTerrainKernel.HeightLayer.DEEP.minY) return 1;
        return 0;
    }

    /** Include carried equipment and the cursor, so rearranging a bag cannot remove its protection. */
    public static int talismanLayer(EntityPlayer player) {
        int highest = -1;
        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++)
            highest = Math.max(highest, layer(player.inventory.getStackInSlot(slot)));
        return Math.max(highest, layer(player.inventory.getItemStack()));
    }

    private static int layer(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemGiantTalisman
                ? ((ItemGiantTalisman) stack.getItem()).layerIndex(stack) : -1;
    }

    public static int normalizeTalismans(EntityPlayer player) {
        int highest = talismanLayer(player);
        if (highest >= 0) tuneTalismans(player, highest);
        return highest;
    }

    /** Retune every copy together; downward travel must not be undone by a second, higher copy. */
    public static void tuneTalismans(EntityPlayer player, int layer) {
        if (layer < 0 || layer > 3) return;
        boolean changed = false;
        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++)
            changed |= tune(player.inventory.getStackInSlot(slot), layer);
        boolean cursorChanged = tune(player.inventory.getItemStack(), layer);
        changed |= cursorChanged;
        if (changed) {
            player.inventory.markDirty();
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
                serverPlayer.openContainer.detectAndSendChanges();
                if (cursorChanged) serverPlayer.connection.sendPacket(
                        new SPacketSetSlot(-1, -1, player.inventory.getItemStack()));
            }
        }
    }

    private static boolean tune(ItemStack stack, int target) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemGiantTalisman)) return false;
        ItemGiantTalisman talisman = (ItemGiantTalisman) stack.getItem();
        if (talisman.layerIndex(stack) == target && stack.getMetadata() >= 0 && stack.getMetadata() < 4) return false;
        talisman.setLayer(stack, target);
        return true;
    }

    /**
     * A visit must touch both endpoints of the same well. Merely entering, crossing a height
     * boundary halfway down, or flying around the outside never retunes a talisman.
     * Only primitive data is saved, so a mid-journey save/reload keeps the visited endpoint.
     */
    public static int advanceTraversal(NBTTagCompound state, JotunheimYmirWells.Plan well,
                                       double feetY, boolean carrying) {
        if (well == null || !carrying) {
            state.removeTag("journey");
            return -1;
        }
        NBTTagCompound journey = state.getCompoundTag("journey");
        if (!state.hasKey("journey") || journey.getInteger("x") != well.x
                || journey.getInteger("z") != well.z || journey.getInteger("pair") != well.lowerLayer.ordinal()) {
            journey = new NBTTagCompound();
            journey.setInteger("x", well.x);
            journey.setInteger("z", well.z);
            journey.setInteger("pair", well.lowerLayer.ordinal());
            journey.setInteger("origin", -1);
            state.setTag("journey", journey);
        }
        int endpoint = feetY <= well.lowerFloorY + 3.0D ? well.lowerLayer.ordinal()
                : feetY >= well.upperFloorY + 0.75D ? well.upperLayer.ordinal() : -1;
        if (endpoint < 0) return -1;
        int origin = journey.getInteger("origin");
        journey.setInteger("origin", endpoint);
        return origin >= 0 && endpoint != origin ? endpoint : -1;
    }
}
