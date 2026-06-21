package com.smd.gctcore.client;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.clientstate.NilfheimErosionClientState;
import com.smd.gctcore.common.world.biome.nilfheim.NilfheimBiomes;
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

        renderNilfheimErosionBar(event, mc);

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

    private static void renderNilfheimErosionBar(RenderGameOverlayEvent.Post event, Minecraft mc) {
        if (mc.player.dimension != NilfheimBiomes.DIMENSION_ID || !NilfheimErosionClientState.active || mc.currentScreen != null) {
            return;
        }

        ScaledResolution sr = event.getResolution();
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;
        int x = centerX + 13;
        int y = centerY - 13;
        int width = 5;
        int height = 26;
        float progress = NilfheimErosionClientState.progress;
        int fill = Math.round((height - 2) * progress);

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        drawRect(x - 1, y - 1, x + width + 1, y + height + 1, 0x90020A12);
        drawRect(x, y, x + width, y + height, 0xB0142438);

        int fillColor = progress >= 1.0F ? 0xFF9BE6FF : 0xD86AB6D7;
        drawRect(x + 1, y + height - 1 - fill, x + width - 1, y + height - 1, fillColor);

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private static void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }
}
