package com.smd.gctcore.common.world.NilfheimDim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.IRenderHandler;

import java.util.Random;

public class NilfheimSnowRenderer extends IRenderHandler {
    private static final ResourceLocation SNOW_TEXTURES = new ResourceLocation("textures/environment/snow.png");
    private final Random random = new Random();

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) {
            return;
        }

        int centerX = MathHelper.floor(entity.posX);
        int centerY = MathHelper.floor(entity.posY);
        int centerZ = MathHelper.floor(entity.posZ);
        int radius = mc.gameSettings.fancyGraphics ? 10 : 6;
        double cameraX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double cameraY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double cameraZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.disableCull();
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(SNOW_TEXTURES);

        buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.setTranslation(-cameraX, -cameraY, -cameraZ);
        float ticks = (float) (world.getTotalWorldTime() % 512L) + partialTicks;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int z = centerZ - radius; z <= centerZ + radius; z++) {
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                mutable.setPos(x, 0, z);
                Biome biome = world.getBiome(mutable);
                if (!biome.getEnableSnow()) {
                    continue;
                }

                int top = world.getPrecipitationHeight(mutable).getY();
                int minY = Math.max(centerY - radius, top);
                int maxY = Math.max(centerY + radius, top);
                if (minY == maxY) {
                    continue;
                }

                random.setSeed((long) (x * x * 3121 + x * 45238971 ^ z * z * 418711 + z * 13761));
                double driftU = random.nextDouble() + ticks * 0.01D * random.nextGaussian();
                double driftV = random.nextDouble() + ticks * random.nextGaussian() * 0.001D;
                double dx = x + 0.5D - entity.posX;
                double dz = z + 0.5D - entity.posZ;
                float distance = MathHelper.sqrt(dx * dx + dz * dz) / (float) radius;
                float alpha = ((1.0F - distance * distance) * 0.32F + 0.42F) * 0.82F;
                if (alpha <= 0.05F) {
                    continue;
                }

                double windX = 0.45D;
                double windZ = 0.18D;
                double v = -(((world.getTotalWorldTime() & 511L) + partialTicks) / 512.0D);
                mutable.setPos(x, top, z);
                int light = (world.getCombinedLight(mutable, 0) * 3 + 15728880) / 4;
                int lightU = light >> 16 & 65535;
                int lightV = light & 65535;

                buffer.pos(x - windX + 0.5D, maxY, z - windZ + 0.5D).tex(0.0D + driftU, minY * 0.25D + v + driftV).color(0.78F, 0.88F, 1.0F, alpha).lightmap(lightU, lightV).endVertex();
                buffer.pos(x + windX + 0.5D, maxY, z + windZ + 0.5D).tex(1.0D + driftU, minY * 0.25D + v + driftV).color(0.78F, 0.88F, 1.0F, alpha).lightmap(lightU, lightV).endVertex();
                buffer.pos(x + windX + 0.5D, minY, z + windZ + 0.5D).tex(1.0D + driftU, maxY * 0.25D + v + driftV).color(0.78F, 0.88F, 1.0F, alpha).lightmap(lightU, lightV).endVertex();
                buffer.pos(x - windX + 0.5D, minY, z - windZ + 0.5D).tex(0.0D + driftU, maxY * 0.25D + v + driftV).color(0.78F, 0.88F, 1.0F, alpha).lightmap(lightU, lightV).endVertex();
            }
        }

        tessellator.draw();
        buffer.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
    }
}
