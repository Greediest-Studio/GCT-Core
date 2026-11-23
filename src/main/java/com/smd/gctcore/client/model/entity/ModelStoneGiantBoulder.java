package com.smd.gctcore.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Exact three-cuboid mesh and box-UV offsets from stone_giant_boulder.geo.json. */
@SideOnly(Side.CLIENT)
public class ModelStoneGiantBoulder extends ModelBase {

    private final ModelRenderer[] parts;

    public ModelStoneGiantBoulder() {
        textureWidth = textureHeight = 256;
        parts = new ModelRenderer[] {
                part(111, 1, -11.0F, -9.5F, -11.0F, 22, 18, 22),
                part(41, 225, -9.0F, 8.5F, -9.0F, 18, 3, 18),
                part(179, 1, -9.0F, -11.5F, -9.0F, 18, 2, 18)
        };
    }

    private ModelRenderer part(int u, int v, float x, float y, float z, int w, int h, int d) {
        ModelRenderer part = new ModelRenderer(this, u, v);
        part.addBox(x, y, z, w, h, d);
        return part;
    }

    public void render(float scale) {
        for (ModelRenderer part : parts) {
            part.render(scale);
        }
    }
}
