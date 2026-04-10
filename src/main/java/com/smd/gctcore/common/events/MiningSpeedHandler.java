package com.smd.gctcore.common.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class MiningSpeedHandler {

    @SideOnly(Side.CLIENT)
    public static final KeyBinding KEY_MINING_SPEED_ADD = new KeyBinding(
            "key.gctcore.mining_speed_add",
            Keyboard.KEY_ADD,
            "key.gctcore.categories");

    @SideOnly(Side.CLIENT)
    public static final KeyBinding KEY_MINING_SPEED_MINUS = new KeyBinding(
            "key.gctcore.mining_speed_minus",
            Keyboard.KEY_SUBTRACT,
            "key.gctcore.categories");

    private static float miningMultiplier = 1.0f;

    private static long lastAdjustTime = 0;
    private static final int HUD_DISPLAY_DURATION = 2000;
    private static final boolean ShowHud = true;

    /**
     * 按键事件 - 调整挖掘倍率
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null) {
            return;
        }

        boolean updated = false;

        if (KEY_MINING_SPEED_ADD.isPressed()) {
            miningMultiplier = Math.min(miningMultiplier + 0.05f, 1.0f);
            updated = true;
        }

        if (KEY_MINING_SPEED_MINUS.isPressed()) {
            miningMultiplier = Math.max(miningMultiplier - 0.05f, 0.1f);
            updated = true;
        }

        if (updated) {
            lastAdjustTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        float originalSpeed = event.getOriginalSpeed();
        float newSpeed = Math.max(originalSpeed * miningMultiplier, 0.1f);
        event.setNewSpeed(newSpeed);
    }

    /**
     * 渲染 - 显示挖掘倍率 (显示 2 秒)
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (!ShowHud || event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null || mc.world == null) {
            return;
        }

        if (mc.currentScreen != null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAdjustTime > HUD_DISPLAY_DURATION) {
            return;
        }

        String text = I18n.format("hud.mining_speed", String.format("%.2f", miningMultiplier));

        int stringWidth = mc.fontRenderer.getStringWidth(text);
        int stringHeight = mc.fontRenderer.FONT_HEIGHT;

        int screenWidth = event.getResolution().getScaledWidth();
        int screenHeight = event.getResolution().getScaledHeight();

        int xPos = screenWidth / 2 - stringWidth / 2;
        int yPos = screenHeight - 50;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        int backgroundColor = 0x60000000;

        drawRect(xPos - 3, yPos - 2, xPos + stringWidth + 3, yPos + stringHeight + 2, backgroundColor);
        mc.fontRenderer.drawStringWithShadow(text, xPos, yPos, 0xFFFFFFFF);

        GlStateManager.disableBlend();
    }

    @SideOnly(Side.CLIENT)
    private void drawRect(int left, int top, int right, int bottom, int color) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(GL11.GL_QUADS, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
