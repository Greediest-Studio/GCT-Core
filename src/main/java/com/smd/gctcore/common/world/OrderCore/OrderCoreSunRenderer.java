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
public class OrderCoreSunRenderer extends IRenderHandler {
    
    // 自定义太阳贴图 - 可以使用自定义的太阳图像
    private static final ResourceLocation SUN_TEXTURE = new ResourceLocation("gctcore", "textures/environment/ordercore_sun.png");
    // 如果没有自定义贴图，可以使用原版太阳
    private static final ResourceLocation VANILLA_SUN = new ResourceLocation("textures/environment/sun.png");
    
    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        // 获取天体角度
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
        
        // 设置自定义太阳颜色 - 这里设置为金红色
        GlStateManager.color(1.0F, 0.6F, 0.2F, 1.0F); // 金红色太阳
        
        // 旋转到太阳位置
        GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        
        // 绑定太阳贴图
        try {
            mc.getTextureManager().bindTexture(SUN_TEXTURE);
        } catch (Exception e) {
            // 如果自定义贴图不存在，使用原版太阳贴图
            mc.getTextureManager().bindTexture(VANILLA_SUN);
        }
        
        // 渲染太阳
        renderSun();
        
        // 恢复默认颜色
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableFog();
    }
    
    private void renderSun() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        float sunSize = 20.0F; // 太阳大小
        
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX); // 7 = GL_QUADS
        
        // 渲染太阳四边形
        buffer.pos(-sunSize, 100.0, -sunSize).tex(0.0, 0.0).endVertex();
        buffer.pos(sunSize, 100.0, -sunSize).tex(1.0, 0.0).endVertex();
        buffer.pos(sunSize, 100.0, sunSize).tex(1.0, 1.0).endVertex();
        buffer.pos(-sunSize, 100.0, sunSize).tex(0.0, 1.0).endVertex();
        
        tessellator.draw();
    }
}