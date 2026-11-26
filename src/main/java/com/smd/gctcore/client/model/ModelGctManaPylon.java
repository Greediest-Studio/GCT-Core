package com.smd.gctcore.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Shared eight-box crystal and ring silhouette used by Botaniverse 1.1-1's
 * four pylon models. Geometry and the 64x64 vanilla box UV layout are identical
 * across its variants; the caller binds the desired GCT-Core tier texture.
 * Ring/crystal transforms and shader choices belong to the caller.
 */
@SideOnly(Side.CLIENT)
public final class ModelGctManaPylon extends ModelBase {
    private static final float SCALE = 1.0F / 16.0F;
    private final ModelRenderer plateFront;
    private final ModelRenderer plateBack;
    private final ModelRenderer plateLeft;
    private final ModelRenderer plateRight;
    private final ModelRenderer shardLeftFront;
    private final ModelRenderer shardRightFront;
    private final ModelRenderer shardLeftBack;
    private final ModelRenderer shardRightBack;

    public ModelGctManaPylon() {
        textureWidth = 64;
        textureHeight = 64;
        plateFront = box(36, 0, -3, -4, -8, 6, 8, 2);
        plateBack = box(36, 0, -3, -4, -8, 6, 8, 2);
        plateBack.rotateAngleY = (float) Math.PI;
        plateLeft = box(36, 0, -3, -4, -8, 6, 8, 2);
        plateLeft.rotateAngleY = (float) Math.PI / 2.0F;
        plateRight = box(36, 0, -3, -4, -8, 6, 8, 2);
        plateRight.rotateAngleY = -(float) Math.PI / 2.0F;
        shardLeftFront = box(0, 21, -5, -9, -5, 5, 16, 3);
        shardRightFront = box(16, 21, 2, -12, -5, 3, 16, 3);
        shardLeftBack = box(0, 0, -5, -10, 0, 6, 16, 5);
        shardRightBack = box(22, 0, 3, -11, 0, 2, 16, 5);
    }

    private ModelRenderer box(int u, int v, float x, float y, float z, int width, int height, int depth) {
        ModelRenderer part = new ModelRenderer(this, u, v);
        part.setRotationPoint(0.0F, 16.0F, 0.0F);
        part.addBox(x, y, z, width, height, depth, 0.0F);
        return part;
    }

    public void renderRing() {
        plateFront.render(SCALE);
        plateBack.render(SCALE);
        plateLeft.render(SCALE);
        plateRight.render(SCALE);
    }

    public void renderCrystal() {
        shardLeftFront.render(SCALE);
        shardRightFront.render(SCALE);
        shardLeftBack.render(SCALE);
        shardRightBack.render(SCALE);
    }

    public void renderAll() {
        renderRing();
        renderCrystal();
    }
}
