package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.client.model.entity.ModelCloudGiantCrystal;
import com.smd.gctcore.common.entity.EntityCloudGiantCrystal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@SideOnly(Side.CLIENT)
public class RenderCloudGiantCrystal extends GeoEntityRenderer<EntityCloudGiantCrystal> {

    private static final float[] BEAM_COLOR = {0.12F, 0.48F, 1.0F};

    public RenderCloudGiantCrystal(RenderManager renderManager) {
        super(renderManager, new ModelCloudGiantCrystal());
        shadowSize = 0.0F;
    }

    @Override
    public void doRender(EntityCloudGiantCrystal entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        if (!entity.isActivated()) {
            return;
        }
        bindTexture(TileEntityBeaconRenderer.TEXTURE_BEACON_BEAM);
        TileEntityBeaconRenderer.renderBeamSegment(
                x - 0.5D, y + 0.25D, z - 0.5D, partialTicks, 1.0D,
                entity.world.getTotalWorldTime(), 0, 256, BEAM_COLOR, 0.16D, 0.28D);
    }
}
