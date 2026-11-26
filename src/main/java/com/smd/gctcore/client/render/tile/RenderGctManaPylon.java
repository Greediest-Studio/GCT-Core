package com.smd.gctcore.client.render.tile;

import com.smd.gctcore.Tags;
import com.smd.gctcore.client.model.ModelGctManaPylon;
import com.smd.gctcore.common.blocks.botania.BlockGctManaPylon;
import com.smd.gctcore.common.botania.GctManaPoolTier;
import com.smd.gctcore.common.tile.botania.TileGctManaPylon;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.Random;

/** Independent plain-texture renderer; does not load Botaniverse classes. */
@SideOnly(Side.CLIENT)
public class RenderGctManaPylon extends TileEntitySpecialRenderer<TileGctManaPylon> {
    private static final ModelGctManaPylon MODEL = new ModelGctManaPylon();
    private static final ResourceLocation[] TEXTURES = {
            texture(GctManaPoolTier.JOETUNHEIM),
            texture(GctManaPoolTier.NIDAVELLIR),
            texture(GctManaPoolTier.VANAHEIM)
    };

    private static ResourceLocation texture(GctManaPoolTier tier) {
        return new ResourceLocation(Tags.MOD_ID, "textures/model/pylon_" + tier.getName() + ".png");
    }

    @Override
    public void render(@Nonnull TileGctManaPylon pylon, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (pylon.getWorld() == null || !pylon.getWorld().isBlockLoaded(pylon.getPos(), false)) {
            return;
        }
        IBlockState state = pylon.getWorld().getBlockState(pylon.getPos());
        if (!(state.getBlock() instanceof BlockGctManaPylon)) {
            return;
        }

        // Preserve the reference's stable per-position phase without ticking
        // or synchronising a tile counter on the server.
        double time = pylon.getWorld().getTotalWorldTime() + partialTicks
                + new Random(pylon.getPos().hashCode()).nextInt(360);
        bindTexture(TEXTURES[pylon.getTier().ordinal()]);

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        GlStateManager.translate(x, y + 1.5D, z);
        GlStateManager.scale(1.0F, -1.0F, -1.0F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5D, 0.0D, -0.5D);
        GlStateManager.rotate((float) ((time * 1.5D) % 360.0D), 0.0F, 1.0F, 0.0F);
        MODEL.renderRing();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5D, Math.sin(time / 20.0D) / 17.5D, -0.5D);
        GlStateManager.rotate((float) (-time % 360.0D), 0.0F, 1.0F, 0.0F);
        GlStateManager.disableCull();
        // Cut out transparent texels so they cannot hide another shard by
        // writing invisible depth. Only the crystal glows; its ring stays lit
        // by the surrounding world.
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        MODEL.renderCrystal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                lastBrightnessX, lastBrightnessY);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();

        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
