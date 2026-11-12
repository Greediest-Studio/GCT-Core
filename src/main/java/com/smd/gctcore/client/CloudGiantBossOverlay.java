package com.smd.gctcore.client;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityCloudGiantPriest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Uses Aether's installed texture with vanilla's multiplayer boss tracking. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class CloudGiantBossOverlay {

    private static final ResourceLocation AETHER_BAR =
            new ResourceLocation("aether_legacy", "textures/gui/boss_bar.png");

    private CloudGiantBossOverlay() {}

    @SubscribeEvent
    public static void renderBossBar(RenderGameOverlayEvent.BossInfo event) {
        if (!event.getBossInfo().getName().getUnformattedText()
                .endsWith(EntityCloudGiantPriest.BOSS_TITLE_SUFFIX)) {
            return;
        }

        // Aether's artwork stays in Aether; the vanilla bar works without it.
        if (!Loader.isModLoaded("aether_legacy")) {
            return;
        }

        event.setCanceled(true);
        event.setIncrement(48);

        Minecraft mc = Minecraft.getMinecraft();
        int center = event.getResolution().getScaledWidth() / 2;
        int y = event.getY();
        int fill = MathHelper.floor(MathHelper.clamp(event.getBossInfo().getPercent(), 0.0F, 1.0F) * 256);
        float scale = Math.min(1.0F, (event.getResolution().getScaledWidth() - 16) / 256.0F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(center, y, 0);
        GlStateManager.scale(scale, 1.0F, 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(AETHER_BAR);
        Gui.drawModalRectWithCustomSizedTexture(-128, 0, 0, 16, 256, 32, 256, 256);
        if (fill > 0) {
            Gui.drawModalRectWithCustomSizedTexture(-128, 0, 0, 0, fill, 16, 256, 256);
        }
        GlStateManager.popMatrix();

        String title = event.getBossInfo().getName().getFormattedText();
        mc.fontRenderer.drawStringWithShadow(title, center - mc.fontRenderer.getStringWidth(title) / 2.0F,
                y - 10, 0xFFFFFF);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
