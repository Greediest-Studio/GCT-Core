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
public class OrderCoreSkyRenderer extends IRenderHandler {
    
    private static final ResourceLocation SKY_TEXTURE = new ResourceLocation("gctcore", "textures/environment/ordercore_sky.png");
    
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
        
        // 渲染天空盒背景
        mc.getTextureManager().bindTexture(SKY_TEXTURE);
        renderSkyBox();
        
        // 渲染太阳和月亮
        renderCelestialBodies(celestialAngle, mc);
        
        GlStateManager.popMatrix();
        
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        // 保持雾效禁用状态，不重新启用
        // GlStateManager.enableFog(); // 注释掉这行以保持雾效禁用
    }
    
    private void renderSkyBox() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        float size = 100.0F;
        
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        
        // 上面
        buffer.pos(-size, size, -size).tex(0.0, 1.0).endVertex();
        buffer.pos(-size, size, size).tex(0.0, 0.0).endVertex();
        buffer.pos(size, size, size).tex(1.0, 0.0).endVertex();
        buffer.pos(size, size, -size).tex(1.0, 1.0).endVertex();
        
        // 北面
        buffer.pos(-size, -size, -size).tex(0.0, 1.0).endVertex();
        buffer.pos(-size, size, -size).tex(0.0, 0.0).endVertex();
        buffer.pos(size, size, -size).tex(1.0, 0.0).endVertex();
        buffer.pos(size, -size, -size).tex(1.0, 1.0).endVertex();
        
        // 南面
        buffer.pos(size, -size, size).tex(0.0, 1.0).endVertex();
        buffer.pos(size, size, size).tex(0.0, 0.0).endVertex();
        buffer.pos(-size, size, size).tex(1.0, 0.0).endVertex();
        buffer.pos(-size, -size, size).tex(1.0, 1.0).endVertex();
        
        // 东面
        buffer.pos(size, -size, -size).tex(0.0, 1.0).endVertex();
        buffer.pos(size, size, -size).tex(0.0, 0.0).endVertex();
        buffer.pos(size, size, size).tex(1.0, 0.0).endVertex();
        buffer.pos(size, -size, size).tex(1.0, 1.0).endVertex();
        
        // 西面
        buffer.pos(-size, -size, size).tex(0.0, 1.0).endVertex();
        buffer.pos(-size, size, size).tex(0.0, 0.0).endVertex();
        buffer.pos(-size, size, -size).tex(1.0, 0.0).endVertex();
        buffer.pos(-size, -size, -size).tex(1.0, 1.0).endVertex();
        
        // 下面 (通常不可见，可以省略)
        buffer.pos(size, -size, -size).tex(0.0, 1.0).endVertex();
        buffer.pos(size, -size, size).tex(0.0, 0.0).endVertex();
        buffer.pos(-size, -size, size).tex(1.0, 0.0).endVertex();
        buffer.pos(-size, -size, -size).tex(1.0, 1.0).endVertex();
        
        tessellator.draw();
    }
    
    private void renderCelestialBodies(float celestialAngle, Minecraft mc) {
        GlStateManager.pushMatrix();
        
        // 渲染太阳
        GlStateManager.color(0.1F, 0.1F, 0.1F, 1.0F); // 黑色太阳
        GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
        
        try {
            ResourceLocation sunTexture = new ResourceLocation("textures/environment/sun.png");
            mc.getTextureManager().bindTexture(sunTexture);
            renderCelestialObject(20.0F, 100.0F);
        } catch (Exception e) {
            // 如果贴图加载失败，渲染一个简单的发光球体
            renderSimpleSun();
        }
        
        // 重置颜色和旋转
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(-celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
        
        // 渲染月亮
        GlStateManager.color(0.95F, 0.95F, 1.0F, 0.9F); // 苍白色月亮
        GlStateManager.rotate((celestialAngle + 0.5F) * 360.0F, 1.0F, 0.0F, 0.0F);
        
        try {
            ResourceLocation moonTexture = new ResourceLocation("textures/environment/moon_phases.png");
            mc.getTextureManager().bindTexture(moonTexture);
            renderCelestialObject(15.0F, -100.0F);
        } catch (Exception e) {
            // 如果贴图加载失败，渲染一个简单的发光球体
            renderSimpleMoon();
        }
        
        // 恢复颜色
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        GlStateManager.popMatrix();
    }
    
    private void renderCelestialObject(float size, float distance) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        
        buffer.pos(-size, distance, -size).tex(0.0, 0.0).endVertex();
        buffer.pos(size, distance, -size).tex(1.0, 0.0).endVertex();
        buffer.pos(size, distance, size).tex(1.0, 1.0).endVertex();
        buffer.pos(-size, distance, size).tex(0.0, 1.0).endVertex();
        
        tessellator.draw();
    }
    
    private void renderSimpleSun() {
        // 渲染简单的太阳 - 发光的圆形
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        
        float sunSize = 20.0F;
        float distance = 100.0F;
        
        // 创建一个黑色的四边形
        buffer.pos(-sunSize, distance, -sunSize).color(0.1F, 0.1F, 0.1F, 1.0F).endVertex();
        buffer.pos(sunSize, distance, -sunSize).color(0.1F, 0.1F, 0.1F, 1.0F).endVertex();
        buffer.pos(sunSize, distance, sunSize).color(0.05F, 0.05F, 0.05F, 1.0F).endVertex();
        buffer.pos(-sunSize, distance, sunSize).color(0.05F, 0.05F, 0.05F, 1.0F).endVertex();
        
        tessellator.draw();
    }
    
    private void renderSimpleMoon() {
        // 渲染简单的月亮 - 发光的圆形
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        
        float moonSize = 15.0F;
        float distance = -100.0F;
        
        // 创建一个苍白色的四边形
        buffer.pos(-moonSize, distance, -moonSize).color(0.95F, 0.95F, 1.0F, 0.9F).endVertex();
        buffer.pos(moonSize, distance, -moonSize).color(0.95F, 0.95F, 1.0F, 0.9F).endVertex();
        buffer.pos(moonSize, distance, moonSize).color(0.9F, 0.9F, 0.98F, 0.9F).endVertex();
        buffer.pos(-moonSize, distance, moonSize).color(0.9F, 0.9F, 0.98F, 0.9F).endVertex();
        
        tessellator.draw();
    }
}