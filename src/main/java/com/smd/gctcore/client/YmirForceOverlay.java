package com.smd.gctcore.client;

import com.smd.gctcore.common.items.ItemGiantTalisman;
import com.smd.gctcore.common.world.jotunheim.JotunheimTerrainKernel.HeightLayer;
import com.smd.gctcore.common.world.jotunheim.WorldProviderJotunheim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** A small altitude scale; green means the carried talisman permits that stratum. */
@SideOnly(Side.CLIENT)
public final class YmirForceOverlay {
    // The same island-body floors as the server rules, not typical terrain surfaces.
    private static final int[] LEVELS = {
            255, HeightLayer.CLOUD.minY, HeightLayer.SURFACE.minY, HeightLayer.DEEP.minY, 0
    };
    private static final String[] LABELS = {null, "cloud", "surface", "deep", "lava"};
    private static final int SAFE = 0xFF568F61;
    private static final int DANGER = 0xFFA04442;
    private static final int DARK_FRAME = 0xFF14202B;
    private static final int SHIELD_FRAME = 0xFF163D72;
    private static final int SHIELD_EDGE = 0xFF4776A7;

    private static boolean active;
    private static boolean shielded;
    private static int dimension = Integer.MIN_VALUE;
    private static World synchronizedWorld;

    /** Called on the client thread by PacketYmirForce. */
    public static void update(boolean enabled, boolean protectedByWell, int dimensionId) {
        Minecraft minecraft = Minecraft.getMinecraft();
        World world = minecraft.world;
        if (world == null || minecraft.player == null) {
            clear();
            return;
        }
        // Ignore a late packet from the dimension the player has just left.
        if (!(world.provider instanceof WorldProviderJotunheim)
                || world.provider.getDimension() != dimensionId) return;
        if (!enabled) {
            clear();
            return;
        }
        synchronizedWorld = world;
        dimension = dimensionId;
        active = true;
        shielded = protectedByWell;
    }

