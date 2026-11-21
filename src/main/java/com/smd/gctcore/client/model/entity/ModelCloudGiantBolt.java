package com.smd.gctcore.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelCloudGiantBolt extends ModelBase {

    private final ModelRenderer[] parts;

    public ModelCloudGiantBolt() {
        this.textureWidth = 128;
        this.textureHeight = 128;
        // Exact box coordinates and UV offsets from cloud_giant_bolt.geo.json.
        // Authored Y is up; the negative Z end is the cyan metal point.
        this.parts = new ModelRenderer[] {
                part(1, 1, -1.0F, -1.0F, -11.0F, 2, 2, 27),
                part(83, 13, -2.0F, -2.0F, -13.0F, 4, 4, 3),
                part(61, 13, -2.0F, -2.0F, -19.0F, 4, 4, 6),
                part(83, 22, -1.0F, -1.0F, -22.0F, 2, 2, 3),
                part(61, 1, -5.0F, -0.5F, 9.0F, 10, 1, 9),
                part(101, 1, -0.5F, -5.0F, 9.0F, 1, 10, 9)
        };
    }

    private ModelRenderer part(int u, int v, float x, float y, float z, int width, int height, int depth) {
        ModelRenderer part = new ModelRenderer(this, u, v);
        part.addBox(x, y, z, width, height, depth);
        return part;
    }

    public void render(float scale) {
        for (ModelRenderer part : this.parts) {
            part.render(scale);
        }
    }
}
