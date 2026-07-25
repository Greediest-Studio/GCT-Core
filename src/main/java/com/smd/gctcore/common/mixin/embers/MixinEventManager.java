package com.smd.gctcore.common.mixin.embers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import teamroots.embers.Embers;
import teamroots.embers.EventManager;
import teamroots.embers.proxy.ClientProxy;
import teamroots.embers.tileentity.TileEntityAlchemyTablet;
import teamroots.embers.tileentity.ITileEntitySpecialRendererLater;

import java.util.List;

@Mixin(value = EventManager.class, remap = false)
public abstract class MixinEventManager {

    @Shadow
    public static float tickCounter;

    @Shadow
    static EntityPlayer clientPlayer;

    @Unique
    private static ITileEntitySpecialRendererLater embers$alchemyTabletRenderer = null;

    /**
     * @author smd
     * @reason 修复getrender开销过高的问题
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public void onRenderAfterWorld(RenderWorldLastEvent event) {
        tickCounter++;
        if (Embers.proxy instanceof ClientProxy) {
            GlStateManager.pushMatrix();
            ClientProxy.particleRenderer.renderParticles(clientPlayer, event.getPartialTicks());
            GlStateManager.popMatrix();
        }

        List<TileEntity> list = Minecraft.getMinecraft().world.loadedTileEntityList;
        double playerX = Minecraft.getMinecraft().player.lastTickPosX +
                event.getPartialTicks() * (Minecraft.getMinecraft().player.posX - Minecraft.getMinecraft().player.lastTickPosX);
        double playerY = Minecraft.getMinecraft().player.lastTickPosY +
                event.getPartialTicks() * (Minecraft.getMinecraft().player.posY - Minecraft.getMinecraft().player.lastTickPosY);
        double playerZ = Minecraft.getMinecraft().player.lastTickPosZ +
                event.getPartialTicks() * (Minecraft.getMinecraft().player.posZ - Minecraft.getMinecraft().player.lastTickPosZ);

        GlStateManager.pushMatrix();
        for (TileEntity te : list) {
            if (te instanceof TileEntityAlchemyTablet) {
                if (embers$alchemyTabletRenderer == null) {
                    embers$alchemyTabletRenderer = (ITileEntitySpecialRendererLater)
                            TileEntityRendererDispatcher.instance.getRenderer(TileEntityAlchemyTablet.class);
                }
                embers$alchemyTabletRenderer.renderLater(
                        te,
                        te.getPos().getX() - playerX,
                        te.getPos().getY() - playerY,
                        te.getPos().getZ() - playerZ,
                        event.getPartialTicks()
                );
            }
        }
        GlStateManager.popMatrix();
    }
}