    public static void clear() {
        active = false;
        shielded = false;
        dimension = Integer.MIN_VALUE;
        synchronizedWorld = null;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (active && !matchesWorld(minecraft)) clear();
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote && event.getWorld() == synchronizedWorld) clear();
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        World oldWorld = synchronizedWorld;
        minecraft.addScheduledTask(() -> {
            if (synchronizedWorld == oldWorld) clear();
        });
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!active || !matchesWorld(minecraft) || minecraft.gameSettings.hideGUI
                || minecraft.currentScreen != null) return;

        int screenHeight = event.getResolution().getScaledHeight();
        int height = Math.max(64, Math.min(144, screenHeight - 72));
        int bottom = screenHeight - 36;
        int top = bottom - height;
        int left = 14;
        int width = 9;
        int permittedLayer = carriedLayer(minecraft.player);

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (shielded) {
            Gui.drawRect(left - 3, top - 3, left + width + 3, bottom + 3, SHIELD_FRAME);
            frame(left - 4, top - 4, left + width + 4, bottom + 4, SHIELD_EDGE);
        }
        Gui.drawRect(left - 1, top - 1, left + width + 1, bottom + 1, DARK_FRAME);
        for (int section = 0; section < 4; section++) {
            int sectionTop = altitudeY(LEVELS[section], top, height);
            int sectionBottom = altitudeY(LEVELS[section + 1], top, height);
            int color = permittedLayer == 3 - section ? SAFE : DANGER;
            Gui.drawRect(left, sectionTop, left + width, sectionBottom, color);
            Gui.drawRect(left, sectionTop, left + 1, sectionBottom, brighten(color));
            Gui.drawRect(left + width - 2, sectionTop, left + width, sectionBottom, shade(color));
            // Quiet pixel marks keep each region readable without a separate texture.
            for (int markY = sectionTop + 5; markY < sectionBottom - 1; markY += 6) {
                Gui.drawRect(left + width - 3, markY, left + width - 1, markY + 1, 0x65000000);
            }
        }
        for (int index = 0; index < LEVELS.length; index++) {
            int y = altitudeY(LEVELS[index], top, height);
            Gui.drawRect(left - 1, y - 1, left + width + 2, y + 1, DARK_FRAME);
            Gui.drawRect(left + 2, y - 1, left + width - 2, y + 1, 0xFFADAFAC);
            String label = Integer.toString(LEVELS[index]);
            if (LABELS[index] != null) {
                label = I18n.format("gui.gctcore.ymir_force." + LABELS[index]) + " " + label;
            }
            minecraft.fontRenderer.drawStringWithShadow(label, left + width + 5, y - 4, 0xD3D4CE);
        }

        double playerY = minecraft.player.lastTickPosY
                + (minecraft.player.posY - minecraft.player.lastTickPosY) * event.getPartialTicks();
        int arrowY = altitudeY(playerY, top, height);
        Gui.drawRect(left - 8, arrowY - 3, left - 5, arrowY + 4, 0xFF111920);
        Gui.drawRect(left - 5, arrowY - 2, left - 3, arrowY + 3, 0xFF111920);
        Gui.drawRect(left - 3, arrowY - 1, left - 1, arrowY + 2, 0xFF111920);
        Gui.drawRect(left - 7, arrowY - 2, left - 5, arrowY + 3, 0xFFF0E5B8);
        Gui.drawRect(left - 5, arrowY - 1, left - 3, arrowY + 2, 0xFFF0E5B8);
        Gui.drawRect(left - 3, arrowY, left - 2, arrowY + 1, 0xFFF0E5B8);

        if (shielded) drawShield(left + width / 2, top - 10);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private static boolean matchesWorld(Minecraft minecraft) {
        return minecraft.player != null && minecraft.world != null && minecraft.world == synchronizedWorld
                && minecraft.world.provider instanceof WorldProviderJotunheim
                && minecraft.world.provider.getDimension() == dimension
                && minecraft.player.dimension == dimension;
    }

    private static int carriedLayer(EntityPlayer player) {
        int highest = -1;
        for (ItemStack stack : player.inventory.mainInventory) highest = higherLayer(highest, stack);
        for (ItemStack stack : player.inventory.offHandInventory) highest = higherLayer(highest, stack);
        for (ItemStack stack : player.inventory.armorInventory) highest = higherLayer(highest, stack);
        return higherLayer(highest, player.inventory.getItemStack());
    }

    private static int higherLayer(int current, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemGiantTalisman)) return current;
        int layer = ((ItemGiantTalisman) stack.getItem()).layerIndex(stack);
        return layer >= 0 && layer < 4 ? Math.max(current, layer) : current;
    }

    private static int altitudeY(double y, int top, int height) {
        return top + (int) Math.round((255.0D - MathHelper.clamp(y, 0.0D, 255.0D)) * height / 255.0D);
    }

    private static void frame(int left, int top, int right, int bottom, int color) {
        Gui.drawRect(left, top, right, top + 1, color);
        Gui.drawRect(left, bottom - 1, right, bottom, color);
        Gui.drawRect(left, top, left + 1, bottom, color);
        Gui.drawRect(right - 1, top, right, bottom, color);
    }

    private static void drawShield(int centerX, int top) {
        Gui.drawRect(centerX - 4, top, centerX + 5, top + 5, SHIELD_EDGE);
        Gui.drawRect(centerX - 3, top + 5, centerX + 4, top + 7, SHIELD_EDGE);
        Gui.drawRect(centerX - 1, top + 7, centerX + 2, top + 8, SHIELD_EDGE);
        Gui.drawRect(centerX - 2, top + 1, centerX + 3, top + 5, SHIELD_FRAME);
        Gui.drawRect(centerX - 1, top + 5, centerX + 2, top + 6, SHIELD_FRAME);
    }

    private static int brighten(int color) {
        return 0xFF000000 | Math.min(255, ((color >> 16) & 255) + 35) << 16
                | Math.min(255, ((color >> 8) & 255) + 35) << 8 | Math.min(255, (color & 255) + 35);
    }

    private static int shade(int color) {
        return 0xFF000000 | (((color >> 16) & 255) * 3 / 4) << 16
                | (((color >> 8) & 255) * 3 / 4) << 8 | (color & 255) * 3 / 4;
    }
}
