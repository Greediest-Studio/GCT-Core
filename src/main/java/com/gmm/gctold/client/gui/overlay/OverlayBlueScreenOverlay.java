package com.gmm.gctold.client.gui.overlay;

import com.gmm.gctold.common.events.BlueScreenTick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class OverlayBlueScreenOverlay {
  private OverlayBlueScreenOverlay() {
  }

  public static class BlueScreenOverlayRenderer {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
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
        if (BlueScreenTick.run(entityPlayerSP)) {
          GlStateManager.disableDepth();
          GlStateManager.depthMask(false);
          GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
          GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
          GlStateManager.disableAlpha();
          (Minecraft.getMinecraft()).renderEngine.bindTexture(new ResourceLocation("gctold:textures/bluescreen_layer.png"));
          GuiIngame.drawModalRectWithCustomSizedTexture(posX + -213, posY + -120, 0.0F, 0.0F, 480, 240, 480.0F, 240.0F);
          GlStateManager.depthMask(true);
          GlStateManager.enableDepth();
          GlStateManager.enableAlpha();
          GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
      }
    }
  }
}

