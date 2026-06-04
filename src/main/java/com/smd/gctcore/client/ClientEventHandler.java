package com.smd.gctcore.client;

import com.smd.gctcore.Tags;
import com.smd.gctcore.misc.PotionsItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public class ClientEventHandler {

    private static final ResourceLocation OVERLAY = new ResourceLocation(Tags.MOD_ID, "textures/overlay/sukhavati_render_overlay.png");

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        if (PotionsItemRegistry.SUKHAVATI == null) return; // not yet registered

        if (!mc.player.isPotionActive(PotionsItemRegistry.SUKHAVATI)) return;

        ScaledResolution sr = new ScaledResolution(mc);
        int width = sr.getScaledWidth();
        int height = sr.getScaledHeight();

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();

        mc.getTextureManager().bindTexture(OVERLAY);
        mc.ingameGUI.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, (float) width, (float) height);

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }
}
