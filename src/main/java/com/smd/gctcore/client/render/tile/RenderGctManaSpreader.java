package com.smd.gctcore.client.render.tile;

import com.smd.gctcore.common.blocks.botania.BlockGctManaSpreader;
import com.smd.gctcore.common.tile.botania.TileGctManaSpreader;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import javax.annotation.Nonnull;

public class RenderGctManaSpreader extends TileEntitySpecialRenderer<TileGctManaSpreader> {

    @Override
    public void render(@Nonnull TileGctManaSpreader spreader, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (spreader.getWorld() == null || !spreader.getWorld().isBlockLoaded(spreader.getPos(), false)) {
            return;
        }
        IBlockState state = spreader.getWorld().getBlockState(spreader.getPos());
        if (!(state.getBlock() instanceof BlockGctManaSpreader)) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(state);
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
        GlStateManager.rotate(spreader.rotationX + 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(spreader.rotationY, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-0.5D, -0.5D, -0.5D);
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc.getBlockRendererDispatcher().getBlockModelRenderer()
                .renderModelBrightnessColor(model, 1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }
}
