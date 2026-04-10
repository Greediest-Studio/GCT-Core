package com.smd.gctcore.common.world.OrderCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class OrderCoreMoonRenderer extends IRenderHandler {
    
    // 自定义月亮贴图
    private static final ResourceLocation MOON_TEXTURE = new ResourceLocation("gctcore", "textures/environment/ordercore_moon.png");
    // 原版月亮贴图作为备用
    private static final ResourceLocation VANILLA_MOON = new ResourceLocation("textures/environment/moon_phases.png");
    
    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        float celestialAngle = world.getCelestialAngle(partialTicks);
        
        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, 
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, 
            GlStateManager.SourceFactor.ONE, 
            GlStateManager.DestFactor.ZERO
        );
        
        GlStateManager.pushMatrix();
        
        // 设置自定义月亮颜色 - 这里设置为蓝紫色
        GlStateManager.color(0.7F, 0.8F, 1.0F, 1.0F); // 蓝紫色月亮
        
        // 旋转到月亮位置 (与太阳相对)
        GlStateManager.rotate((celestialAngle + 0.5F) * 360.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        
        // 绑定月亮贴图
        try {
            mc.getTextureManager().bindTexture(MOON_TEXTURE);
        } catch (Exception e) {
            mc.getTextureManager().bindTexture(VANILLA_MOON);
        }
        
        // 渲染月亮
        renderMoon(world.getMoonPhase());
        
        // 恢复默认颜色
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableFog();
    }
    
    private void renderMoon(int moonPhase) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        float moonSize = 15.0F; // 月亮大小
        
        // 根据月相计算UV坐标
        int phaseU = moonPhase % 4;
        int phaseV = moonPhase / 4 % 2;
        float u1 = phaseU / 4.0F;
        float u2 = (phaseU + 1) / 4.0F;
        float v1 = phaseV / 2.0F;
        float v2 = (phaseV + 1) / 2.0F;
        
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        
        // 渲染月亮四边形
        buffer.pos(-moonSize, -100.0, moonSize).tex(u2, v2).endVertex();
        buffer.pos(moonSize, -100.0, moonSize).tex(u1, v2).endVertex();
        buffer.pos(moonSize, -100.0, -moonSize).tex(u1, v1).endVertex();
        buffer.pos(-moonSize, -100.0, -moonSize).tex(u2, v1).endVertex();
        
        tessellator.draw();
    }
}