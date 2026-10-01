package com.gmm.gctold.client.gui.overlay;

import com.gmm.gctold.common.events.SanityRenderDisplayOverlayIngame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class OverlaySanityRender {
  private OverlaySanityRender() {
  }

  public static class SanityOverlayRenderer {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    @SideOnly(Side.CLIENT)
    public void eventHandler(RenderGameOverlayEvent event) {
      if (!event.isCancelable() && event.getType() == RenderGameOverlayEvent.ElementType.HELMET) {
        int posX = event.getResolution().getScaledWidth() / 2;
        int posY = event.getResolution().getScaledHeight() / 2;
        EntityPlayerSP entityPlayerSP = (Minecraft.getMinecraft()).player;
        World world = ((EntityPlayer)entityPlayerSP).world;
        int x = (int)((EntityPlayer)entityPlayerSP).posX;
        int y = (int)((EntityPlayer)entityPlayerSP).posY;
        int z = (int)((EntityPlayer)entityPlayerSP).posZ;
        if (SanityRenderDisplayOverlayIngame.run(entityPlayerSP)) {
          (Minecraft.getMinecraft()).fontRenderer.drawString("San", posX + -9, posY + -94, -1);
          (Minecraft.getMinecraft()).fontRenderer.drawString("" + entityPlayerSP.getEntityData().getFloat("sanityAbyss") + "", posX + -7, posY + -85, -1);
        }
      }
    }
  }
}

