package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.client.model.entity.ModelStoneGiantBoulder;
import com.smd.gctcore.common.entity.EntityStoneGiantBoulder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderStoneGiantBoulder extends Render<EntityStoneGiantBoulder> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Tags.MOD_ID, "textures/entity/stone_giant_boulder.png");
    private static final float MODEL_SCALE = 88.0F / 127.0F;
    private final ModelStoneGiantBoulder model = new ModelStoneGiantBoulder();

    public RenderStoneGiantBoulder(RenderManager manager) {
        super(manager);
        shadowSize = 0.4F;
    }

    @Override
    public void doRender(EntityStoneGiantBoulder entity, double x, double y, double z,
                         float yaw, float partialTicks) {
        bindEntityTexture(entity);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height * 0.5F, z);
        // A physical rock tumbles throughout its flight, rather than facing its target.
        GlStateManager.rotate((entity.ticksExisted + partialTicks) * 11.0F, 1.0F, 0.35F, 0.2F);
        GlStateManager.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(getTeamColor(entity));
        }
        model.render(1.0F / 16.0F);
        if (renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, yaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityStoneGiantBoulder entity) {
        return TEXTURE;
    }
}
