package com.gmm.gctold;

import com.gmm.gctold.client.gui.GuiGUISanityAltar;
import com.gmm.gctold.client.gui.GuiEarthbound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public final class GctAllGuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GuiGUISanityAltar.GUIID) {
            return new GuiGUISanityAltar.SanityAltarContainer(world, x, y, z, player);
        }
        if (id == GuiEarthbound.GUIID) {
            return new GuiEarthbound.EarthboundContainer(world, x, y, z, player);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GuiGUISanityAltar.GUIID) {
            return new GuiGUISanityAltar.SanityAltarScreen(world, x, y, z, player);
        }
        if (id == GuiEarthbound.GUIID) {
            return new GuiEarthbound.EarthboundScreen(world, x, y, z, player);
        }
        return null;
    }
}
